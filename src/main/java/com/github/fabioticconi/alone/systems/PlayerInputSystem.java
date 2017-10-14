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

package com.github.fabioticconi.alone.systems;

import com.artemis.ComponentMapper;
import com.artemis.managers.PlayerManager;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.Main;
import com.github.fabioticconi.alone.components.Speed;
import com.github.fabioticconi.alone.components.Stamina;
import com.github.fabioticconi.alone.constants.Side;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;

public class PlayerInputSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(PlayerInputSystem.class);

    ComponentMapper<Speed>   mSpeed;
    ComponentMapper<Stamina> mStamina;

    BumpSystem   sBump;
    ItemSystem   sItems;
    HungerSystem sHunger;
    ThrowSystem  sThrow;

    PlayerManager pManager;

    public float handleKeys(final BitVector keys)
    {
        // FIXME: hackish, very crappy but it should work
        final int pID = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Stamina stamina = mStamina.get(pID);

        final float speed = mSpeed.get(pID).value;

        if (keys.get(KeyEvent.VK_UP))
        {
            if (stamina.exhausted)
            {
                // FIXME: this should go to a message log

                log.info("you are too exhausted to move");

                return 0f;
            }

            if (keys.get(KeyEvent.VK_LEFT))
            {
                // northwest
                return sBump.bumpAction(pID, Side.NW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // northeast
                return sBump.bumpAction(pID, Side.NE);
            }
            else
            {
                // north
                return sBump.bumpAction(pID, Side.N);
            }
        }
        else if (keys.get(KeyEvent.VK_DOWN))
        {
            if (stamina.exhausted)
            {
                // FIXME: this should go to a message log

                log.info("you are too exhausted to move");

                return 0f;
            }

            if (keys.get(KeyEvent.VK_LEFT))
            {
                // southwest
                return sBump.bumpAction(pID, Side.SW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // southeast
                return sBump.bumpAction(pID, Side.SE);
            }
            else
            {
                // south
                return sBump.bumpAction(pID, Side.S);
            }
        }
        else if (keys.get(KeyEvent.VK_RIGHT))
        {
            if (stamina.exhausted)
            {
                // FIXME: this should go to a message log

                log.info("you are too exhausted to move");

                return 0f;
            }

            // east
            return sBump.bumpAction(pID, Side.E);
        }
        else if (keys.get(KeyEvent.VK_LEFT))
        {
            if (stamina.exhausted)
            {
                // FIXME: this should go to a message log

                log.info("you are too exhausted to move");

                return 0f;
            }

            // west
            return sBump.bumpAction(pID, Side.W);
        }
        else if (keys.get(KeyEvent.VK_G))
        {
            // no combined key-presses like for movement, so we make sure to clear it
            keys.clear(KeyEvent.VK_G);

            if (sItems.get(pID) < 0)
                return 0f;
            else
                return 0.1f; // very small fixed "cost"
        }
        else if (keys.get(KeyEvent.VK_D))
        {
            // no combined key-presses like for movement, so we make sure to clear it
            keys.clear(KeyEvent.VK_D);

            if (sItems.drop(pID) < 0)
                return 0f;
            else
                return 0.1f; // very small fixed "cost"
        }
        else if (keys.get(KeyEvent.VK_E))
        {
            keys.clear(KeyEvent.VK_E);

            return sHunger.devourClosestCorpse(pID);
        }
        else if (keys.get(KeyEvent.VK_T))
        {
            keys.clear(KeyEvent.VK_T);

            return sThrow.throwSomethingAtClosestEnemy(pID);
        }
        else if (keys.get(KeyEvent.VK_ESCAPE))
        {
            Main.keepRunning = false;
        }
        else if (keys.get(KeyEvent.VK_SPACE))
        {
            if (keys.get(KeyEvent.VK_CONTROL))
            {
                // Ctrl+Space means we are toggling the real-time mode
                Main.realtime = !Main.realtime;
                // as well as toggling the pause, of course
                Main.paused = !Main.paused;

                keys.clear(KeyEvent.VK_CONTROL);
            }
            else
            {
                // Space alone has two meanings:

                if (Main.realtime)
                {
                    // in real-time mode, space means pause/unpause the game
                    Main.paused = !Main.paused;
                }
                else
                {
                    // in turn-based mode, space means unpause the game for this frame
                    Main.paused = false;
                }
            }

            keys.clear(KeyEvent.VK_SPACE);
        }

        return 0f;
    }
}
