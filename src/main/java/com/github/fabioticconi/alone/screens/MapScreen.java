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

import java.awt.event.KeyEvent;

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
        return "Map Generation:";
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

        // TODO must take the full map, downscaled a lot (maybe take average colour per tile? or most common colour?)
        // and show it in a square/rectangle leaving some little black margin

        terminal.writeCenter("[R]egenerare world", terminal.getHeightInCharacters() - 4);
        terminal.writeCenter("[ENTER] to confirm, [ESC] to go back", terminal.getHeightInCharacters() - 2);
    }
}
