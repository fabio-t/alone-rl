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
import com.github.fabioticconi.alone.components.Name;
import com.github.fabioticconi.alone.components.Player;
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.attributes.Sight;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.messages.AbstractMessage;
import com.github.fabioticconi.alone.utils.Coords;
import net.mostlyoriginal.api.system.core.PassiveSystem;

/**
 * Author: Fabio Ticconi
 * Date: 30/10/17
 */
public class MessageSystem extends PassiveSystem
{
    ComponentMapper<Name>     mName;
    ComponentMapper<Position> mPos;
    ComponentMapper<Player>   mPlayer;
    ComponentMapper<Sight>    mSight;

    PlayerManager pManager;

    public void send(final int actorId, final AbstractMessage msg)
    {
        if (actorId < 0)
            return;

        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Sight sight = mSight.get(playerId);

        final Player player;
        if (playerId == actorId)
        {
            player = mPlayer.get(actorId);
            msg.distance = 0;
            msg.direction = Side.HERE;
        }
        else
        {
            player = mPlayer.get(playerId);
            msg.thirdPerson = true;

            final Position p1 = mPos.get(actorId);
            final Position p2 = mPos.get(playerId);

            if (p1 != null && p2 != null)
            {
                msg.distance = Coords.distanceChebyshev(p1.x, p1.y, p2.x, p2.y);

                // only send events in a certain range of the player
                if (msg.distance > sight.value)
                    return;

                msg.direction = Side.getSide(p1.x, p1.y, p2.x, p2.y);
            }
            else
            {
                msg.distance = 0;
                msg.direction = Side.HERE;
            }
        }

        msg.actor = mName.get(actorId).name;

        player.messages.push(msg);
    }

    public void send(final int actorId, final int targetId, final AbstractMessage msg)
    {
        if (actorId < 0 || targetId < 0)
            return;

        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Sight sight = mSight.get(playerId);

        final Position p1 = mPos.get(actorId);
        final Position p2 = mPos.get(targetId);
        final Position p3 = mPos.get(playerId);

        final int  distance;
        final Side direction;
        if (p1 != null && p2 != null)
        {
            distance = Math.min(Coords.distanceChebyshev(p1.x, p1.y, p3.x, p3.y),
                                Coords.distanceChebyshev(p2.x, p2.y, p3.x, p3.y));

            // only send events in a certain range of the player
            if (distance > sight.value)
                return;

            direction = Side.getSide(p1.x, p1.y, p2.x, p2.y);
        }
        else
        {
            distance = 0;
            direction = Side.HERE;
        }

        msg.distance = distance;
        msg.direction = direction;

        // TODO maybe only let distance=0 message go to the player if the actor or target is the player?

        msg.actor = mName.get(actorId).name;
        msg.target = mName.get(targetId).name;

        final Player player;
        if (playerId == actorId)
        {
            player = mPlayer.get(actorId);
        }
        else if (playerId == targetId)
        {
            player = mPlayer.get(targetId);
            msg.thirdPerson = true;
        }
        else
        {
            player = mPlayer.get(playerId);
            msg.thirdPerson = true;
        }

        player.messages.push(msg);
    }
}
