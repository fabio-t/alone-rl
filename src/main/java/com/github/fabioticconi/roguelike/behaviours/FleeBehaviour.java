/**
 * Copyright 2016 Fabio Ticconi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fabioticconi.roguelike.behaviours;

import java.util.Set;

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

import net.mostlyoriginal.api.system.core.PassiveSystem;

/**
 *
 * @author Fabio Ticconi
 */
public class FleeBehaviour extends PassiveSystem implements Behaviour
{
    ComponentMapper<Sight>     mSight;
    ComponentMapper<Position>  mPosition;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Carnivore> mCarnivore; // FIXME make a more generic FleeFrom taking a component type

    MovementSystem             movement;

    @Wire
    EntityGrid                 grid;
    @Wire
    Map                        map;

    int                        entityId;
    Position                   curPos;
    Position                   fleeFrom;

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelike.behaviours.Behaviour#evaluate()
     */
    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        curPos = mPosition.get(entityId);
        final int sight = mSight.get(entityId).value;

        final Set<Integer> creatures = grid.getEntitiesWithinRadius(curPos.x, curPos.y, sight);

        for (final int creatureId : creatures)
        {
            if (mCarnivore.has(creatureId))
            {
                fleeFrom = mPosition.get(creatureId);

                // System.out.println(map.distance(curPos.x, curPos.y, fleeFrom.x, fleeFrom.y));

                // if a predator has just entered the field of view, we are not that much concerned;
                // if it's in the same cell as we are, we are maximally concerned
                // FIXME this is wrong we have to maximise!
                return (float) map.distance(curPos.x, curPos.y, fleeFrom.x, fleeFrom.y) / (float) sight;
            }
        }

        return 0f;
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

        // if the first predator is in the same cell, flee randomly
        // where it's open
        if (fleeFrom.x == curPos.x && fleeFrom.y == curPos.y)
        {
            direction = map.getFreeExitRandomised(curPos.x, curPos.y);
        }
        else
        {
            // otherwise, flee in the direction opposite to the predator

            direction = Side.getSideAt(curPos.x - fleeFrom.x, curPos.y - fleeFrom.y);
        }

        // System.out.println(direction);

        final float speed = mSpeed.get(entityId).value;

        return movement.moveTo(entityId, speed, direction);
    }
}
