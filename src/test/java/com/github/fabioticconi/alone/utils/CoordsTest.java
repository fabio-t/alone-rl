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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoordsTest
{
    @Test
    public void testDistances()
    {
        assertEquals(0f, Coords.distanceEuclidean(5, 5, 5, 5));
        assertEquals(5f, Coords.distanceEuclidean(0, 0, 3, 4));

        assertEquals(0, Coords.distanceBlock(5, 5, 5, 5));
        assertEquals(7, Coords.distanceBlock(0, 0, 3, 4));

        assertEquals(0, Coords.distanceChebyshev(5, 5, 5, 5));
        assertEquals(4, Coords.distanceChebyshev(0, 0, 3, 4));

        // floor of the euclidean distance
        assertEquals(1f, Coords.distancePseudoEuclidean(0, 0, 1, 1));
        assertEquals(5f, Coords.distancePseudoEuclidean(0, 0, 3, 4));
    }

    @Test
    public void testPackUnpackCoords()
    {
        final int[] x = { 0, 100, 1000, Integer.MAX_VALUE - 1, Integer.MAX_VALUE };
        final int[] y = { 0, 100, 1000, Integer.MAX_VALUE - 1, Integer.MAX_VALUE };

        for (int i = 0; i < x.length; i++)
        {
            final long key = Coords.packCoords(x[i], y[i]);

            final int[] coords = Coords.unpackCoords(key);

            assertEquals(x[i], coords[0]);
            assertEquals(y[i], coords[1]);
        }
    }

    @Test
    public void testUnpackIntoArray()
    {
        final long key = Coords.packCoords(123, 456);

        final int[] coords = new int[2];
        Coords.unpackCoords(key, coords);

        assertEquals(123, coords[0]);
        assertEquals(456, coords[1]);
    }
}
