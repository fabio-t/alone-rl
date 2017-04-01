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
import com.github.fabioticconi.roguelite.Roguelite;
import com.github.fabioticconi.roguelite.components.*;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.EntityGrid;
import com.github.fabioticconi.roguelite.map.Map;
import com.github.fabioticconi.roguelite.systems.HungerSystem;
import com.github.fabioticconi.roguelite.systems.MovementSystem;
import com.github.fabioticconi.roguelite.utils.Coords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point2I;

import java.util.List;
import java.util.Set;

/**
 * @author Fabio Ticconi
 */
public class ChaseBehaviour extends AbstractBehaviour
{
    ComponentMapper<Hunger>    mHunger;
    ComponentMapper<Sight>     mSight;
    ComponentMapper<Position>  mPosition;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Herbivore> mHerbivore;

    MovementSystem sMovement;
    HungerSystem   sHunger;

    @Wire EntityGrid grid;
    @Wire Map        map;

    Position curPos;
    Position chasePos;
    int chaseId;

    static final Logger log = LoggerFactory.getLogger(Roguelite.class);

    @Override protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class, Hunger.class).build(world);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelite.behaviours.Behaviour#evaluate(int)
     */
    @Override public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!interested(entityId))
            return 0f;

        curPos = mPosition.get(entityId);
        final int sight = mSight.get(entityId).value;
        final float hunger = mHunger.get(entityId).value;

        // all creatures in the visible area for this predator
        final Set<Integer> creatures = grid.getEntities(map.getVisibleCells(curPos.x, curPos.y, sight));

        float minDistance = Float.MAX_VALUE;

        Position temp;
        for (final int creatureId : creatures)
        {
            if (mHerbivore.has(creatureId))
            {
                temp = mPosition.get(creatureId);

                final float distance = (float)Coords.distanceChebyshev(curPos.x, curPos.y, temp.x, temp.y) / sight;

                // we want the closest prey
                if (distance < minDistance)
                {
                    minDistance = distance;
                    chasePos = temp;
                    chaseId = creatureId;
                }
            }
        }

        // might be there's no prey
        if (chaseId == 0)
            return 0f;

        // average between our hunger and the prey's closeness
        float v = 0.5f * (hunger + 1f - minDistance);

        return v;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelite.behaviours.Behaviour#update()
     */
    @Override public float update()
    {
        final Position pos   = mPosition.get(entityId);
        final float    speed = mSpeed.get(entityId).value;

        if (pos.equals(chasePos))
        {
            // prey is right here! Let's eat!
            sHunger.feed(entityId);

            world.delete(chaseId);

            return 0f;
        }

        final List<Point2I> path = map.getLineOfSight(pos.x, pos.y, chasePos.x, chasePos.y);

        // if the path is empty, it's not clear what's going on (we know the
        // prey is visible, from before..)
        // but if there's only one element, then the prey is right here and we
        // don't do anything (for now)
        if (path.isEmpty())
        {
            log.warn("path shouldn't be empty");
            return 0f;
        }

        // position 0 is "HERE"
        final Point2I closest = path.get(1);

        // System.out.println("chasing: " + closest.x + ", " + closest.y);

        // move one step towards the prey
        return sMovement.moveTo(entityId, speed, Side.getSideAt(closest.x - pos.x, closest.y - pos.y));
    }
}
