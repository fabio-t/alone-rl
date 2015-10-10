/**
 * Copyright 2015 Fabio Ticconi
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

import com.github.fabioticconi.roguelike.constants.Options;
import com.github.fabioticconi.roguelike.utils.SimplexNoise;

/**
 *
 * @author Fabio Ticconi
 */
public class Map
{
    Cell map[][];

    float heightMap[][];

    public Map()
    {
        // heightMap = SimplexNoise.generateSimplexNoise(Options.MAP_SIZE_X, Options.MAP_SIZE_Y);
        heightMap = SimplexNoise.generateOctavedSimplexNoise(Options.MAP_SIZE_X, Options.MAP_SIZE_Y, 3, 0.4f, 0.005f);

        map = new Cell[Options.MAP_SIZE_X][Options.MAP_SIZE_Y];

        for (int x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (int y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                if (heightMap[x][y] < -0.3)
                {
                    map[x][y] = Cell.DEEP_WATER;
                }
                else if (heightMap[x][y] < -0.2f)
                {
                    map[x][y] = Cell.WATER;
                }
                else if (heightMap[x][y] < 0.3f)
                {
                    map[x][y] = Cell.GRASS;
                }
                else if (heightMap[x][y] < 0.4f)
                {
                    map[x][y] = Cell.HILL;
                }
                else
                {
                    map[x][y] = Cell.MOUNT;
                }
            }
        }
    }

    public boolean isBlockedAt(final int x, final int y)
    {
        return x >= Options.MAP_SIZE_X || x < 0 || y >= Options.MAP_SIZE_Y || y < 0 || map[x][y] == Cell.WALL;
    }

    public Cell get(final int x, final int y)
    {
        if (x < 0 || x >= Options.MAP_SIZE_X || y < 0 || y >= Options.MAP_SIZE_Y)
            return Cell.EMPTY;

        return map[x][y];
    }

    public void set(final int x, final int y, final Cell type)
    {
        if (x < 0 || x >= Options.MAP_SIZE_X || y < 0 || y >= Options.MAP_SIZE_Y)
            return;

        map[x][y] = type;
    }
}
