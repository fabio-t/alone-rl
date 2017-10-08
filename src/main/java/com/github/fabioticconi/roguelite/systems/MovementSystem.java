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
import com.github.fabioticconi.roguelite.PushSystem;
import com.github.fabioticconi.roguelite.components.*;
import com.github.fabioticconi.roguelite.components.actions.MoveAction;
import com.github.fabioticconi.roguelite.constants.Cell;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.map.SingleGrid;
import com.github.fabioticconi.roguelite.utils.Coords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point2I;

import java.util.List;

/**
 * @author Fabio Ticconi
 */
public class MovementSystem extends DelayedIteratingSystem
{
    static final Logger log = LoggerFactory.getLogger(MovementSystem.class);

    ComponentMapper<Position>   mPosition;
    ComponentMapper<MoveAction> mMove;
    ComponentMapper<Health>     mHealth;
    ComponentMapper<Tree>       mTree;
    ComponentMapper<Pushable>   mPushable;

    MapSystem     sMap;
    AttackSystem  sAttack;
    StaminaSystem sStamina;
    TreeSystem    sTree;
    PushSystem    sPush;

    @Wire
    SingleGrid grid;

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

        if (sMap.isFree(newX, newY))
        {
            final int id = grid.move(p.x, p.y, newX, newY);

            if (id >= 0)
            {
                log.error("entity {} was at the new position {}", id, p);

                return;
            }

            p.x = newX;
            p.y = newY;

            // consume a fixed amount of stamina
            sStamina.consume(entityId, m.cost);
        }
    }

    // Public API

    public float moveTo(final int entityId, final float speed, final Position target)
    {
        final Position pos = mPosition.get(entityId);

        if (Coords.distanceChebyshev(pos.x, pos.y, target.x, target.y) == 1)
        {
            // it's only one step away, no point calculating line of sight
            return moveTo(entityId, speed, Side.getSideAt(target.x - pos.x, target.y - pos.y));
        }

        final List<Point2I> path = sMap.getLineOfSight(pos.x, pos.y, target.x, target.y);

        if (path == null || path.size() < 2)
        {
            // the target position is the same as the entity's position,
            // or the target is not visible. Either way, we don't move.

            return 0f;
        }

        // position 0 is "HERE"
        final Point2I p = path.get(1);

        return moveTo(entityId, speed, Side.getSideAt(p.x - pos.x, p.y - pos.y));
    }

    public float moveTo(final int entityId, final float speed, final Side direction)
    {
        if (direction.equals(Side.HERE))
            return 0f;

        final Position p = mPosition.get(entityId);

        final int newX = p.x + direction.x;
        final int newY = p.y + direction.y;

        if (!sMap.isFree(newX, newY))
        {
            // clear movement completely, even if we were going in another direction
            mMove.remove(entityId);

            final int obstacleId = grid.get(newX, newY);

            if (obstacleId >= 0)
            {
                if (mTree.has(obstacleId))
                {
                    return sTree.cut(entityId, obstacleId);
                }
                else if (mPushable.has(obstacleId))
                {
                    return sPush.push(entityId, obstacleId);
                }
                else if (mHealth.has(obstacleId))
                    return sAttack.attack(entityId, obstacleId);
            }

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

        m.cooldown = speed * m.cost;
        m.direction = direction;

        offerDelay(m.cooldown);

        return m.cooldown;
    }
}
