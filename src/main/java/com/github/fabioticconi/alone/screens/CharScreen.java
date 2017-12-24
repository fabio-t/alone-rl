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
import com.artemis.ComponentMapper;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.components.attributes.Agility;
import com.github.fabioticconi.alone.components.attributes.Constitution;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.systems.CreatureSystem;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Collections;

/**
 * Author: Fabio Ticconi
 * Date: 21/11/17
 */
public class CharScreen extends AbstractScreen
{
    CreatureSystem sCreature;

    ComponentMapper<Strength>     mStr;
    ComponentMapper<Agility>      mAgi;
    ComponentMapper<Constitution> mCon;

    byte[] stats = new byte[3];
    int curStat = 0;
    int points = 1;

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
            if (curStat < 2)
                curStat++;
        }
        else if (keys.get(KeyEvent.VK_UP))
        {
            if (curStat > 0)
                curStat--;
        }
        else if (keys.get(KeyEvent.VK_LEFT))
        {
            if (stats[curStat] > -2)
            {
                stats[curStat]--;
                points++;
            }
        }
        else if (keys.get(KeyEvent.VK_RIGHT))
        {
            if (stats[curStat] < 2 && points > 0)
            {
                stats[curStat]++;
                points--;
            }
        }
        else if (keys.get(KeyEvent.VK_ENTER))
        {
            // confirmed, let's play!

            // but first, we place creatures, items, trees..
            sCreature.reset();

            // we also need to recalculate the player
            final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

            mStr.create(playerId).value = stats[0];
            mAgi.create(playerId).value = stats[1];
            mCon.create(playerId).value = stats[2];

            sCreature.makeDerivative(playerId);

            screen.select(PlayScreen.class);
        }

        keys.clear();

        return 0f;
    }

    @Override
    public void display(final AsciiPanel terminal)
    {
        terminal.clear();

        drawHeader(terminal);

        terminal.writeCenter("Available Attribute Points: [" + points + "]", 20);

        int yoff = terminal.getHeightInCharacters() / 2 - 1;
        final int xoff = terminal.getWidthInCharacters() / 2 - 12;

        final String str = "[" + String.join("", Collections.nCopies(stats[0]+3, "=")) + "]";
        final String agi = "[" + String.join("", Collections.nCopies(stats[1]+3, "=")) + "]";
        final String con = "[" + String.join("", Collections.nCopies(stats[2]+3, "=")) + "]";

        terminal.write("Strength:     " + str, xoff, yoff, curStat==0?Color.YELLOW:Color.WHITE);
        yoff += 3;
        terminal.write("Agility:      " + agi, xoff, yoff, curStat==1?Color.YELLOW:Color.WHITE);
        yoff += 3;
        terminal.write("Constitution: " + con, xoff, yoff, curStat==2?Color.YELLOW:Color.WHITE);

        terminal.writeCenter("[ENTER] to play, [ESC] to go back", terminal.getHeightInCharacters() - 2);
    }
}
