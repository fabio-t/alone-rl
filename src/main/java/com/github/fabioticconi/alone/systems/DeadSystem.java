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
import com.artemis.systems.IteratingSystem;
import com.github.fabioticconi.alone.components.*;
import rlforj.math.Point;

import java.awt.*;

/**
 * Author: Fabio Ticconi
 * Date: 01/10/17
 */
public class DeadSystem extends IteratingSystem
{
    ComponentMapper<Position> mPos;
    ComponentMapper<Size>     mSize;
    ComponentMapper<Name>     mName;

    MapSystem map;

    public DeadSystem()
    {
        super(Aspect.all(Dead.class));
    }

    @Override
    protected void process(final int entityId)
    {
        final Position p    = mPos.get(entityId);
        final Size     size = mSize.get(entityId);
        final Name     name = mName.get(entityId);

        // remove dead creature from the world
        map.obstacles.del(p.x, p.y);
        world.delete(entityId);

        final Point p2 = map.getFirstTotallyFree(p.x, p.y, -1);

        // add corpse item
        final int        corpseId = world.create();
        final EntityEdit edit     = world.edit(corpseId);

        edit.create(Position.class).set(p2.x, p2.y);
        edit.create(Sprite.class).set('$', Color.RED.darker().darker(), false);
        edit.create(Corpse.class);
        edit.create(Health.class).set(size.value + 3);
        edit.add(new Name(name.name + "'s corpse", "corpse"));

        map.items.set(corpseId, p2.x, p2.y);
    }
}
