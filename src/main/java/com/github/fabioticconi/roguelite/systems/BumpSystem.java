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
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelite.PushSystem;
import com.github.fabioticconi.roguelite.components.*;
import com.github.fabioticconi.roguelite.components.actions.BumpAction;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.map.SingleGrid;
import com.github.fabioticconi.roguelite.utils.Coords;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import rlforj.math.Point2I;

import java.util.List;

/**
 * Author: Fabio Ticconi
 * Date: 11/10/17
 */
public class BumpSystem extends DelayedIteratingSystem
{
    ComponentMapper<BumpAction> mBump;
    ComponentMapper<Health>     mHealth;
    ComponentMapper<Tree>       mTree;
    ComponentMapper<Pushable>   mPushable;
    ComponentMapper<Position>   mPos;
    ComponentMapper<Player>     mPlayer;

    MapSystem map;

    AttackSystem   sAttack;
    TreeSystem     sTree;
    PushSystem     sPush;
    MovementSystem sMove;

    @Wire
    SingleGrid grid;

    public BumpSystem()
    {
        super(Aspect.all(BumpAction.class));
    }

    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mBump.get(entityId).cooldown;
    }

    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mBump.get(entityId).cooldown -= accumulatedDelta;
    }

    @Override
    protected void processExpired(final int entityId)
    {
        // TODO: actually run the action?

        // FIXME: something to remember: if STARTING an action changed state, we cannot roll back
        // in case this bump action is interrupted.
        // (incidentally, we need interrupting behaviour)
    }

    public float bumpAction(final int entityId, final Side direction)
    {
        if (direction.equals(Side.HERE))
            return 0f;

        final Position p = mPos.get(entityId);

        final int newX = p.x + direction.x;
        final int newY = p.y + direction.y;

        if (map.isFree(newX, newY))
        {
            return sMove.moveTo(entityId, direction);
        }

        final int targetId = grid.get(newX, newY);

        if (targetId < 0)
        {
            // there's no creature/wall but the cell itself is inaccessible
            return 0f;
        }

        if (mPlayer.has(entityId) && mTree.has(targetId))
        {
            return sTree.cut(entityId, targetId);
        }
        else if (mPlayer.has(entityId) && mPushable.has(targetId))
        {
            return sPush.push(entityId, targetId);
        }
        else if (mHealth.has(targetId))
        {
            return sAttack.attack(entityId, targetId);
        }

        return 0f;
    }

    public float bumpAction(final int entityId, final Position target)
    {
        final Position pos = mPos.get(entityId);

        if (pos.equals(target))
        {
            // we cannot bump "here"
            return 0f;
        }

        if (Coords.distanceChebyshev(pos.x, pos.y, target.x, target.y) == 1)
        {
            // it's only one step away, no point calculating line of sight
            return bumpAction(entityId, Side.getSide(pos.x, pos.y, target.x, target.y));
        }

        final List<Point2I> path = map.getLineOfSight(pos.x, pos.y, target.x, target.y);

        if (path == null || path.size() < 2)
        {
            // the target position is the same as the entity's position,
            // or the target is not visible. Either way, we don't move.

            return 0f;
        }

        // position 0 is "HERE"
        final Point2I p = path.get(1);

        return bumpAction(entityId, Side.getSide(pos.x, pos.y, p.x, p.y));
    }
}
