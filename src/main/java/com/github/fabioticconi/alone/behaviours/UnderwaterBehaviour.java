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
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.Speed;
import com.github.fabioticconi.alone.components.Underwater;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.constants.TerrainType;
import com.github.fabioticconi.alone.systems.BumpSystem;
import com.github.fabioticconi.alone.systems.MapSystem;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Author: Fabio Ticconi
 * Date: 17/10/17
 */
public class UnderwaterBehaviour extends AbstractBehaviour
{
    ComponentMapper<Position> mPos;
    MapSystem                 map;
    BumpSystem                sBump;

    @Wire
    Random r;

    private Position curPos;

    private final EnumSet<TerrainType> validCells = EnumSet.of(TerrainType.WATER);

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Underwater.class).build(world);
    }

    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!interested(entityId))
            return 0f;

        curPos = mPos.get(entityId);

        final MapSystem.Cell c = map.get(curPos.x, curPos.y);

        if (c.type != TerrainType.WATER)
        {
            // can't move at all if on solid ground

            return 0f;
        }

        return 0.1f; // just baseline
    }

    @Override
    public float update()
    {
        final Set<Side> exits = map.getFreeExits(curPos.x, curPos.y, validCells);

        if (exits.isEmpty())
            return 0f;

        if (exits.size() == 1)
            return sBump.bumpAction(entityId, exits.iterator().next());

        // get one of the elements
        final int choice = r.nextInt(exits.size());

        int i = 0;
        for (final Side exit : exits)
        {
            if (i == choice)
                return sBump.bumpAction(entityId, exit);

            i++;
        }

        return 0f;
    }
}
