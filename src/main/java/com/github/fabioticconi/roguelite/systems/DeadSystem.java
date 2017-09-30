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

package com.github.fabioticconi.roguelite.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.github.fabioticconi.roguelite.components.Dead;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.map.SingleGrid;

/**
 * Author: Fabio Ticconi
 * Date: 01/10/17
 */
public class DeadSystem extends IteratingSystem
{
    ComponentMapper<Position> mPos;

    @Wire
    SingleGrid grid;

    public DeadSystem()
    {
        super(Aspect.all(Dead.class));
    }

    @Override
    protected void process(final int entityId)
    {
        // TODO: either create a new entity (corpse item) or change it
        // the latter has the problem that the Ids might have been stored somewhere (eg, AttackAction
        // for example stores the target id - what if somebody killed it before us?) and by not removing the entity
        // we may have an inconsistent state.

        // if we delete the entity, then artemis-odb should "notify" interested systems, most importantly
        // the link manager. That one changes dead entities to -1, so that elsewhere in the code I just have to check
        // that

        final Position p = mPos.get(entityId);

        grid.del(p.x, p.y);

        world.delete(entityId);
    }
}
