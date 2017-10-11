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
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.systems.BumpSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Set;

public class WanderBehaviour extends AbstractBehaviour
{
    static final Logger log = LoggerFactory.getLogger(WanderBehaviour.class);

    ComponentMapper<Position> mPosition;

    BumpSystem sBump;
    MapSystem  sMap;

    @Wire
    Random r;

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class).build(world);
    }

    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!interested(entityId))
            return 0f;

        // must simply be the "default" if no other behaviour kicks in, or if their values are really too small
        return 0.01f;
    }

    @Override
    public float update()
    {
        final Position pos = mPosition.get(entityId);

        // System.out.println(entityId + " " + pos);

        final Set<Side> exits = sMap.getFreeExits(pos.x, pos.y);

        if (exits.isEmpty())
            return 0f;

        // get one of the elements
        final int choice = r.nextInt(exits.size());

        int i = 0;
        for (final Side exit : exits)
        {
            if (i == choice)
                return sBump.bumpAction(entityId, exit);

            i++;
        }

        log.warn("size was %d and choice was %d but was never selected", exits.size(), choice);

        return 0f;
    }
}
