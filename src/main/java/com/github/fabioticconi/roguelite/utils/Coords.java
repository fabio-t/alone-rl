/*
 * Copyright (C) 2017 Fabio Ticconi
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

package com.github.fabioticconi.roguelite.utils;

import com.github.fabioticconi.roguelite.constants.Options;

public class Coords
{
    public static float distanceEuclidean(final int x1, final int y1, final int x2, final int y2)
    {
        final float xdiff = x1 - x2;
        final float ydiff = y1 - y2;

        return (float) Math.sqrt(xdiff * xdiff + ydiff * ydiff);
    }

    public static float distancePseudoEuclidean(final int x1, final int y1, final int x2, final int y2)
    {
        final float xdiff = x1 - x2;
        final float ydiff = y1 - y2;

        return (float) Math.floor(Math.sqrt(xdiff * xdiff + ydiff * ydiff));
    }

    public static int distanceBlock(final int x1, final int y1, final int x2, final int y2)
    {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public static int distanceChebyshev(final int x1, final int y1, final int x2, final int y2)
    {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    public static void visitVonNeumannNeighbours(final int x, final int y, final int r)
    {
        if (r == 0)
        {
            System.out.println("HERE");
            return;
        }

        final int xmin = Math.max(x - r, 0);
        final int xmax = Math.min(x + r, Options.MAP_SIZE_X - 1);

        int i = -r - Math.min(x - r, 0);
        for (int x_t = xmin; x_t <= xmax; x_t++, i++)
        {
            final int z = r - Math.abs(i);

            final int y1 = y + z;

            if (y1 < Options.MAP_SIZE_Y)
            {
                System.out.println("i=" + i + ", x=" + x_t + ", y=" + y1 + ", " + distanceBlock(x, y, x_t, y1));
            }

            final int y2 = y - z;

            if (y1 == y2 || y2 < 0)
            {
                continue;
            }

            System.out.println("i=" + i + ", x=" + x_t + ", y=" + y2 + ", " + distanceBlock(x, y, x_t, y2));
        }
    }

    public static void visitMooreNeighbours(final int x, final int y, final int r)
    {

    }

    public static long packCoords(final int x, final int y)
    {
        return x | ((long) y << 32);
    }

    public static int[] unpackCoords(final long key)
    {
        final int[] coords = new int[2];

        coords[0] = (int) key;
        coords[1] = (int) (key >> 32);

        return coords;
    }
}
