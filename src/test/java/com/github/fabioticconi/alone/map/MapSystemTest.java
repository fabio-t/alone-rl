/*
 * Copyright (C) 2015-2017 Fabio Ticconi
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

package com.github.fabioticconi.alone.map;

import com.github.fabioticconi.alone.systems.MapSystem;
import com.github.fabioticconi.alone.utils.Coords;
import com.github.fabioticconi.alone.utils.LongBag;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class MapSystemTest
{
    MapSystem map;

    @Before
    public void setup() throws IOException
    {
        map = new MapSystem();
    }

    @Test
    public void testGetFirstFreeExit() throws Exception
    {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetFreeExitRandomised() throws Exception
    {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetFirstOfType() throws Exception
    {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testBasics() throws Exception
    {
        // test get, set, contains, isObstacle
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetVisibleCells() throws Exception
    {
        final LongBag set = map.getVisibleCells(100, 100, 10);

        int[] coords;
        for (final long key : set.getData())
        {
            coords = Coords.unpackCoords(key);
            System.out.println(
                "(" + coords[0] + ", " + coords[1] + ") --> " + Coords.distanceBlock(100, 100, coords[0], coords[1]) +
                ", " + Coords.distanceChebyshev(100, 100, coords[0], coords[1]) + ", " +
                Coords.distancePseudoEuclidean(100, 100, coords[0], coords[1]));
        }

        int count = 0;
        for (final long key : set.getData())
        {
            coords = Coords.unpackCoords(key);

            if (Coords.distancePseudoEuclidean(100, 100, coords[0], coords[1]) > 11f)
            {
                count++;
            }
        }

        System.out.println("coords: tot=" + set.size() + ", >10=" + count);
    }

    @Test
    public void testGetLineOfSight() throws Exception
    {
        throw new RuntimeException("not yet implemented");
    }

}
