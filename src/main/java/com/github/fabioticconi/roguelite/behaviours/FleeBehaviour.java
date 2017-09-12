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
package com.github.fabioticconi.roguelite.behaviours;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelite.components.Carnivore;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.Map;
import com.github.fabioticconi.roguelite.map.SingleGrid;
import com.github.fabioticconi.roguelite.systems.MovementSystem;
import com.github.fabioticconi.roguelite.utils.Coords;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fabio Ticconi
 */
public class FleeBehaviour extends AbstractBehaviour
{
    static final Logger log = LoggerFactory.getLogger(FleeBehaviour.class);

    ComponentMapper<Sight>     mSight;
    ComponentMapper<Position>  mPosition;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Carnivore> mCarnivore; // FIXME make a more generic FleeFrom
    // taking a component type

    MovementSystem sMovement;

    @Wire
    SingleGrid grid;
    @Wire
    Map        map;

    Position curPos;
    Position fleeFrom;

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class).build(world);

        fleeFrom = new Position(0, 0);
    }

    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!interested(entityId))
            return 0f;

        curPos = mPosition.get(entityId);
        final int sight = mSight.get(entityId).value;

        final IntSet creatures = grid.getEntities(map.getVisibleCells(curPos.x, curPos.y, sight));

        if (creatures.isEmpty())
            return 0f;

        fleeFrom.x = 0;
        fleeFrom.y = 0;

        int      count = 0;
        Position tempPos;
        for (final int creatureId : creatures)
        {
            if (mCarnivore.has(creatureId))
            {
                tempPos = mPosition.get(creatureId);

                fleeFrom.x += tempPos.x;
                fleeFrom.y += tempPos.y;

                count++;
            }
        }

        if (count == 0)
            return 0f;

        fleeFrom.x = Math.floorDiv(fleeFrom.x, count);
        fleeFrom.y = Math.floorDiv(fleeFrom.y, count);

        return 1f - (float) Coords.distanceChebyshev(curPos.x, curPos.y, fleeFrom.x, fleeFrom.y) / sight;
    }

    @Override
    public float update()
    {
        Side direction;

        direction = Side.getSideAt(curPos.x - fleeFrom.x, curPos.y - fleeFrom.y);

        if (map.isObstacle(curPos.x, curPos.y, direction))
        {
            // FIXME is that even possible, since we are looking at visible
            // cells and moving diagonally?
            // if so, we should try the closest exits to the target one
//
            direction = map.getFreeExitRandomised(curPos.x, curPos.y);

            log.error("we were fleeing toward a visible cell but now it's a obstacle");
        }

        if (direction == Side.HERE)
            return 0f;

        final float speed = mSpeed.get(entityId).value;

        return sMovement.moveTo(entityId, speed, direction);
    }
}
