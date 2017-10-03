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

package com.github.fabioticconi.roguelite.systems;

import com.artemis.ComponentMapper;
import com.artemis.managers.PlayerManager;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.roguelite.Roguelite;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.constants.Side;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import java.awt.event.KeyEvent;

public class PlayerInputSystem extends PassiveSystem
{
    ComponentMapper<Speed> mSpeed;

    MovementSystem sMove;
    ItemSystem     sItems;

    PlayerManager pManager;

    public float handleKeys(final BitVector keys)
    {
        // FIXME: hackish, very crappy but it should work
        final int pID = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final float speed = mSpeed.get(pID).value;

        if (keys.get(KeyEvent.VK_UP))
        {
            if (keys.get(KeyEvent.VK_LEFT))
            {
                // northwest
                return sMove.moveTo(pID, speed, Side.NW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // northeast
                return sMove.moveTo(pID, speed, Side.NE);
            }
            else
            {
                // north
                return sMove.moveTo(pID, speed, Side.N);
            }
        }
        else if (keys.get(KeyEvent.VK_DOWN))
        {
            if (keys.get(KeyEvent.VK_LEFT))
            {
                // southwest
                return sMove.moveTo(pID, speed, Side.SW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // southeast
                return sMove.moveTo(pID, speed, Side.SE);
            }
            else
            {
                // south
                return sMove.moveTo(pID, speed, Side.S);
            }
        }
        else if (keys.get(KeyEvent.VK_RIGHT))
        {
            // east
            return sMove.moveTo(pID, speed, Side.E);
        }
        else if (keys.get(KeyEvent.VK_LEFT))
        {
            // west
            return sMove.moveTo(pID, speed, Side.W);
        }
        else if (keys.get(KeyEvent.VK_G))
        {
            // no combined key-presses like for movement, so we make sure to clear it
            keys.clear(KeyEvent.VK_G);

            if (sItems.get(pID) < 0)
                return 0f;
            else
                return 1f; // fixed "cost"
        }
        else if (keys.get(KeyEvent.VK_D))
        {
            // no combined key-presses like for movement, so we make sure to clear it
            keys.clear(KeyEvent.VK_D);

            if (sItems.drop(pID) < 0)
                return 0f;
            else
                return 1f; // fixed "cost"
        }
        else if (keys.get(KeyEvent.VK_ESCAPE))
        {
            Roguelite.keepRunning = false;
        }

        return 0f;
    }
}
