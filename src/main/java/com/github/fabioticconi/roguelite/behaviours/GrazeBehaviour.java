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
import com.github.fabioticconi.roguelite.components.Hunger;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.constants.Cell;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.systems.HungerSystem;
import com.github.fabioticconi.roguelite.systems.MovementSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point2I;

import java.util.EnumSet;
import java.util.List;

/**
 * @author Fabio Ticconi
 */
public class GrazeBehaviour extends AbstractBehaviour
{
    static final Logger log = LoggerFactory.getLogger(GrazeBehaviour.class);

    ComponentMapper<Hunger>   mHunger;
    ComponentMapper<Sight>    mSight;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Speed>    mSpeed;

    HungerSystem   sHunger;
    MovementSystem sMovement;

    MapSystem sMap;

    // FIXME: this should be in a Context of sort
    private Hunger hunger;

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class, Hunger.class).build(world);
    }

    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!interested(entityId))
            return 0f;

        hunger = mHunger.get(entityId);

        final float value = hunger.value;

        if (value < 0.4f*hunger.maxValue)
            return 0f;

        // 2^x - 1
        // this exponential function gives more importance to high
        // hunger values than to low hunger values
        return (float) (Math.pow(2d, value)) - 1f;
    }

    @Override
    public float update()
    {
        final Position pos   = mPosition.get(entityId);
        final int      sight = mSight.get(entityId).value;
        final float    speed = mSpeed.get(entityId).value;

        // FIXME: should differentiate on the "feeding capability"
        // and also, possibly, on the creature's preference (ie, the EnumSet
        // should be within a EatingPreference component of some kind)
        final int[] coords = sMap.getFirstOfType(pos.x, pos.y, sight, EnumSet.of(Cell.GRASS, Cell.HILL_GRASS));

        // TODO: the behaviour actually FAILED here, couldn't do anything:
        // should we somehow relay this information to the AISystem, so that
        // at the next tick it can try a different thing? Otherwise we are stuck
        // in a "Graze loop"
        if (coords == null)
            return 0f;

        // we are right on a feed-friendly cell, so let's eat
        if (coords[0] == pos.x && coords[1] == pos.y)
            return sHunger.feed(entityId);

        final List<Point2I> path = sMap.getLineOfSight(pos.x, pos.y, coords[0], coords[1]);

        // TODO same as above
        if (path.size() < 2)
            return 0f;

        // position 0 is "HERE"
        final Point2I closest = path.get(1);

        // move one step towards the cell type we need
        return sMovement.moveTo(entityId, speed, Side.getSideAt(closest.x - pos.x, closest.y - pos.y));
    }
}
