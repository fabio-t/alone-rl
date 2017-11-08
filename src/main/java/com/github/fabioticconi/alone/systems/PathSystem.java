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
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.alone.components.Path;
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.Speed;
import com.github.fabioticconi.alone.constants.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point;

/**
 * Author: Fabio Ticconi
 * Date: 11/10/17
 */
public class PathSystem extends DelayedIteratingSystem
{
    static final Logger log = LoggerFactory.getLogger(PathSystem.class);

    ComponentMapper<Path>     mPath;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Position> mPos;

    MapSystem  map;
    BumpSystem sBump;

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
        final Path     path = mPath.get(entityId);
        final Position p    = mPos.get(entityId);

        if (path.i >= path.steps.size())
        {
            // for some reason the path has ended outside of us. Let's just terminate
            mPath.remove(entityId);

            log.warn("{} was moving via a Path but there are no steps left");

            return;
        }

        final Point p2   = path.steps.get(path.i++);
        final Side  side = Side.getSide(p.x, p.y, p2.x, p2.y);

        final float wait = sBump.bumpAction(entityId, side);

        // bump can remove the Path in case movement fails (eg, there's an obstacle and so we actually bump)
        if (!mPath.has(entityId))
            return;

        if (path.i == path.steps.size())
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
