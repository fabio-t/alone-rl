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
package com.github.fabioticconi.roguelite.map;

import com.github.fabioticconi.roguelite.constants.Options;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Supports storage and retrieval of entities placed on a 2D grid, where each cell can contain multiple
 * entities.
 * <p>
 * Has plenty of support methods for accumulating entities in specific topologies (eg, by
 * moving outward/spiralling from a central point, up to a specified radius).
 *
 * @author Fabio Ticconi
 */
public class MultipleGrid
{
    Long2ObjectMap<IntSet> grid;

    public MultipleGrid()
    {
        grid = new Long2ObjectOpenHashMap<>();
        grid.defaultReturnValue(IntSets.EMPTY_SET);
    }

    /**
     * Returns all entities in the specified cell.
     * <p>
     * NB: the returned set is <b>UNMODIFIABLE</b> to avoid allocating a new set
     * for each call.
     *
     * @param x
     * @param y
     * @return
     */
    public IntSet getEntities(final int x, final int y)
    {
        final long pos = x | ((long) y << 32);

        final IntSet entities = grid.get(pos);

        return IntSets.unmodifiable(entities);
    }

    /**
     * Returns all entities in the specified cell.
     * <p>
     * NB: the returned set is <b>UNMODIFIABLE</b> to avoid allocating a new set
     * for each call.
     *
     * @param pos packed coordinates
     * @return
     */
    public IntSet getEntities(final long pos)
    {
        final IntSet entities = grid.get(pos);

        return IntSets.unmodifiable(entities);
    }

    /**
     * Returns all entities in the specified cells.
     * <p>
     * NB: the returned set is <b>UNMODIFIABLE</b> to avoid allocating a new set
     * for each call.
     *
     * @param cells set of packed coordinates of entities
     * @return
     */
    public IntSet getEntities(final LongSet cells)
    {
        final IntSet entities = new IntOpenHashSet();

        for (final long pos : cells)
        {
            entities.addAll(grid.get(pos));
        }

        return entities;
    }

    /**
     * Moves concentrically from the specified cell, collecting entities. If
     * there are any entities in the same cell, returned those only. If there
     * are any entities in the first ring around the cell, returns those only.
     * It keeps going around until it finds entities or up to maxRadius,
     * whichever comes first.
     *
     * @param x
     * @param y
     * @param maxRadius
     * @return
     */
    public IntSet getClosestEntities(final int x, final int y, int maxRadius)
    {
        IntSet curEntities = grid.get(x | ((long) y << 32));

        if (!curEntities.isEmpty())
            return curEntities;

        final IntSet entities = new IntOpenHashSet();

        // avoid stupid crashes for negative radii
        maxRadius = Math.abs(maxRadius);

        int cur_y = y - 1;
        int cur_x = x;
        for (int d = 1; d <= maxRadius; d++)
        {
            // FIXME what do we do if the north row is "out of bound" already?
            // we should skip the next for and position ourselves immediately to
            // the
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

                curEntities = grid.get(cur_x | ((long) cur_y << 32));

                // accumulate entities within this circle
                entities.addAll(curEntities);
            }

            // continue south, through the east column
            for (; cur_y < max_y; cur_y++)
            {
                if (cur_y < 0 || cur_y >= Options.MAP_SIZE_Y)
                {
                    continue;
                }

                curEntities = grid.get(cur_x | ((long) cur_y << 32));

                // accumulate entities within this circle
                entities.addAll(curEntities);
            }

            // continue west, through the south row
            for (; cur_x > min_x; cur_x--)
            {
                // if we are out of bounds
                if (cur_x < 0 || cur_x >= Options.MAP_SIZE_X)
                {
                    continue;
                }

                curEntities = grid.get(cur_x | ((long) cur_y << 32));

                // accumulate entities within this circle
                entities.addAll(curEntities);
            }

            // continue north, through the west column of this circle
            for (; cur_y >= min_y; cur_y--)
            {
                if (cur_y < 0 || cur_y >= Options.MAP_SIZE_Y)
                {
                    continue;
                }

                curEntities = grid.get(cur_x | ((long) cur_y << 32));

                // accumulate entities within this circle
                entities.addAll(curEntities);
            }

            // if at this round we have found entities, we must stop
            if (!entities.isEmpty())
                return entities;

            // at this point we are positioned WITHIN the north row of the next
            // cycle
        }

