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
import com.github.fabioticconi.alone.systems.ScreenSystem;

import java.awt.event.KeyEvent;

/**
 * Author: Fabio Ticconi
 * Date: 21/11/17
 */
public class CharScreen extends AbstractScreen
{
    @Override
    public String header()
    {
        return "Character Generation";
    }

    @Override
    public float handleKeys(final BitVector keys)
    {
        if (keys.get(KeyEvent.VK_ESCAPE))
        {
            screen.select(MapScreen.class);
        }
        else if (keys.get(KeyEvent.VK_DOWN))
        {
            // TODO go to next stat
        }
        else if (keys.get(KeyEvent.VK_UP))
        {
            // TODO go back to previous stat
        }
        else if (keys.get(KeyEvent.VK_LEFT))
        {
            // TODO remove one point from selected stat
        }
        else if (keys.get(KeyEvent.VK_RIGHT))
        {
            // TODO add one point to selected stat
        }

        keys.clear();

        return 0f;
    }

    @Override
    public void display(final AsciiPanel terminal)
    {
        // TODO draw a list of three options, one for each character stat,
        // and keep track of how many stat points have been added

        // (colour currently selected stat in yellow?)

        terminal.writeCenter("[type ENTER to confirm]", terminal.getHeightInCharacters() - 2);
    }
}
