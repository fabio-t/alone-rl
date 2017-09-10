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
import com.github.fabioticconi.roguelite.Roguelite;
import com.github.fabioticconi.roguelite.components.Herbivore;
import com.github.fabioticconi.roguelite.components.Hunger;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.ItemGrid;
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
    static final Logger log = LoggerFactory.getLogger(Roguelite.class);
    ComponentMapper<Hunger>    mHunger;
    ComponentMapper<Sight>     mSight;
    ComponentMapper<Position>  mPosition;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Herbivore> mHerbivore;
    MovementSystem             sMovement;
    HungerSystem               sHunger;

    @Wire
    ItemGrid grid;
    @Wire
    Map      map;

    Position curPos;
    Position chasePos;
    int      chaseId;

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class, Hunger.class).build(world);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelite.behaviours.Behaviour#evaluate(int)
     */
    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!interested(entityId))
            return 0f;

        curPos = mPosition.get(entityId);
        final int   sight  = mSight.get(entityId).value;
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

                final float distance = (float) Coords.distanceChebyshev(curPos.x, curPos.y, temp.x, temp.y) / sight;

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
    @Override
    public float update()
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