        // if we are here, we haven't found any entities so we return the empty
        // set we
        // had created at the beginning

        return entities;
    }

    /**
     * Gets all entities inside the squared-ring at distance r from the
     * specified point.
     *
     * @param x
     * @param y
     * @param r
     * @return
     */
    public IntSet getEntitiesAtRadius(final int x, final int y, final int r)
    {
        final IntSet entities = new IntOpenHashSet();

        // only want the items in the specific cell
        if (r <= 0)
            return grid.get(x | ((long) y << 32));

        IntSet curEntities;

        // we put the cursor where it would have been if we were in one
        // iteration
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

    /**
     * Starting from the specified cell, moves concentrically within the given
     * radius. The returned set is newly allocated and guarantees the entities
     * are ordered by closeness to the starting point.
     *
     * @param x
     * @param y
     * @param r
     * @return
     */
    public IntSet getEntitiesWithinRadius(final int x, final int y, final int r)
    {
        final IntSet entities = new IntLinkedOpenHashSet();

        IntSet curEntities = grid.getOrDefault(x | ((long) y << 32), null);

        if (curEntities != null)
        {
            entities.addAll(curEntities);
        }

        int cur_y = y - 1;
        int cur_x = x;
        for (int d = 1; d <= r; d++)
        {
            // FIXME what do we do if the north row is "out of bound" already?
            // we should skip the next for and position ourselves immediately to
            // the
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

            // at this point we are positioned WITHIN the north row of the next
            // cicle
        }

        return entities;
    }

    /**
     * Puts the entity at the specified coordinates. Doesn't do anything if the
     * entity is already there.
     *
     * @param id
     * @param x
     * @param y
     */
    public void putEntity(final int id, final int x, final int y)
    {
        final long pos = x | ((long) y << 32);

        IntSet entities = grid.getOrDefault(pos, null);

        if (entities == null)
        {
            entities = new IntOpenHashSet();

            entities.add(id);

            grid.put(pos, entities);
        }
        else if (!entities.rem(id))
        {
            // TODO: log if it was already present at this position?

            entities.add(id);
        }
    }

    /**
     * Removes the specified entity from one cell and puts it into another.
     * Doesn't do anything if the the entity is NOT in the start cell. Equally,
     * there isn't any change if the entity is already in the end cell.
     *
     * @param id
     * @param start_x
     * @param start_y
     * @param end_x
     * @param end_y
     * @return
     */
    public boolean moveEntity(final int id, final int start_x, final int start_y, final int end_x, final int end_y)
    {
        final long pos = start_x | ((long) start_y << 32);

        final IntSet entities = grid.getOrDefault(pos, null);

        // TODO: just put the object at position "end"? (should we check there,
        // too?)
        if (entities == null || entities.isEmpty())
            return false;

        final boolean found = entities.remove(id);

        if (found)
        {
            putEntity(id, end_x, end_y);

            return true;
        }

        return false;
    }

    /**
     * Returns the number of entities present at the specified position.
     *
     * @param x
     * @param y
     * @return
     */
    public int count(final int x, final int y)
    {
        final long pos = x | ((long) y << 32);

        final IntSet entities = grid.getOrDefault(pos, null);

        if (entities == null)
            return 0;

        return entities.size();
    }

    /**
     * Returns the number of entities present at the specified position.
     *
     * @param pos
     * @return
     */
    public int count(final long pos)
    {
        final IntSet entities = grid.getOrDefault(pos, null);

        if (entities == null)
            return 0;

        return entities.size();
    }
}