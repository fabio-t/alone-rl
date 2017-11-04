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

import com.artemis.ComponentMapper;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.components.Corpse;
import com.github.fabioticconi.alone.systems.HungerSystem;

/**
 * Author: Fabio Ticconi
 * Date: 02/11/17
 */
public class EatScreen extends InventoryScreen
{
    HungerSystem sHunger;

    ComponentMapper<Corpse> mCorpse;

    @Override
    public float handleKeys(final BitVector keys)
    {
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final int targetId = getTarget(keys);

        if (targetId < 0)
            return super.handleKeys(keys);

        screen.select(PlayScreen.class);

        return sAction.act(sHunger.eat(playerId, targetId));
    }

    @Override
    public String header()
    {
        return "Eat item:";
    }

    @Override
    public boolean canDraw(final int entityId)
    {
        return mCorpse.has(entityId);
    }
}
