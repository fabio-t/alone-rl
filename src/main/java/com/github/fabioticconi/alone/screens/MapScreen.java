/*
 * Copyright (C) 2015-2026 Fabio Ticconi
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

package com.github.fabioticconi.alone.screens;

import asciiPanel.AsciiPanel;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.constants.Options;
import com.github.fabioticconi.alone.systems.MapSystem;
import com.github.fabioticconi.tergen.Layer;
import com.github.fabioticconi.tergen.Terrain;
import com.github.fabioticconi.tergen.TerrainConfig;
import com.github.fabioticconi.tergen.TerrainGenerator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: Fabio Ticconi
 * Date: 21/11/17
 */
public class MapScreen extends AbstractScreen
{
    /**
     * The terrain.yml threshold below which a cell is water: the generator's
     * sea level is aligned to it, and rivers and lakes are carved down to it
     * so that the height-to-cell mapping shows them as water.
     */
    private static final float WATER_HEIGHT = 0.05f;

    /** Row-major elevation, indexed [y * MAP_SIZE_X + x]. */
    float[] heightmap;

    @Override
    protected void initialize()
    {
        try
        {
            map.loadTemplates();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        regenerate();
    }

    void regenerate()
    {
        final int seed = (int) (System.currentTimeMillis() / 1000);

        final TerrainConfig config = TerrainConfig.defaults()
                                                  .size(Options.MAP_SIZE_X, Options.MAP_SIZE_Y)
                                                  .seed(seed)
                                                  .island(0.85f)
                                                  .seaLevel(WATER_HEIGHT);

        try (Terrain terrain = TerrainGenerator.generate(config))
        {
            heightmap = terrain.floats(Layer.HEIGHT);

            // the game maps elevation to terrain cells (see terrain.yml), so
            // rivers and lakes are carved into the heightmap to show up as
            // shallow water
            final byte[] rivers = terrain.mask(Layer.RIVERS);
            final byte[] lakes  = terrain.mask(Layer.LAKES);

            for (int i = 0; i < heightmap.length; i++)
            {
                if (rivers[i] == 1 || lakes[i] == 1)
                    heightmap[i] = Math.min(heightmap[i], WATER_HEIGHT - 0.01f);
            }
        }

        map.loadTerrain(heightmap);
    }

    @Override
    public String header()
    {
        return "World Generation:";
    }

    @Override
    public float handleKeys(final BitVector keys)
    {
        if (keys.get(KeyEvent.VK_ESCAPE))
            screen.select(StartScreen.class);
        else if (keys.get(KeyEvent.VK_R))
        {
            regenerate();
        }
        else if (keys.get(KeyEvent.VK_ENTER))
        {
            try
            {
                map.saveTerrain(heightmap);
            } catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            screen.select(CharScreen.class);
        }

        keys.clear();

        return 0f;
    }

    @Override
    public void display(final AsciiPanel terminal)
    {
        terminal.clear();

        // title:
        drawHeader(terminal);

        final int xmin = 1;
        final int xmax = terminal.getWidthInCharacters() - 1;
        final int ymin = 4;
        final int ymax = terminal.getHeightInCharacters() - 5;

        final int tileWidth  = Math.floorDiv(Options.MAP_SIZE_X, xmax - xmin);
        final int tileHeight = Math.floorDiv(Options.MAP_SIZE_Y, ymax - ymin);

        final ArrayList<MapSystem.Cell> cells = new ArrayList<>(tileWidth * tileHeight);

        for (int x = xmin; x < xmax; x++)
        {
            for (int y = ymin; y < ymax; y++)
            {
                for (int tileX = 0; tileX < tileWidth; tileX++)
                {
                    for (int tileY = 0; tileY < tileHeight; tileY++)
                    {
                        final int posX = (x - xmin) * tileWidth + tileX;
                        final int posY = (y - ymin) * tileHeight + tileY;

                        // render terrain
                        final MapSystem.Cell cell = map.get(posX, posY);

                        cells.add(cell);
                    }
                }

                float r = 0, g = 0, b = 0;
                for (final MapSystem.Cell cell : cells)
                {
                    r += cell.col.getRed() * cell.col.getRed();
                    g += cell.col.getGreen() * cell.col.getGreen();
                    b += cell.col.getBlue() * cell.col.getBlue();
                }

                r = (float) Math.sqrt(r / cells.size());
                g = (float) Math.sqrt(g / cells.size());
                b = (float) Math.sqrt(b / cells.size());

                final Color col = new Color(Math.round(r), Math.round(g), Math.round(b));
                terminal.write(' ', x, y, Color.WHITE, col);

                // final Cell cell = cells.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet()
                //      .stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
                //
                // terminal.write(cell.c, x, y, cell.col, cell.bg);

                cells.clear();
            }
        }

        // TODO must take the full map, downscaled a lot (maybe take average colour per tile? or most common colour?)
        // and show it in a square/rectangle leaving some little black margin

        terminal.writeCenter("[R]egenerate", terminal.getHeightInCharacters() - 4);
        terminal.writeCenter("[ENTER] to confirm, [ESC] to go back", terminal.getHeightInCharacters() - 2);
    }
}
