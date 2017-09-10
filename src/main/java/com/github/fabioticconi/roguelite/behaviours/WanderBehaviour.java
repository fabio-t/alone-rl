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
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.Map;
import com.github.fabioticconi.roguelite.systems.MovementSystem;

public class WanderBehaviour extends AbstractBehaviour
{
    ComponentMapper<Position> mPosition;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Sight>    mSight;

    MovementSystem sMovement;

    @Wire
    Map map;

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

        return 0.2f;
    }

    @Override
    public float update()
    {
        final Position pos   = mPosition.get(entityId);
        final Sight    sight = mSight.getSafe(entityId, null);
        final float    speed = mSpeed.get(entityId).value;

        final Side direction;

        if (sight == null || sight.value == 0)
        {
            direction = Side.getRandom();
        }
        else
        {
            direction = map.getFreeExitRandomised(pos.x, pos.y);
        }

        if (direction == Side.HERE)
            return 0f;

        // FIXME this should be normalised in a way that the return cooldown is always dependent on
        // some specific property (ie, "alertness") which can change for various reasons - but outside the behaviours.
        // this would mean that this update function wouldn't return anything.
        return sMovement.moveTo(entityId, speed, direction);
    }
}
