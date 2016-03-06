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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Fabio Ticconi
 */
public class EntityGrid
{
    HashMap<Long, List<Integer>> grid;

    public EntityGrid()
    {
        grid = new HashMap<Long, List<Integer>>();
    }

    public List<Integer> getEntities(final int x, final int y)
    {
        final long pos = x | ((long) y << 32);

        final List<Integer> entities = grid.getOrDefault(pos, null);

        return entities;
    }

    public int getFirstEntity(final int x, final int y)
    {
        final long pos = x | ((long) y << 32);

        final List<Integer> entities = grid.getOrDefault(pos, null);

        if (entities == null || entities.isEmpty())
            return 0;

        return entities.get(0);
    }

    public void putEntity(final int id, final int x, final int y)
    {
        final long pos = x | ((long) y << 32);

        List<Integer> entities = grid.getOrDefault(pos, null);

        final Integer idVal = Integer.valueOf(id);

        if (entities == null)
        {
            entities = new LinkedList<Integer>();

            entities.add(idVal);

            grid.put(pos, entities);
        }
        else if (!entities.remove(idVal))
        {
            // TODO: log if it was already present at this position?

            entities.add(idVal);
        }
    }

    public boolean moveEntity(final int id, final int start_x, final int start_y, final int end_x, final int end_y)
    {
        final Integer idVal = Integer.valueOf(id);

        final List<Integer> entities = getEntities(start_x, start_y);

        // TODO: just put the object at position "end" (should we check there, too?)
        if (entities == null)
            return false;

        final boolean found = entities.remove(idVal);

        if (found)
        {
            putEntity(id, end_x, end_y);

            return true;
        }

        return false;
    }
}
