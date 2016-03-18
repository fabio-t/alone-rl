/**
 * Copyright 2015 Fabio Ticconi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.fabioticconi.roguelike.map;

import com.github.fabioticconi.roguelike.constants.Cell;
import com.github.fabioticconi.roguelike.constants.Options;
import com.github.fabioticconi.roguelike.constants.Side;
import com.github.fabioticconi.terrain_generator.ImageWriter;
import com.github.fabioticconi.terrain_generator.SimplexNoise;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import rlforj.los.ILosBoard;

/**
 *
 * @author Fabio Ticconi
 */
public class Map implements ILosBoard
{
    final Cell    map[][];

    final LongSet lastVisited;

    final float   heightMap[][];

    // TODO: when the terrain-generator is done, we'd have not only
    // the height map but also the humidity map and the temperature map,
    // as well as, of course, the "biome map" containing the actual color
    // codes.

    public Map()
    {
        lastVisited = new LongOpenHashSet();

        heightMap = SimplexNoise.generateOctavedSimplexNoise(Options.MAP_SIZE_X, Options.MAP_SIZE_Y, 6, 0.4f, 0.003f);
        final ImageWriter img = new ImageWriter(Options.MAP_SIZE_X, Options.MAP_SIZE_Y, false);
        img.savePng("map.png", heightMap);

        map = new Cell[Options.MAP_SIZE_X][Options.MAP_SIZE_Y];

        float value;

        for (int x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (int y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                // value = heightMap[x][y];
                value = 0.5f * (1 + heightMap[x][y]);

                // System.out.println(value);

                final float water = 0.3f;

                if (value < water)
                {
                    if (value < water * 0.7f)
                    {
                        map[x][y] = Cell.DEEP_WATER;
                    } else
                    {
                        map[x][y] = Cell.WATER;
                    }
                } else
                {
                    // final float val = t;
                    // normalize val so 0 is at water level
                    final float val = (value - water) / (1.0f - water);

                    // set color based on above the see level
                    // beach, plain, forest, mountains etc

                    if (val < 0.1f)
                    {
                        map[x][y] = Cell.SAND;
                    } else if (val < 0.3f)
                    {
                        map[x][y] = Cell.GRASS;
                    } else if (val < 0.55f)
                    {
                        map[x][y] = Cell.HILL;
                    } else if (val < 0.7f)
                    {
                        map[x][y] = Cell.MOUNTAIN;
                    } else
                    {
                        map[x][y] = Cell.HIGH_MOUNTAIN;
                    }
                }
            }
        }
    }

    /**
     * Take a position and returns the first free exit (if any), starting from
     * North and going clockwise.
     *
     * @param x
     * @param y
     * @return An exit, or HERE if none is available
     */
    public Side getFirstFreeExit(final int x, final int y)
    {
        int side_x = Side.N.x + x;
        int side_y = Side.N.y + y;

        if (!isObstacle(side_x, side_y))
            return Side.N;

        side_x = Side.E.x + x;
        side_y = Side.E.y + y;

        if (!isObstacle(side_x, side_y))
            return Side.E;

        side_x = Side.S.x + x;
        side_y = Side.S.y + y;

        if (!isObstacle(side_x, side_y))
            return Side.S;

        side_x = Side.W.x + x;
        side_y = Side.W.y + y;

        if (!isObstacle(side_x, side_y))
            return Side.W;

        return Side.HERE;
    }

    /**
     * Takes a position and returns a free exit (if available), with some
     * randomisation (not guaranteed).
     *
     * @param x
     * @param y
     * @return An exit, or HERE if none is available
     */
    public Side getFreeExitRandomised(final int x, final int y)
    {
        final Side firstFree = getFirstFreeExit(x, y);

        if (firstFree == Side.HERE)
            return firstFree;

        final Side random = Side.getRandom();

        if (isObstacle(x, y, random))
            return firstFree;
        else
            return random;
    }

    public Cell get(final int x, final int y)
    {
        if (contains(x, y))
            return map[x][y];

        return Cell.EMPTY;
    }

    public void set(final int x, final int y, final Cell type)
    {
        if (contains(x, y))
        {
            map[x][y] = type;
        }
    }

    public int distance(final int x1, final int y1, final int x2, final int y2)
    {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /*
     * (non-Javadoc)
     *
     * @see rlforj.los.ILosBoard#contains(int, int)
     */
    @Override
    public boolean contains(final int x, final int y)
    {
        return x >= 0 && x < Options.MAP_SIZE_X && y >= 0 && y < Options.MAP_SIZE_Y;
    }

    /*
     * (non-Javadoc)
     *
     * @see rlforj.los.ILosBoard#isObstacle(int, int)
     */
    @Override
    public boolean isObstacle(final int x, final int y)
    {
        return x >= Options.MAP_SIZE_X
                || x < 0
                || y >= Options.MAP_SIZE_Y
                || y < 0
                || map[x][y] == Cell.WALL
                || map[x][y] == Cell.CLOSED_DOOR;
    }

    public boolean isObstacle(final int x, final int y, final Side direction)
    {
        return isObstacle(x + direction.x, y + direction.y);
    }

    /*
     * (non-Javadoc)
     *
     * @see rlforj.los.ILosBoard#visit(int, int)
     */
    @Override
    public void visit(final int x, final int y)
    {
        lastVisited.add(x | ((long) y << 32));
    }

    public void clearLastVisited()
    {
        lastVisited.clear();
    }

    public LongSet getLastVisited()
    {
        return LongSets.unmodifiable(lastVisited);
    }
}
