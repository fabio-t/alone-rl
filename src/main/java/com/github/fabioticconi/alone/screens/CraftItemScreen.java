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
import com.github.fabioticconi.alone.components.Inventory;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.messages.CraftMsg;
import com.github.fabioticconi.alone.systems.CraftSystem;
import com.github.fabioticconi.alone.systems.MessageSystem;
import com.github.fabioticconi.alone.systems.ScreenSystem;

import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * Author: Fabio Ticconi
 * Date: 05/11/17
 */
public class CraftItemScreen extends AbstractScreen
{
    ComponentMapper<Inventory> mInventory;

    ScreenSystem  screen;
    CraftSystem   sCraft;
    MessageSystem msg;

    CraftScreen craftScreen;

    @Override
    public String header()
    {
        return "Craft " + craftScreen.craftItem.tag + "?";
    }

    @Override
    public float handleKeys(final BitVector keys)
    {
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        if (keys.get(KeyEvent.VK_ESCAPE))
            screen.select(CraftScreen.class);
        else if (keys.get(KeyEvent.VK_ENTER))
        {
            System.out.println(craftScreen.craftItem);
            final int id = sCraft.craftItem(playerId, craftScreen.craftItem);
            if (id >= 0)
            {
                final Inventory inv = mInventory.get(playerId);
                inv.items.add(id);
                msg.send(playerId, id, new CraftMsg());
            }
            else
                msg.send(playerId, id, new CannotMsg("craft"));

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

        int height = terminal.getHeightInCharacters() / 3;

        terminal.writeCenter("Consumes:", height);
        terminal.writeCenter(Arrays.toString(craftScreen.craftItem.sources), height + 2);

        height += 8;

        terminal.writeCenter("Tools needed:", height);
        terminal.writeCenter(Arrays.toString(craftScreen.craftItem.tools), height + 2);

        terminal.writeCenter("[ type ENTER to confirm ]", terminal.getHeightInCharacters() - 2);
    }
}
