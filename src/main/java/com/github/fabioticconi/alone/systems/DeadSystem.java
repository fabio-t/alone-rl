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

package com.github.fabioticconi.alone.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;

import java.awt.*;

/**
 * Author: Fabio Ticconi
 * Date: 01/10/17
 */
public class DeadSystem extends IteratingSystem
{
    ComponentMapper<Position> mPos;
    ComponentMapper<Size>     mSize;

    @Wire
    SingleGrid obstacles;

    @Wire
    MultipleGrid items;

    public DeadSystem()
    {
        super(Aspect.all(Dead.class));
    }

    @Override
    protected void process(final int entityId)
    {
        final Position p    = mPos.get(entityId);
        final Size     size = mSize.get(entityId);

        // remove dead creature from the world
        obstacles.del(p.x, p.y);
        world.delete(entityId);

        // add corpse item
        final int        corpseId = world.create();
        final EntityEdit edit     = world.edit(corpseId);

        edit.create(Position.class).set(p.x, p.y);
        edit.create(Sprite.class).set('$', Color.RED.darker().darker(), false);
        edit.create(Corpse.class);
        edit.create(Health.class).set(size.value + 3);

        items.add(corpseId, p.x, p.y);
    }
}
