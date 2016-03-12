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

import com.github.fabioticconi.roguelike.constants.Options;

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

    public List<Integer> getClosestEntities(final int x, final int y, int maxRadius)
    {
        List<Integer> curEntities = grid.getOrDefault(x | ((long) y << 32), null);

        if (curEntities != null)
            return curEntities;

        final List<Integer> entities = new LinkedList<Integer>();

        // avoid stupid crashes for negative radii
        maxRadius = Math.abs(maxRadius);

        int cur_y = y;
        int cur_x = x;
        for (int d = 1; d < maxRadius; d++)
        {
            // FIXME what do we do if the north row is "out of bound" already?
            // we should skip the next for and position ourselves immediately to the
            // correct east-side column, at the same y position as we are

            // go one step north, into the upper row of the new circle
            cur_y--;

            final int max_x = x + d;
            final int max_y = y + d;
            final int min_x = x - d;
            final int min_y = y - d;

            // continue east, through the north row
            for (; cur_x < max_x; cur_x++)
            {
                // if we are out of bounds
                if (cur_x < 0 || cur_x >= Options.MAP_SIZE_X)
                {
                    continue;
                }

                curEntities = grid.getOrDefault(cur_x | ((long) cur_y << 32), null);

                if (curEntities != null)
                {
                    // accumulate entities within this circle
                    entities.addAll(curEntities);
                }
            }

            // continue south, through the east column
            for (; cur_y < max_y; cur_y++)
            {
                if (cur_y < 0 || cur_y >= Options.MAP_SIZE_Y)
                {
                    continue;
                }

                curEntities = grid.getOrDefault(cur_x | ((long) cur_y << 32), null);

                if (curEntities != null)
                {
                    // accumulate entities within this circle
                    entities.addAll(curEntities);
                }
            }

            // continue west, through the south row
            for (; cur_x > min_x; cur_x--)
            {
                // if we are out of bounds
                if (cur_x < 0 || cur_x >= Options.MAP_SIZE_X)
                {
                    continue;
                }

                curEntities = grid.getOrDefault(cur_x | ((long) cur_y << 32), null);

                if (curEntities != null)
                {
                    // accumulate entities within this circle
                    entities.addAll(curEntities);
                }
            }

            // continue north, through the west column of this circle
            for (; cur_y > min_y; cur_y--)
            {
                if (cur_y < 0 || cur_y >= Options.MAP_SIZE_Y)
                {
                    continue;
                }

                curEntities = grid.getOrDefault(cur_x | ((long) cur_y << 32), null);

                if (curEntities != null)
                {
                    // accumulate entities within this circle
                    entities.addAll(curEntities);
                }
            }

            // if at this round we have found entities, we must stop
            if (!entities.isEmpty())
                return entities;

            // at this point we are positioned WITHIN the north row of the next cicle
        }

        // if we are here, we haven't found any entities so we return the empty set we
        // had created at the beginning

        return entities;
    }

    public List<Integer> getEntitiesAtRadius(final int x, final int y, final int r)
    {
        if (r <= 0)
            return grid.getOrDefault(x | ((long) y << 32), new LinkedList<Integer>());

        final List<Integer> entities = new LinkedList<Integer>();
        List<Integer> curEntities;

        int cur_y = y;
        int cur_x = x;

        // FIXME what do we do if the north row is "out of bound" already?
        // we should skip the next for and position ourselves immediately to the
        // correct east-side column, at the same y position as we are

        // go one step north, into the upper row of the new circle
        cur_y--;

        final int max_x = x + r;
        final int max_y = y + r;
        final int min_x = x - r;
        final int min_y = y - r;

        // continue east, through the north row
        for (; cur_x < max_x; cur_x++)
        {
            // if we are out of bounds
            if (cur_x < 0 || cur_x >= Options.MAP_SIZE_X)
            {
                continue;
            }

            curEntities = grid.getOrDefault(cur_x | ((long) cur_y << 32), null);

            if (curEntities != null)
            {
                // accumulate entities within this circle
                entities.addAll(curEntities);
            }
        }

        // continue south, through the east column
        for (; cur_y < max_y; cur_y++)
        {
            if (cur_y < 0 || cur_y >= Options.MAP_SIZE_Y)
            {
                continue;
            }

            curEntities = grid.getOrDefault(cur_x | ((long) cur_y << 32), null);

            if (curEntities != null)
            {
                // accumulate entities within this circle
                entities.addAll(curEntities);
            }
        }

        // continue west, through the south row
        for (; cur_x > min_x; cur_x--)
        {
            // if we are out of bounds
            if (cur_x < 0 || cur_x >= Options.MAP_SIZE_X)
            {
                continue;
            }

            curEntities = grid.getOrDefault(cur_x | ((long) cur_y << 32), null);

            if (curEntities != null)
            {
                // accumulate entities within this circle
                entities.addAll(curEntities);
            }
        }

        // continue north, through the west column of this circle
        for (; cur_y > min_y; cur_y--)
        {
            if (cur_y < 0 || cur_y >= Options.MAP_SIZE_Y)
            {
                continue;
            }

            curEntities = grid.getOrDefault(cur_x | ((long) cur_y << 32), null);

            if (curEntities != null)
            {
                // accumulate entities within this circle
                entities.addAll(curEntities);
            }
        }

        return entities;
    }

    public List<Integer> getEntitiesWithinRadius(final int x, final int y, final int r)
    {
        final List<Integer> entities = new LinkedList<Integer>();
        List<Integer> curEntities;

        final int min_x = x - r < 0 ? 0 : x - r;
        final int min_y = y - r < 0 ? 0 : y - r;
        final int max_x = x + r > Options.MAP_SIZE_X ? Options.MAP_SIZE_X : x + r;
        final int max_y = y + r > Options.MAP_SIZE_Y ? Options.MAP_SIZE_Y : y + r;

        for (int p_x = min_x; p_x < max_x; p_x++)
        {
            for (int p_y = min_y; p_y < max_y; p_y++)
            {
                curEntities = grid.getOrDefault(p_x | ((long) p_y << 32), null);

                if (curEntities != null)
                {
                    entities.addAll(curEntities);
                }
            }
        }

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
