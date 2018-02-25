/*
 * Copyright (C) 2015-2017 Fabio Ticconi
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
import com.artemis.annotations.Wire;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.Main;
import com.github.fabioticconi.alone.systems.CreatureSystem;
import com.github.fabioticconi.alone.systems.MapSystem;
import com.github.fabioticconi.alone.systems.ScreenSystem;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

/**
 * Author: Fabio Ticconi
 * Date: 04/11/17
 */
public class StartScreen extends AbstractScreen
{
    MapSystem      map;
    CreatureSystem sCreature;
    ScreenSystem   screen;

    @Wire
    Properties properties;

    @Override
    public float handleKeys(final BitVector keys)
    {
        if (keys.get(KeyEvent.VK_N))
        {
            keys.clear();

            // every time "new" is selected, everything is lost, even if we don't proceed further
            map.reset();

            screen.select(MapScreen.class);
        }
        else if (keys.get(KeyEvent.VK_C))
        {
            keys.clear();

            // TODO: [C] should load a saved game (if any), or if the world is already loaded
            // (eg, if the player types ESC while playing), it must simply jump to the PlayScreen

            try
            {
                map.loadTerrain();
            } catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            // FIXME: remove this later, we should instead load the data from the last save
            sCreature.placeObjects();

            screen.select(PlayScreen.class);
        }
        else if (keys.get(KeyEvent.VK_Q))
        {
            Main.keepRunning = false;
        }

        return 0f;
    }

    @Override
    public void display(final AsciiPanel terminal)
    {
        terminal.clear(' ');

        final String header = header();

        terminal.writeCenter(header, 2);
        terminal.writeCenter(String.join("", Collections.nCopies(header.length(), "-")), 3);

        int y = terminal.getHeightInCharacters() / 2 - 4 * 2;
        terminal.writeCenter("[N]ew", y);
        y = y + 2;
        terminal.writeCenter("[C]ontinue", y, Color.GRAY);
        y = y + 2;
        terminal.writeCenter("[O]ptions [inactive]", y);
        y = y + 2;
        terminal.writeCenter("Save & [Q]uit", y);

        y = terminal.getHeightInCharacters() - 2;
        terminal.writeCenter("by Fabio Ticconi", y);

        // TODO credits?
    }

    @Override
    public String header()
    {
        return String.format("%s v%s", properties.getProperty("name"), properties.getProperty("version"));
    }
}
