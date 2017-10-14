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
package com.github.fabioticconi.alone.map;

import it.unimi.dsi.fastutil.ints.IntSets;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Fabio Ticconi
 */
public class MultipleGridTest
{
    @Test
    public final void testGetAndMoveEntities()
    {
        final MultipleGrid grid = new MultipleGrid();

        grid.add(1, 5, 10);
        grid.add(2, 10, 5);
        grid.add(3, 100, 500);

        Set<Integer> e1 = grid.get(5, 10);
        Set<Integer> e2 = grid.get(10, 5);
        Set<Integer> e3 = grid.get(100, 500);
        Set<Integer> e4 = grid.get(1, 1);

        assertNotNull(e1);
        assertNotNull(e2);
        assertNotNull(e3);
        assertNotNull(e4);

        assertEquals(e4, IntSets.EMPTY_SET);

        assertTrue(grid.move(1, 5, 10, 10, 5));
        assertTrue(grid.move(3, 100, 500, 1, 1));

        e1 = grid.get(5, 10);
        e2 = grid.get(10, 5);
        e3 = grid.get(100, 500);
        e4 = grid.get(1, 1);

        assertNotNull(e1);
        assertNotNull(e2);
        assertNotNull(e3);
        assertNotNull(e4);

        assertTrue(e1.isEmpty());
        assertEquals(e2.size(), 2);
        assertTrue(e3.isEmpty());
        assertEquals(e4.size(), 1);
    }

    @Test
    public void testGetClosestEntities() throws Exception
    {
        final MultipleGrid grid = new MultipleGrid();

        for (int x = 10, i = 1; x < 17; x++)
        {
            for (int y = 10; y < 17; y++, i++)
            {
                if (x == 13 && y == 13)
                {
                    continue;
                }

                grid.add(i, x, y);
            }
        }

        Set<Integer> entities = grid.getClosest(13, 13, 0);

        assertNotNull(entities);
        assertTrue(entities.isEmpty());

        entities = grid.getClosest(13, 13, 1);

        assertNotNull(entities);
        assertEquals(entities.size(), 8);

        entities = grid.getClosest(13, 13, 2);

        assertNotNull(entities);
        assertEquals(entities.size(), 8);

        entities = grid.getClosest(13, 13, 3);

        assertNotNull(entities);
        assertEquals(entities.size(), 8);
    }

    @Test
    public void testGetEntitiesAtRadius() throws Exception
    {
        final MultipleGrid grid = new MultipleGrid();

        for (int x = 10, i = 1; x < 17; x++)
        {
            for (int y = 10; y < 17; y++, i++)
            {
                if (x == 13 && y == 13)
                {
                    continue;
                }

                grid.add(i, x, y);
            }
        }

        Set<Integer> entities = grid.getAtRadius(13, 13, 0);

        assertNotNull(entities);
        assertTrue(entities.isEmpty());

        entities = grid.getAtRadius(13, 13, 1);

        System.out.println();

        assertNotNull(entities);
        assertEquals(entities.size(), 8);

        entities = grid.getAtRadius(13, 13, 2);

        System.out.println();

        assertNotNull(entities);
        assertEquals(entities.size(), 16);

        entities = grid.getAtRadius(13, 13, 3);

        assertNotNull(entities);
        assertEquals(entities.size(), 24);
    }

    @Test
    public void testGetEntitiesWithinRadius() throws Exception
    {
        final MultipleGrid grid = new MultipleGrid();

        int i = 1;
        for (int x = 10; x < 17; x++)
        {
            for (int y = 10; y < 17; y++, i++)
            {
                if (x == 13 && y == 13)
                {
                    continue;
                }

                grid.add(i, x, y);
            }
        }

        Set<Integer> entities = grid.getWithinRadius(13, 13, 0);

        assertNotNull(entities);
        assertTrue(entities.isEmpty());

        entities = grid.getWithinRadius(13, 13, 1);

        assertNotNull(entities);
        assertEquals(entities.size(), 8);

        entities = grid.getWithinRadius(13, 13, 2);

        assertNotNull(entities);
        assertEquals(entities.size(), 24);

        entities = grid.getWithinRadius(13, 13, 3);

        assertNotNull(entities);
        assertEquals(entities.size(), 48);

        entities = grid.getWithinRadius(13, 13, 3);
    }
}
