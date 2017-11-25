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

package com.github.fabioticconi.alone.screens;

import asciiPanel.AsciiPanel;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.constants.Cell;
import com.github.fabioticconi.alone.constants.Options;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Author: Fabio Ticconi
 * Date: 21/11/17
 */
public class MapScreen extends AbstractScreen
{
    // TODO if no map is present at startup, must generate it
    // so that the first call to display will show it

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
            // TODO regenerate map (must call TerGen and store data somewhere)
        }
        else if (keys.get(KeyEvent.VK_ENTER))
        {
            // TODO maybe must "confirm" the map through the MapSystem, somehow?
            // Or we could achieve it just by copying the current map data to the data/map/ folder
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

        final ArrayList<Cell> cells = new ArrayList<>(tileWidth * tileHeight);

        for (int x = xmin; x < xmax; x++)
        {
            for (int y = ymin; y < ymax; y++)
            {
                for (int tileX = 0; tileX < tileWidth; tileX++)
                {
                    for (int tileY = 0; tileY < tileHeight; tileY++)
                    {
                        final int posX = x * tileWidth + tileX;
                        final int posY = y * tileHeight + tileY;

                        // render terrain
                        final Cell cell = map.get(posX, posY);

                        cells.add(cell);
                    }
                }

                float r=0,g=0,b=0;
                for (final Cell cell : cells)
                {
                    r += cell.bg.getRed()*cell.bg.getRed();
                    g += cell.bg.getGreen()*cell.bg.getGreen();
                    b += cell.bg.getBlue()*cell.bg.getBlue();
                }

                r = (float)Math.sqrt(r / cells.size());
                g = (float)Math.sqrt(g / cells.size());
                b = (float)Math.sqrt(b / cells.size());

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
