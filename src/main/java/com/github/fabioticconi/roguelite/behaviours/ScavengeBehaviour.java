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
import com.github.fabioticconi.roguelite.components.Corpse;
import com.github.fabioticconi.roguelite.components.Hunger;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.map.MultipleGrid;
import com.github.fabioticconi.roguelite.systems.HungerSystem;
import com.github.fabioticconi.roguelite.systems.MovementSystem;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Fabio Ticconi
 * Date: 01/10/17
 */
public class ScavengeBehaviour extends AbstractBehaviour
{
    static final Logger log = LoggerFactory.getLogger(ScavengeBehaviour.class);

    ComponentMapper<Hunger>   mHunger;
    ComponentMapper<Sight>    mSight;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Corpse>   mCorpse;

    HungerSystem   sHunger;
    MovementSystem sMovement;
    MapSystem      sMap;

    @Wire
    MultipleGrid items;

    private Position pos;
    private Position corpsePos;
    private int      corpseId;

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

        pos = mPosition.get(entityId);

        final int sight = mSight.get(entityId).value;

        final Hunger cHunger = mHunger.get(entityId);
        final float  hunger  = cHunger.value / cHunger.maxValue;

        final IntSet visibleItems = items.get(sMap.getVisibleCells(pos.x, pos.y, sight));

        corpsePos = null;

        for (final int itemId : visibleItems)
        {
            if (mCorpse.has(itemId))
            {
                corpsePos = mPosition.get(itemId);
                corpseId = itemId;

                break;
            }
        }

        if (corpsePos == null)
            return 0f;

        // wherever the corpse is, we go eat it
        return hunger;
    }

    @Override
    public float update()
    {
        if (pos.equals(corpsePos))
        {
            return sHunger.devour(entityId, corpseId);
        }

        final float speed = mSpeed.get(entityId).value;

        return sMovement.moveTo(entityId, speed, corpsePos);
    }
}
