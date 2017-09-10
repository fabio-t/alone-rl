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
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.actions.MoveAction;
import com.github.fabioticconi.roguelite.constants.Cell;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.ItemGrid;
import com.github.fabioticconi.roguelite.map.Map;

/**
 * @author Fabio Ticconi
 */
public class MovementSystem extends DelayedIteratingSystem
{
    ComponentMapper<Position>   mPosition;
    ComponentMapper<MoveAction> mMove;

    @Wire
    Map      map;
    @Wire
    ItemGrid grid;

    public MovementSystem()
    {
        super(Aspect.all(Position.class, MoveAction.class));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#getRemainingDelay(int)
     */
    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mMove.get(entityId).cooldown;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#processDelta(int, float)
     */
    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mMove.get(entityId).cooldown -= accumulatedDelta;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#processExpired(int)
     */
    @Override
    protected void processExpired(final int entityId)
    {
        final Position   p = mPosition.get(entityId);
        final MoveAction m = mMove.get(entityId);

        mMove.remove(entityId);

        final int newX = p.x + m.direction.x;
        final int newY = p.y + m.direction.y;

        if (!map.isObstacle(newX, newY))
        {
            grid.moveEntity(entityId, p.x, p.y, newX, newY);

            p.x = newX;
            p.y = newY;
        }
        else
        {
            // moving towards a closed door opens it, instead of actually moving
//            if (map.get(newX, newY) == Cell.CLOSED_DOOR)
//            {
//                map.set(newX, newY, Cell.OPEN_DOOR);
//            }
        }
    }

    // Public API

    public float moveTo(final int entityId, final float speed, final Side direction)
    {
        final MoveAction m = mMove.create(entityId);

        // TODO: check here if direction is obstacle, and if so raise a "collision"
        // so that the appropriate action can be taken
        // (doing it here, instead of in the player/AI specific code, might be a bit ugly but
        // it should allow the same code to work for player and mobs)

        if (m.direction == direction)
            return m.cooldown;

        m.cooldown = speed;
        m.direction = direction;

        offerDelay(m.cooldown);

        return m.cooldown;
    }
}
