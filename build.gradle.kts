/*
 * Copyright (C) 2015-2026 Fabio Ticconi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

plugins {
    java
    application
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.github.fabio-t"
version = "0.3.0"

// artemis-odb (2.3.0) runs fine on modern JVMs; the terrain-generator Java
// binding uses the Foreign Function & Memory API and needs Java 22+.
// 25 is the current LTS.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("net.onedaybeard.artemis:artemis-odb:2.3.0")
    implementation("net.mostlyoriginal.artemis-odb:contrib-core:2.5.0")

    implementation("com.github.trystan:AsciiPanel:372dfbae98")
    implementation("com.github.fabio-t:rlforj-alt:0.3.0")

    implementation(platform("com.fasterxml.jackson:jackson-bom:2.22.2"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")

    implementation("ch.qos.logback:logback-classic:1.6.3")

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "com.github.fabioticconi.alone.Main"
    // The terrain generator is called through the FFM API, a restricted operation.
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    filesMatching("project.properties") {
        expand("version" to version, "name" to "AloneRL")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass,
            "Enable-Native-Access" to "ALL-UNNAMED"
        )
    }
}

tasks.shadowJar {
    archiveClassifier = "all"
}

// ---------------------------------------------------------------------------
// Native terrain-generator library
//
// Map generation uses fabio-t/terrain-generator, a Rust library exposed to
// Java over a C ABI. The Java-side binding is vendored under
// src/main/java/com/github/fabioticconi/tergen; the native shared library is
// built from source with cargo (Rust >= 1.91) by the tasks below and placed
// under natives/<os>-<arch>/. At runtime the game looks for it there.
// ---------------------------------------------------------------------------

val tergenRepo = "https://github.com/fabio-t/terrain-generator"
val tergenTag = "v0.2.0"
val tergenSrcDir = layout.buildDirectory.dir("tergen-src")

val osName: String = System.getProperty("os.name").lowercase()
val osArch: String = System.getProperty("os.arch").let {
    when (it) {
        "amd64", "x86_64" -> "x86_64"
        "aarch64", "arm64" -> "aarch64"
        else -> it
    }
}
val platformId: String = when {
    osName.contains("win") -> "windows-$osArch"
    osName.contains("mac") -> "macos-$osArch"
    else -> "linux-$osArch"
}
val nativeLibFileName: String = when {
    osName.contains("win") -> "terrain_generator_ffi.dll"
    osName.contains("mac") -> "libterrain_generator_ffi.dylib"
    else -> "libterrain_generator_ffi.so"
}
val nativesDir = layout.projectDirectory.dir("natives/$platformId")

val fetchTergen = tasks.register("fetchTergen") {
    description = "Clones terrain-generator $tergenTag (skipped if already present)"
    outputs.dir(tergenSrcDir)
    onlyIf { !tergenSrcDir.get().dir(".git").asFile.exists() }
    doLast {
        providers.exec {
            commandLine(
                "git", "clone", "--depth", "1", "--branch", tergenTag,
                tergenRepo, tergenSrcDir.get().asFile.absolutePath
            )
        }.result.get()
    }
}

val buildTergen = tasks.register("buildTergen") {
    description = "Builds the terrain-generator native library with cargo"
    dependsOn(fetchTergen)
    val builtLib = tergenSrcDir.map { it.file("target/release/$nativeLibFileName") }
    outputs.file(builtLib)
    doLast {
        providers.exec {
            workingDir(tergenSrcDir.get().asFile)
            commandLine("cargo", "build", "--release", "-p", "terrain_generator_ffi")
        }.result.get()
    }
}

val nativeLib = tasks.register<Copy>("nativeLib") {
    description = "Places the terrain-generator native library under natives/$platformId"
    dependsOn(buildTergen)
    from(tergenSrcDir.map { it.file("target/release/$nativeLibFileName") })
    into(nativesDir)
}

tasks.named<JavaExec>("run") {
    // The game resolves data/ and natives/ relative to the working directory.
    workingDir = rootDir
}

// ---------------------------------------------------------------------------
// itch.io packaging
//
// `gradle itchPackage` produces build/itch/alone-rl-<version>-<platform>.zip:
// a self-contained app image (bundled JRE via jpackage) with the game data,
// the native terrain library and an itch.io manifest. Run it on each target
// OS (see .github/workflows/release.yml) and push the zips with butler.
// ---------------------------------------------------------------------------

val itchStaging = layout.buildDirectory.dir("itch/staging")
val itchAppImage = layout.buildDirectory.dir("itch/image")

val itchAppImageTask = tasks.register("itchAppImage") {
    description = "Builds a self-contained app image with jpackage"
    dependsOn(tasks.shadowJar, nativeLib)

    val jarProvider = tasks.shadowJar.flatMap { it.archiveFile }
    val launcher = javaToolchains.launcherFor(java.toolchain)
    inputs.file(jarProvider)
    outputs.dir(itchAppImage)

    doLast {
        val staging = itchStaging.get().asFile
        staging.deleteRecursively()
        staging.mkdirs()

        copy {
            from(jarProvider)
            into(staging)
        }
        copy {
            from(layout.projectDirectory.dir("data"))
            into(File(staging, "data"))
            exclude("map/elevation.data")
        }
        copy {
            from(nativesDir.file(nativeLibFileName))
            into(staging)
        }

        val imageDir = itchAppImage.get().asFile
        imageDir.deleteRecursively()
        imageDir.mkdirs()

        val jpackage = launcher.get().metadata.installationPath.file("bin/jpackage").asFile
        providers.exec {
            commandLine(
                jpackage.absolutePath,
                "--type", "app-image",
                "--name", "AloneRL",
                "--app-version", version.toString().substringBefore('-'),
                "--input", staging.absolutePath,
                "--main-jar", jarProvider.get().asFile.name,
                "--dest", imageDir.absolutePath,
                "--java-options", "--enable-native-access=ALL-UNNAMED",
                "--java-options", "-Dtergen.library=\$APPDIR/$nativeLibFileName",
                "--java-options", "-Dalone.data=\$APPDIR/data"
            )
        }.result.get()
    }
}

tasks.register<Zip>("itchPackage") {
    description = "Zips the app image with an itch.io manifest"
    group = "distribution"
    dependsOn(itchAppImageTask)

    archiveFileName = "alone-rl-$version-$platformId.zip"
    destinationDirectory = layout.buildDirectory.dir("itch")

    // Gradle archives don't preserve source permissions by default; without
    // this the launcher and bundled JRE binaries lose their execute bits.
    eachFile {
        if (file.canExecute())
            permissions { unix("rwxr-xr-x") }
    }

    from(itchAppImage)
    from(layout.projectDirectory.file(".itch.toml")) {
        filter { line: String ->
            line.replace("@LAUNCH_PATH@", when {
                osName.contains("win") -> "AloneRL/AloneRL.exe"
                osName.contains("mac") -> "AloneRL.app"
                else -> "AloneRL/bin/AloneRL"
            })
        }
    }
}
