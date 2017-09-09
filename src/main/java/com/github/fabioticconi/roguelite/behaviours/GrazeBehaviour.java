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
import com.github.fabioticconi.roguelite.components.Hunger;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.constants.Cell;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.Map;
import com.github.fabioticconi.roguelite.systems.HungerSystem;
import com.github.fabioticconi.roguelite.systems.MovementSystem;
import rlforj.math.Point2I;

import java.util.EnumSet;
import java.util.List;

/**
 * @author Fabio Ticconi
 */
public class GrazeBehaviour extends AbstractBehaviour
{
    ComponentMapper<Hunger>   mHunger;
    ComponentMapper<Sight>    mSight;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Speed>    mSpeed;

    HungerSystem   sHunger;
    MovementSystem sMovement;

    @Wire
    Map map;

    Hunger hunger;

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

        hunger = mHunger.get(entityId);

        final float value = hunger.value;

        if (value < 0.4f)
            return 0f;

        // 2^x - 1
        // this exponential function gives more importance to high
        // hunger values than to low hunger values
        return (float) (Math.pow(2d, value)) - 1f;
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
        final int      sight = mSight.get(entityId).value;
        final float    speed = mSpeed.get(entityId).value;

        // FIXME: should differentiate on the "feeding capability"
        // and also, possibly, on the creature's preference (ie, the EnumSet
        // should be within a
        // EatingPreference component of some kind)
        final int[] coords = map.getFirstOfType(pos.x, pos.y, sight, EnumSet.of(Cell.GRASS, Cell.HILL));

        // TODO: the behaviour actually FAILED here, couldn't do anything:
        // should we somehow relay this information to the AISystem, so that
        // at the next tick it can try a different thing? Otherwise we are stuck
        // in a "Graze loop"
        if (coords == null)
            return 0f;

        // System.out.println("grazing to (final dest): " + coords[0] + ", " +
        // coords[1]);

        // we are right on a feed-friendly cell, so let's eat
        if (coords[0] == pos.x && coords[1] == pos.y)
            return sHunger.feed(entityId);

        final List<Point2I> path = map.getLineOfSight(pos.x, pos.y, coords[0], coords[1]);

        // TODO same as above
        if (path.size() < 2)
            return 0f;

        // position 0 is "HERE"
        final Point2I closest = path.get(1);

        // System.out.println("grazing to: " + closest.x + ", " + closest.y);

        // move one step towards the cell type we need
        return sMovement.moveTo(entityId, speed, Side.getSideAt(closest.x - pos.x, closest.y - pos.y));
    }
}
