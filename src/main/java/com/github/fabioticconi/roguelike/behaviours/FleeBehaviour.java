/**
 * Copyright 2016 Fabio Ticconi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.fabioticconi.roguelike.behaviours;

import java.util.Set;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelike.components.Carnivore;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Sight;
import com.github.fabioticconi.roguelike.components.Speed;
import com.github.fabioticconi.roguelike.constants.Side;
import com.github.fabioticconi.roguelike.map.EntityGrid;
import com.github.fabioticconi.roguelike.map.Map;
import com.github.fabioticconi.roguelike.systems.MovementSystem;

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

    MovementSystem             movement;

    @Wire
    EntityGrid                 grid;
    @Wire
    Map                        map;

    Position                   curPos;
    Position                   fleeFrom;

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class).build(world);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelike.behaviours.Behaviour#evaluate()
     */
    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (notInterested(entityId))
            return 0f;

        curPos = mPosition.get(entityId);
        final int sight = mSight.get(entityId).value;

        final Set<Integer> creatures = grid.getEntitiesWithinRadius(curPos.x, curPos.y, sight);

        if (creatures.isEmpty())
            return 0f;

        fleeFrom = new Position(0, 0);

        int count = 0;
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

        fleeFrom.x = (int) ((float) fleeFrom.x / (float) count);
        fleeFrom.y = (int) ((float) fleeFrom.y / (float) count);

        return 1f - (float) map.distanceBlock(curPos.x, curPos.y, fleeFrom.x, fleeFrom.y) / (float) sight;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelike.behaviours.Behaviour#update()
     */
    @Override
    public float update()
    {
        // FIXME maybe we should take the center of mass of multiple predators,
        // instead of the closest one

        Side direction;

        // if the predators' center is HERE, flee randomly out
        if (fleeFrom.x == curPos.x && fleeFrom.y == curPos.y)
        {
            direction = map.getFreeExitRandomised(curPos.x, curPos.y);
        } else
        {
            // otherwise, flee in the opposite direction

            direction = Side.getSideAt(curPos.x - fleeFrom.x, curPos.y - fleeFrom.y);
        }

        // System.out.println(direction);

        final float speed = mSpeed.get(entityId).value;

        return movement.moveTo(entityId, speed, direction);
    }
}