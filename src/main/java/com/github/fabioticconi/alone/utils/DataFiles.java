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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves the game's data directory, so the game works both from a source
 * checkout (data/ in the working directory) and from a packaged install
 * (data/ next to the application, pointed at with -Dalone.data).
 *
 * @author Fabio Ticconi
 */
public final class DataFiles
{
    private static final Path ROOT = resolveRoot();

    private DataFiles()
    {
    }

    private static Path resolveRoot()
    {
        final String override = System.getProperty("alone.data");

        if (override != null && !override.isBlank())
            return Path.of(override);

        return Path.of("data");
    }

    /**
     * The resolved data directory.
     */
    public static Path root()
    {
        return ROOT;
    }

    public static InputStream read(final String relative) throws IOException
    {
        return Files.newInputStream(ROOT.resolve(relative));
    }

    public static OutputStream write(final String relative) throws IOException
    {
        final Path target = ROOT.resolve(relative);

        Files.createDirectories(target.getParent());

        return Files.newOutputStream(target);
    }

    public static boolean exists(final String relative)
    {
        return Files.exists(ROOT.resolve(relative));
    }
}
