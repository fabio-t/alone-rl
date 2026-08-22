# Vendored terrain-generator Java binding

The sources in this package are vendored, unmodified, from
[fabio-t/terrain-generator](https://github.com/fabio-t/terrain-generator)
at tag `v0.2.0` (`java/com/github/fabioticconi/tergen/`), where they are
licensed under the **Apache License 2.0** (see `THIRD-PARTY.md` in the
repository root).

They bind the terrain generator's native library (`terrain_generator_ffi`,
written in Rust) through the Java Foreign Function & Memory API. The
native library itself is built by the `nativeLib` Gradle task and is
looked up at runtime via the `tergen.library` system property (see
`com.github.fabioticconi.alone.utils.NativeLibraries`).

To update: bump `tergenTag` in `build.gradle.kts` and re-copy these files
from the same tag, so the binding and the native library never drift apart.
