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
import com.artemis.annotations.Wire;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.Main;
import com.github.fabioticconi.alone.systems.ScreenSystem;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Properties;

/**
 * Author: Fabio Ticconi
 * Date: 04/11/17
 */
public class StartScreen extends AbstractScreen
{
    ScreenSystem screen;

    @Wire
    Properties properties;

    @Override
    public float handleKeys(final BitVector keys)
    {
        // TODO: eventually, "N" should re-run the world to start from scratch,
        // while "C" either selects the PlayScreen, or (at first start) loads the save file

        if (keys.get(KeyEvent.VK_N) || keys.get(KeyEvent.VK_C))
        {
            keys.clear();

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

        final String title = title();

        terminal.writeCenter(title, 2);
        terminal.writeCenter(String.join("", Collections.nCopies(title.length(), "-")), 3);

        int y = terminal.getHeightInCharacters() / 2 - 4 * 2;
        terminal.writeCenter("(N)ew", y);
        y = y + 2;
        terminal.writeCenter("(C)ontinue", y);
        y = y + 2;
        terminal.writeCenter("(O)ptions [inactive]", y);
        y = y + 2;
        terminal.writeCenter("Save & (Q)uit", y);

        y = terminal.getHeightInCharacters() - 2;
        terminal.writeCenter("by Fabio Ticconi", y);

        // TODO credits?
    }

    private String title()
    {
        return String.format("%s v%s", properties.getProperty("name"), properties.getProperty("version"));
    }
}
