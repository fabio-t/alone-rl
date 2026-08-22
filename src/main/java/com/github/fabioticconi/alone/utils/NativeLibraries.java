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
package com.github.fabioticconi.alone.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Locates the terrain-generator native library before the binding loads it.
 *
 * <p>The vendored {@code com.github.fabioticconi.tergen} binding loads the
 * shared library from the {@code tergen.library} system property, falling
 * back to the platform's library search path. In a source checkout the
 * library sits under {@code natives/<os>-<arch>/} (see the {@code nativeLib}
 * Gradle task); a packaged install sets {@code tergen.library} itself.
 *
 * @author Fabio Ticconi
 */
public final class NativeLibraries
{
    private NativeLibraries()
    {
    }

    /**
     * Points {@code tergen.library} at the bundled terrain-generator library,
     * if the property isn't set already and a bundled copy can be found.
     * Must run before any {@code tergen} class is loaded.
     */
    public static void locateTerrainGenerator()
    {
        if (System.getProperty("tergen.library") != null)
            return;

        final String os   = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        final String arch = switch (System.getProperty("os.arch"))
        {
            case "amd64", "x86_64" -> "x86_64";
            case "aarch64", "arm64" -> "aarch64";
            case String other -> other;
        };

        final String platform;
        final String fileName;
        if (os.contains("win"))
        {
            platform = "windows-" + arch;
            fileName = "terrain_generator_ffi.dll";
        }
        else if (os.contains("mac"))
        {
            platform = "macos-" + arch;
            fileName = "libterrain_generator_ffi.dylib";
        }
        else
        {
            platform = "linux-" + arch;
            fileName = "libterrain_generator_ffi.so";
        }

        final Path candidate = Path.of("natives", platform, fileName);

        if (Files.exists(candidate))
            System.setProperty("tergen.library", candidate.toAbsolutePath().toString());

        // otherwise leave it to the binding's fallback (system library path)
    }
}
