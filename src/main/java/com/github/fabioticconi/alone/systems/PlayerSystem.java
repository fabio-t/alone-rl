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
import com.github.fabioticconi.alone.components.Player;
import com.github.fabioticconi.alone.components.Speed;
import com.github.fabioticconi.alone.components.Stamina;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.messages.AbstractMessage;
import com.github.fabioticconi.alone.messages.Message;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;

public class PlayerSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(PlayerSystem.class);

    ComponentMapper<Stamina> mStamina;
    ComponentMapper<Speed>   mSpeed;
    ComponentMapper<Player>  mPlayer;

    ActionSystem sAction;
    BumpSystem   sBump;
    ItemSystem   sItems;
    HungerSystem sHunger;
    ThrowSystem  sThrow;

    PlayerManager pManager;

    // FIXME only for debug..
    private float savedSpeed = -1f;

    public float handleKeys(final BitVector keys)
    {
        // FIXME: hackish, very crappy but it should work
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Stamina stamina = mStamina.get(playerId);

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
                return sBump.bumpAction(playerId, Side.NW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // northeast
                return sBump.bumpAction(playerId, Side.NE);
            }
            else
            {
                // north
                return sBump.bumpAction(playerId, Side.N);
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
                return sBump.bumpAction(playerId, Side.SW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // southeast
                return sBump.bumpAction(playerId, Side.SE);
            }
            else
            {
                // south
                return sBump.bumpAction(playerId, Side.S);
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
            return sBump.bumpAction(playerId, Side.E);
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
            return sBump.bumpAction(playerId, Side.W);
        }
        else if (keys.get(KeyEvent.VK_G))
        {
            keys.clear(KeyEvent.VK_G);

            return sAction.act(sItems.get(playerId));
        }
        else if (keys.get(KeyEvent.VK_D))
        {
            keys.clear(KeyEvent.VK_D);

            return sAction.act(sItems.drop(playerId));
        }
        else if (keys.get(KeyEvent.VK_E))
        {
            keys.clear(KeyEvent.VK_E);

            return sAction.act(sHunger.devourClosestCorpse(playerId));
        }
        else if (keys.get(KeyEvent.VK_T))
        {
            keys.clear(KeyEvent.VK_T);

            return sAction.act(sThrow.throwWeapon(playerId));
        }
        else if (keys.get(KeyEvent.VK_ESCAPE))
        {
            Main.keepRunning = false;
        }
        else if (keys.get(KeyEvent.VK_F1))
        {
            keys.clear(KeyEvent.VK_F1);
            if (savedSpeed == -1f)
                savedSpeed = mSpeed.get(playerId).value;
            mSpeed.get(playerId).value = 0f; // FIXME to remove later, only for debug
        }
        else if (keys.get(KeyEvent.VK_F2))
        {
            keys.clear(KeyEvent.VK_F2);

            if (savedSpeed >= 0f)
                mSpeed.create(playerId).value = savedSpeed; // FIXME to remove later, only for debug
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

                // in real-time mode, space means pause/unpause the game, while
                // in turn-based mode, space means unpause the game until SPACE is released
                Main.paused = Main.realtime && !Main.paused;
            }

            keys.clear(KeyEvent.VK_SPACE);
        }

        return 0f;
    }

    public void message(final AbstractMessage msg, final int actorId)
    {
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        if (actorId != playerId)
            return;

        final Player p = mPlayer.get(actorId);

        p.messages.push(msg);
    }
}
