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

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.MoveAction;
import com.github.fabioticconi.alone.constants.Cell;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.map.MapSystem;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;
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
    ComponentMapper<Stamina>    mStamina;
    ComponentMapper<Speed>      mSpeed;
    ComponentMapper<Obstacle>   mObstacle;

    MapSystem     sMap;
    StaminaSystem sStamina;

    @Wire
    SingleGrid grid;

    @Wire
    MultipleGrid items;

    public MovementSystem()
    {
        super(Aspect.all(Position.class, MoveAction.class).exclude(Dead.class));
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

        if (!mObstacle.has(entityId))
        {
            // it's a moving item
            items.move(entityId, p.x, p.y, newX, newY);

            p.x = newX;
            p.y = newY;

            // it doesn't have stamina
        }
        else if (sMap.isFree(newX, newY))
        {
            final int id = grid.move(p.x, p.y, newX, newY);

            if (id >= 0)
            {
                log.error("entity {} was at the new position {}", id, p);

                return;
            }

            p.x = newX;
            p.y = newY;

            sStamina.consume(entityId, m.cost);
        }
    }

    // Public API

    public float moveTo(final int entityId, final Side direction)
    {
        if (direction.equals(Side.HERE))
            return 0f;

        final Position p     = mPosition.get(entityId);
        final Speed    speed = mSpeed.get(entityId);

        final int newX = p.x + direction.x;
        final int newY = p.y + direction.y;

        if (!sMap.isFree(newX, newY))
        {
            // clear movement completely, even if we were going in another direction
            mMove.remove(entityId);

            return 0f;
        }

        // now we can actually move (or update the movement)

        final MoveAction m = mMove.create(entityId);

        // if going in the same direction, don't increase speed
        if (m.direction == direction)
            return m.cooldown;

        final Cell cell = sMap.get(newX, newY);

        switch (cell)
        {
            case HILL:
            case HILL_GRASS:
                m.cost = 1.25f;

                break;

            case MOUNTAIN:
                m.cost = 1.5f;
                break;

            case HIGH_MOUNTAIN:
            case WATER:
                m.cost = 2f;
                break;

            case DEEP_WATER:
                m.cost = 3f;
                break;

            default:
                m.cost = 1f;
        }

        m.cooldown = speed.value * m.cost;
        m.direction = direction;

        offerDelay(m.cooldown);

        return m.cooldown;
    }
}
