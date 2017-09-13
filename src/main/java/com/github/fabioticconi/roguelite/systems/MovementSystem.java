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

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelite.components.Obstacle;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.actions.MoveAction;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.map.SingleGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fabio Ticconi
 */
public class MovementSystem extends DelayedIteratingSystem
{
    static final Logger log = LoggerFactory.getLogger(MovementSystem.class);

    ComponentMapper<Position>   mPosition;
    ComponentMapper<MoveAction> mMove;
    ComponentMapper<Obstacle>   mObstacle;

    MapSystem  sMap;

    @Wire
    SingleGrid grid;

    public MovementSystem()
    {
        super(Aspect.all(Position.class, MoveAction.class));
    }

    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mMove.get(entityId).cooldown;
    }

    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mMove.get(entityId).cooldown -= accumulatedDelta;
    }

    @Override
    protected void processExpired(final int entityId)
    {
        final Position   p = mPosition.get(entityId);
        final MoveAction m = mMove.get(entityId);

        mMove.remove(entityId);

        final int newX = p.x + m.direction.x;
        final int newY = p.y + m.direction.y;

        if (sMap.isObstacle(newX, newY) || !grid.isEmpty(newX, newY))
        {
            // this is weird, and should not be a bump action because it's not a planned action (we were just moving
            // and someone stepped in).
            // Simply, the move fails
            log.info(String.format("%d tried to move to (%d,%d) but it's not empty", entityId, newX, newY));
        }
        else
        {
            final int id = grid.move(p.x, p.y, newX, newY);

            p.x = newX;
            p.y = newY;

            if (id >= 0)
            {
                log.error(String.format("entity %d was at the new position %s", id, p));
            }
        }
    }

    // Public API

    public float moveTo(final int entityId, final float speed, final Side direction)
    {
        final Position p = mPosition.get(entityId);

        final int newX = p.x + direction.x;
        final int newY = p.y + direction.y;

        if (sMap.isObstacle(newX, newY) || !grid.isEmpty(newX, newY))
        {
            // TODO: handle "bump action": this might be an attack if there's a creature there.
            // This should be handled nicely.

            // clear movement completely, even if we were going in another direction
            mMove.remove(entityId);

            return 0f;
        }

        // now we can actually move (or update the movement)

        final MoveAction m = mMove.create(entityId);

        // if going in the same direction, don't increase speed
        if (m.direction == direction)
            return m.cooldown;

        m.cooldown = speed; // TODO: add terrain-specific speed penalty, plus check water etc
        m.direction = direction;

        offerDelay(m.cooldown);

        return m.cooldown;
    }
}
