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

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.components.attributes.Sight;
import com.github.fabioticconi.alone.constants.Options;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.map.MapSystem;
import com.github.fabioticconi.alone.map.SingleGrid;
import com.github.fabioticconi.alone.utils.Coords;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point2I;
import rlforj.pathfinding.AStar;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Fabio Ticconi
 * Date: 11/10/17
 */
public class BumpSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(BumpSystem.class);

    ComponentMapper<Health>    mHealth;
    ComponentMapper<Tree>      mTree;
    ComponentMapper<Pushable>  mPushable;
    ComponentMapper<Crushable> mCrushable;
    ComponentMapper<Position>  mPos;
    ComponentMapper<Player>    mPlayer;
    ComponentMapper<Sight>     mSight;

    MapSystem map;

    ActionSystem   sAction;
    AttackSystem   sAttack;
    TreeSystem     sTree;
    PushSystem     sPush;
    CrushSystem    sCrush;
    MovementSystem sMove;

    @Wire
    SingleGrid grid;

    public float bumpAction(final int entityId, final Side direction)
    {
        if (direction.equals(Side.HERE))
            return 0f;

        final Position p = mPos.get(entityId);

        final int newX = p.x + direction.x;
        final int newY = p.y + direction.y;

        if (!map.contains(newX, newY))
            return 0f;

        ActionContext c = null;

        final int targetId = grid.get(newX, newY);

        if (targetId < 0)
        {
            c = sMove.move(entityId, direction);
            sAction.act(c);
            return c.delay;
        }

        if (mPlayer.has(entityId) && mTree.has(targetId))
        {
            c = sTree.cut(entityId, targetId);
        }
        // else if (mPlayer.has(entityId) && mPushable.has(targetId))
        // {
        //     c = sPush.push(entityId, targetId);
        // }
        else if (mPlayer.has(entityId) && mCrushable.has(targetId))
        {
            c = sCrush.crush(entityId, targetId);
        }
        else if (mHealth.has(targetId))
        {
            c = sAttack.attack(entityId, targetId);
        }

        return sAction.act(c);
    }

    public float bumpAction(final int entityId, final Position target)
    {
        final Position pos   = mPos.get(entityId);
        final Sight    sight = mSight.get(entityId);

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

        // let's give them the opportunity to plan a path even if the creature is at the border of the vision
        // but apparently "enclosed" - so we increase the radius of the grid to be explored by AStar
        final Point2I[] path = map.getPath(pos.x, pos.y, target.x, target.y, sight.value+2);

        if (path == null || path.length < 2)
        {
            // the target position is the same as the entity's position,
            // or the target is not visible. Either way, we don't move.

            log.warn("{} cannot find a path from {} to {}", entityId, pos, target);

            return 0f;
        }

        // position 0 is "HERE"
        final Point2I p = path[1];

        return bumpAction(entityId, Side.getSide(pos.x, pos.y, p.x, p.y));
    }
}
