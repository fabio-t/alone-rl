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

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelike.components.Herbivore;
import com.github.fabioticconi.roguelike.components.Hunger;
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
public class ChaseBehaviour extends PassiveSystem implements Behaviour
{
    ComponentMapper<Hunger>    mHunger;
    ComponentMapper<Sight>     mSight;
    ComponentMapper<Position>  mPosition;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Herbivore> mHerbivore;

    MovementSystem             movement;

    @Wire
    EntityGrid                 grid;
    @Wire
    Map                        map;

    int                        entityId;
    Position                   curPos;
    Position                   chase;

    Aspect                     aspect;

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.BaseSystem#initialize()
     */
    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class, Hunger.class).build(world);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelike.behaviours.Behaviour#evaluate(int)
     */
    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!aspect.isInterested(world.getEntity(entityId)))
            return 0f;

        curPos = mPosition.get(entityId);
        final int sight = mSight.get(entityId).value;

        final Set<Integer> creatures = grid.getEntitiesWithinRadius(curPos.x, curPos.y, sight);

        for (final int creatureId : creatures)
        {
            if (mHerbivore.has(creatureId))
            {
                chase = mPosition.get(creatureId);

                final float hunger = mHunger.get(entityId).value;

                return 0.5f * hunger +
                       0.5f * (1f - (float) map.distance(curPos.x, curPos.y, chase.x, chase.y) / (float) sight);
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
        final float speed = mSpeed.get(entityId).value;

        final Side direction = Side.getSideAt(chase.x - curPos.x, chase.y - curPos.y);

        return movement.moveTo(entityId, speed, direction);
    }
}
