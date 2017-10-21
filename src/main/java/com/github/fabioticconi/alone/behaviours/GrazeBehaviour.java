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
package com.github.fabioticconi.alone.behaviours;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.github.fabioticconi.alone.components.Hunger;
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.Speed;
import com.github.fabioticconi.alone.components.attributes.Sight;
import com.github.fabioticconi.alone.constants.Cell;
import com.github.fabioticconi.alone.map.MapSystem;
import com.github.fabioticconi.alone.systems.ActionSystem;
import com.github.fabioticconi.alone.systems.BumpSystem;
import com.github.fabioticconi.alone.systems.HungerSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

/**
 * @author Fabio Ticconi
 */
public class GrazeBehaviour extends AbstractBehaviour
{
    static final Logger log = LoggerFactory.getLogger(GrazeBehaviour.class);

    ComponentMapper<Hunger>   mHunger;
    ComponentMapper<Sight>    mSight;
    ComponentMapper<Position> mPosition;

    HungerSystem sHunger;
    BumpSystem   sBump;
    ActionSystem sAction;

    MapSystem map;

    // FIXME: this should be in a Context of sort
    private Hunger hunger;

    private EnumSet<Cell> validCells = EnumSet.of(Cell.GRASS, Cell.HILL_GRASS);

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

        final float value = hunger.value / hunger.maxValue;

        if (value < 0.4f)
            return 0f;

        return value;
    }

    @Override
    public float update()
    {
        final Position pos   = mPosition.get(entityId);
        final int      sight = mSight.get(entityId).value;

        // FIXME: does not take visibility into account!

        // FIXME: should differentiate on the "feeding capability"
        // and also, possibly, on the creature's preference (ie, the EnumSet
        // should be within a EatingPreference component of some kind)
        final int[] coords = map.getFirstOfType(pos.x, pos.y, sight, validCells);

        // TODO: the behaviour actually FAILED here, couldn't do anything:
        // should we somehow relay this information to the AISystem, so that
        // at the next tick it can try a different thing? Otherwise we are stuck
        // in a "Graze loop"
        // FIXME: solution: if no visible grass cells, evaluate this behaviour to 0
        if (coords == null)
            return 0f;

        // we are right on a feed-friendly cell, so let's eat
        if (coords[0] == pos.x && coords[1] == pos.y)
            return sAction.act(sHunger.feed(entityId));

        final Position destination = new Position(coords[0], coords[1]);

        return sBump.bumpAction(entityId, destination);
    }
}
