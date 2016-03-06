/**
 * Copyright 2016 Fabio Ticconi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fabioticconi.roguelike.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 *
 * @author Fabio Ticconi
 */
public class EntityGridTest
{
    @Test
    public final void test()
    {
        final EntityGrid grid = new EntityGrid();

        grid.putEntity(1, 5, 10);
        grid.putEntity(2, 10, 5);
        grid.putEntity(3, 100, 500);

        List<Integer> e1 = grid.getEntities(5, 10);
        List<Integer> e2 = grid.getEntities(10, 5);
        List<Integer> e3 = grid.getEntities(100, 500);
        List<Integer> e4 = grid.getEntities(1, 1);

        assertNotNull(e1);
        assertNotNull(e2);
        assertNotNull(e3);
        assertNull(e4);

        assertTrue(grid.moveEntity(1, 5, 10, 10, 5));
        assertTrue(grid.moveEntity(3, 100, 500, 1, 1));

        e1 = grid.getEntities(5, 10);
        e2 = grid.getEntities(10, 5);
        e3 = grid.getEntities(100, 500);
        e4 = grid.getEntities(1, 1);

        assertNotNull(e1);
        assertNotNull(e2);
        assertNotNull(e3);
        assertNotNull(e4);

        assertTrue(e1.isEmpty());
        assertEquals(e2.size(), 2);
        assertTrue(e3.isEmpty());
        assertEquals(e4.size(), 1);
    }

}
