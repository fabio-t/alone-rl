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
import com.github.fabioticconi.roguelite.components.Herbivore;
import com.github.fabioticconi.roguelite.components.Hunger;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.map.SingleGrid;
import com.github.fabioticconi.roguelite.systems.BumpSystem;
import com.github.fabioticconi.roguelite.utils.Coords;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fabio Ticconi
 */
public class ChaseBehaviour extends AbstractBehaviour
{
    static final Logger log = LoggerFactory.getLogger(ChaseBehaviour.class);

    ComponentMapper<Hunger>    mHunger;
    ComponentMapper<Sight>     mSight;
    ComponentMapper<Position>  mPosition;
    ComponentMapper<Herbivore> mHerbivore;

    BumpSystem sBump;
    MapSystem  sMap;

    @Wire
    SingleGrid grid;

    private Position chasePos;

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

        final Position pos   = mPosition.get(entityId);
        final int      sight = mSight.get(entityId).value;

        final Hunger cHunger = mHunger.get(entityId);
        final float  hunger  = cHunger.value / cHunger.maxValue; // need a value between 0 and 1

        // System.out.println(entityId + " " + curPos);

        // all creatures in the visible area for this predator
        final IntSet creatures = grid.getEntities(sMap.getVisibleCells(pos.x, pos.y, sight));

        float minDistance = Float.MAX_VALUE;
        chasePos = null;

        Position temp;
        for (final int creatureId : creatures)
        {
            if (mHerbivore.has(creatureId))
            {
                temp = mPosition.get(creatureId);

                final float distance = (float) Coords.distanceChebyshev(pos.x, pos.y, temp.x, temp.y);

                // we want the closest prey
                if (distance < minDistance)
                {
                    minDistance = distance;
                    chasePos = temp;
                }
            }
        }

        // might be there's no prey
        if (chasePos == null)
            return 0f;

        // average between our hunger and the prey's closeness
        return 0.5f * (hunger + 1f - (minDistance / sight));
    }

    @Override
    public float update()
    {
        return sBump.bumpAction(entityId, chasePos);
    }
}
