/**
 * Copyright 2016 Fabio Ticconi
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.fabioticconi.roguelite.behaviours;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelite.components.Carnivore;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Sight;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.EntityGrid;
import com.github.fabioticconi.roguelite.map.Map;
import com.github.fabioticconi.roguelite.systems.MovementSystem;
import com.github.fabioticconi.roguelite.utils.Coords;

import java.util.Set;

/**
 *
 * @author Fabio Ticconi
 */
public class FleeBehaviour extends AbstractBehaviour
{
    ComponentMapper<Sight>     mSight;
    ComponentMapper<Position>  mPosition;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Carnivore> mCarnivore; // FIXME make a more generic FleeFrom
    // taking a component type

    MovementSystem sMovement;

    @Wire EntityGrid grid;
    @Wire Map        map;

    Position curPos;
    Position fleeFrom;

    @Override protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class).build(world);

        fleeFrom = new Position(0, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelite.behaviours.Behaviour#evaluate()
     */
    @Override public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!interested(entityId))
            return 0f;

        curPos = mPosition.get(entityId);
        final int sight = mSight.get(entityId).value;

        final Set<Integer> creatures = grid.getEntities(map.getVisibleCells(curPos.x, curPos.y, sight));

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

        // System.out.println(curPos
        // + " | "
        // + fleeFrom
        // + " --> "
        // + Coords.distanceChebyshev(curPos.x, curPos.y, fleeFrom.x,
        // fleeFrom.y));

        return 1f - (float) Coords.distanceChebyshev(curPos.x, curPos.y, fleeFrom.x, fleeFrom.y) / sight;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelite.behaviours.Behaviour#update()
     */
    @Override public float update()
    {
        Side direction;

        // if the predators' center is HERE, flee randomly out
        if (fleeFrom.x == curPos.x && fleeFrom.y == curPos.y)
        {
            direction = map.getFreeExitRandomised(curPos.x, curPos.y);
        } else
        {
            direction = Side.getSideAt(curPos.x - fleeFrom.x, curPos.y - fleeFrom.y);

            if (map.isObstacle(curPos.x, curPos.y, direction))
            {
                // FIXME is that even possible, since we are looking at visible
                // cells and moving diagonally?
                // if so, we should try the closest exits to the target one

                direction = map.getFreeExitRandomised(curPos.x, curPos.y);
            }
        }

        if (direction == Side.HERE)
            return 0f;

        // System.out.println(direction);

        final float speed = mSpeed.get(entityId).value;

        return sMovement.moveTo(entityId, speed, direction);
    }
}
