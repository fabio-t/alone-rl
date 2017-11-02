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
import com.artemis.ComponentMapper;
import com.artemis.managers.PlayerManager;
import com.artemis.utils.BitVector;
import com.artemis.utils.IntBag;
import com.github.fabioticconi.alone.Main;
import com.github.fabioticconi.alone.components.Inventory;
import com.github.fabioticconi.alone.constants.Options;

import java.awt.event.KeyEvent;

/**
 * Author: Fabio Ticconi
 * Date: 02/11/17
 */
public class InventoryScreen extends AbstractScreen
{
    ComponentMapper<Inventory> mInventory;

    ScreenSystem screen;

    PlayerManager pManager;

    @Override
    public float handleKeys(final BitVector keys)
    {
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        if (keys.get(KeyEvent.VK_ESCAPE))
        {
            keys.clear(KeyEvent.VK_ESCAPE);

            screen.select(PlayScreen.class);
        }

        return 0f;
    }

    @Override
    public void display(final AsciiPanel terminal)
    {
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        terminal.clear(' ');

        final Inventory inv = mInventory.get(playerId);

        final IntBag items = inv.items;
        for (int i = 0, size = inv.items.size(); i < size; i++)
        {
            final int itemId = inv.items.get(i);

            terminal.writeCenter(Integer.toString(itemId), terminal.getHeightInCharacters()/2);
        }
    }
}
