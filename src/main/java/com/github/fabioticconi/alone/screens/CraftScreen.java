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
import com.github.fabioticconi.alone.components.Equip;
import com.github.fabioticconi.alone.components.Inventory;
import com.github.fabioticconi.alone.components.Name;
import com.github.fabioticconi.alone.systems.ActionSystem;
import com.github.fabioticconi.alone.systems.CraftSystem;
import com.github.fabioticconi.alone.systems.ItemSystem;
import com.github.fabioticconi.alone.systems.ScreenSystem;

import java.awt.event.KeyEvent;

/**
 * Author: Fabio Ticconi
 * Date: 05/11/17
 */
public class CraftScreen extends AbstractScreen
{
    ComponentMapper<Name>      mName;
    ComponentMapper<Inventory> mInventory;
    ComponentMapper<Equip>     mEquip;

    ScreenSystem screen;
    ActionSystem sAction;
    ItemSystem   sItems;
    CraftSystem  sCraft;

    PlayerManager pManager;

    String craftItem = "Test";

    @Override
    public float handleKeys(final BitVector keys)
    {
        if (keys.get(KeyEvent.VK_ESCAPE))
            screen.select(PlayScreen.class);
        else
        {
            final int pos = getTargetIndex(keys);

            if (pos >= 0)
                screen.select(CraftItemScreen.class);
        }

        keys.clear();

        return 0f;
    }

    @Override
    public void display(final AsciiPanel terminal)
    {
        drawHeader(terminal);

        drawList(terminal, sCraft.getRecipeNames());
    }

    @Override
    public String header()
    {
        return "Craft item:";
    }
}
