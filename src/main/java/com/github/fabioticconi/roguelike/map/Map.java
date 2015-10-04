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

/**
 *
 * @author Fabio Ticconi
 */
public class Map
{
    Cell map[][];

    public final int maxX;
    public final int maxY;

    public Map(final int max_x, final int max_y)
    {
        maxX = max_x;
        maxY = max_y;

        map = new Cell[max_x][max_y];

        for (int x = 0; x < max_x; x++)
        {
            for (int y = 0; y < max_y; y++)
            {
                map[x][y] = Cell.GROUND;
            }
        }
    }

    public boolean isBlockedAt(final int x, final int y)
    {
        return x >= maxX || x < 0 || y >= maxY || y < 0 || map[x][y] == Cell.WALL;
    }

    public Cell get(final int x, final int y)
    {
        if (x < 0 || x >= maxX || y < 0 || y >= maxY)
            return Cell.EMPTY;

        return map[x][y];
    }

    public void set(final int x, final int y, final Cell type)
    {
        if (x < 0 || x >= maxX || y < 0 || y >= maxY)
            return;

        map[x][y] = type;
    }
}
