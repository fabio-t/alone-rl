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
import com.artemis.annotations.Wire;
import com.github.fabioticconi.alone.components.Carnivore;
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.Speed;
import com.github.fabioticconi.alone.components.attributes.Sight;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.map.MapSystem;
import com.github.fabioticconi.alone.map.SingleGrid;
import com.github.fabioticconi.alone.systems.BumpSystem;
import com.github.fabioticconi.alone.utils.Coords;
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
    ComponentMapper<Carnivore> mCarnivore; // FIXME make a more generic FleeFrom

    BumpSystem sBump;

    MapSystem sMap;

    @Wire
    SingleGrid grid;

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

        // System.out.println(entityId + " " + curPos);

        final IntSet creatures = grid.getEntities(sMap.getVisibleCells(curPos.x, curPos.y, sight));

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
        Side direction = Side.getSideAt(curPos.x - fleeFrom.x, curPos.y - fleeFrom.y);

        if (!sMap.isFree(curPos.x, curPos.y, direction))
        {
            // go to a random direction, whether free or not!
            // note how this could result in animals killing members of their own group and such,
            // which we take as simulating a stampede.

            direction = Side.getRandom();
        }

        return sBump.bumpAction(entityId, direction);
    }
}
