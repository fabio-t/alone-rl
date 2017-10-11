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
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelite.components.Path;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.MapSystem;
import rlforj.math.Point2I;

/**
 * Author: Fabio Ticconi
 * Date: 11/10/17
 */
public class PathSystem extends DelayedIteratingSystem
{
    ComponentMapper<Path>  mPath;
    ComponentMapper<Speed> mSpeed;

    MapSystem map;

    MovementSystem sMove;
    BumpSystem     sBump;

    public PathSystem()
    {
        super(Aspect.all(Position.class, Speed.class, Path.class));
    }

    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mPath.get(entityId).cooldown;
    }

    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mPath.get(entityId).cooldown -= accumulatedDelta;
    }

    @Override
    protected void processExpired(final int entityId)
    {
        final Path    path      = mPath.get(entityId);
        final Point2I newP      = path.steps.remove(0);
        final Side    direction = Side.getSideAt(newP.x, newP.y);

        final float wait = sBump.bumpAction(entityId, direction);

        // FIXME: if wait is zero, it usually means the bump failed somehow. If that's true than we need
        // to stop path-moving. Maybe we should reserve -1 for when the bump fails?

        if (path.steps.isEmpty())
        {
            // we arrived!
            mPath.remove(entityId);
        }
        else
        {
            final float speed = mSpeed.get(entityId).value;

            path.cooldown = Math.max(speed, wait);

            offerDelay(speed);
        }
    }
}
