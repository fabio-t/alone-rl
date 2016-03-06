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
import com.github.fabioticconi.terrain_generator.ImageWriter;
import com.github.fabioticconi.terrain_generator.SimplexNoise;

/**
 *
 * @author Fabio Ticconi
 */
public class Map
{
    Cell  map[][];

    float heightMap[][];

    // These two will have to be filled out with a different noise map
    // then the heightmap (probability with smaller scale and roughness, so as
    // to have big stable "patches" and only rarely some smaller patch.
    // Both temperature and humidity will be used
    float temperature[][];
    float humidity[][];

    public Map()
    {
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
                    }
                    else
                    {
                        map[x][y] = Cell.WATER;
                    }
                }
                else
                {
                    // final float val = t;
                    // normalize val so 0 is at water level
                    final float val = (value - water) / (1.0f - water);

                    // set color based on above the see level
                    // beach, plain, forest, mountains etc

                    if (val < 0.1f)
                    {
                        map[x][y] = Cell.SAND;
                    }
                    else if (val < 0.3f)
                    {
                        map[x][y] = Cell.GRASS;
                    }
                    else if (val < 0.55f)
                    {
                        map[x][y] = Cell.HILL;
                    }
                    else if (val < 0.7f)
                    {
                        map[x][y] = Cell.MOUNTAIN;
                    }
                    else
                    {
                        map[x][y] = Cell.HIGH_MOUNTAIN;
                    }
                }
            }
        }
    }

    // public Color getColor(float t)
    // {
    // t = (1f + t) / 2f;
    // // value = 0.5f + value;
    //
    // if (t > 1f)
    // {
    // // System.out.println("error: " + value);
    // t = 1f;
    // }
    // if (t < 0f)
    // {
    // // System.out.println("error: " + value);
    // t = 0f;
    // }
    //
    // final float water = 0.3f;
    //
    // if (t < water)
    // {
    // if (t < water * 0.7f)
    // return new Color(0.2f, 0.5f, 0.9f);
    // else
    // return new Color(0.4f, 0.7f, 1f);
    // }
    // else
    // {
    // // final float val = t;
    // // normalize val so 0 is at water level
    // final float val = (t - water) / (1.0f - water);
    //
    // // set color based on above the see level
    // // beach, plain, forest, mountains etc
    //
    // if (val < 0.1f)
    // return Color.YELLOW;
    // else if (val < 0.3f)
    // return Color.GREEN;
    // else if (val < 0.55f)
    // return new Color(.1f, 0.8f, .2f);
    // else if (val < 0.7f)
    // return Color.GRAY;
    // else
    // return Color.WHITE;
    // }
    // }

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
