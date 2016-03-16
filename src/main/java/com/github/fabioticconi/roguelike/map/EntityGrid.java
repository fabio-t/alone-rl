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

import java.util.List;

import com.github.fabioticconi.roguelike.constants.Options;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 *
 * @author Fabio Ticconi
 */
public class EntityGrid
{
    Long2ObjectMap<IntList> grid;

    int                     i = 0;

    public EntityGrid()
    {
        grid = new Long2ObjectOpenHashMap<IntList>();
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

        final List<Integer> entities = new IntArrayList();

        // avoid stupid crashes for negative radii
        maxRadius = Math.abs(maxRadius);

        int cur_y = y - 1;
        int cur_x = x;
        for (int d = 1; d <= maxRadius; d++)
        {
            // FIXME what do we do if the north row is "out of bound" already?
            // we should skip the next for and position ourselves immediately to the
            // correct east-side column, at the same y position as we are

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
            for (; cur_y >= min_y; cur_y--)
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
        final IntList entities = new IntArrayList();

        if (r <= 0)
            return grid.getOrDefault(x | ((long) y << 32), entities);

        List<Integer> curEntities;

        // we put the cursor where it would have been if we were in one iteration
        // of "getClosestEntities"
        int cur_y = y - r;
        int cur_x = x - r + 1;

        // FIXME what do we do if the north row is "out of bound" already?
        // we should skip the next for and position ourselves immediately to the
        // correct east-side column, at the same y position as we are

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
        for (; cur_y >= min_y; cur_y--)
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
        final List<Integer> entities = new IntArrayList();

        List<Integer> curEntities = grid.getOrDefault(x | ((long) y << 32), null);

        if (curEntities != null)
        {
            entities.addAll(curEntities);
        }

        int cur_y = y - 1;
        int cur_x = x;
        for (int d = 1; d <= r; d++)
        {
            // FIXME what do we do if the north row is "out of bound" already?
            // we should skip the next for and position ourselves immediately to the
            // correct east-side column, at the same y position as we are

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

                if (curEntities == entities)
                {
                    System.exit(0);
                }

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
            for (; cur_y >= min_y; cur_y--)
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

            // at this point we are positioned WITHIN the north row of the next cicle
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

        IntList entities = grid.getOrDefault(pos, null);

        final Integer idVal = Integer.valueOf(id);

        if (entities == null)
        {
            entities = new IntArrayList();

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
        final long pos = start_x | ((long) start_y << 32);

        final IntList entities = grid.getOrDefault(pos, null);

        final Integer idVal = Integer.valueOf(id);

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
