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
import com.github.fabioticconi.roguelite.components.Group;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.map.SingleGrid;
import com.github.fabioticconi.roguelite.systems.GroupSystem;
import com.github.fabioticconi.roguelite.systems.MovementSystem;
import com.github.fabioticconi.roguelite.utils.Coords;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlockBehaviour extends AbstractBehaviour
{
    static final Logger log = LoggerFactory.getLogger(FlockBehaviour.class);

    ComponentMapper<Sight>    mSight;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Group>    mGroup;

    MovementSystem sMovement;
    GroupSystem    sGroup;
    MapSystem      sMap;

    @Wire
    SingleGrid grid;

    private Position curPos;
    private Position centerOfGroup;
    private Position closest;

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class, Group.class).build(world);

        centerOfGroup = new Position(0, 0);
    }

    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!interested(entityId))
            return 0f;

        final int sight = mSight.get(entityId).value;

        if (sight == 0)
            return 0f;

        final int    groupId = mGroup.get(entityId).groupId;
        final IntSet members = sGroup.getGroup(groupId);

        if (members.size() < 2)
            return 0f;

        curPos = mPosition.get(entityId);

        final LongSet visibleCells = sMap.getVisibleCells(curPos.x, curPos.y, sight);

        final IntSet creatures = grid.getEntities(visibleCells);

        centerOfGroup.x = 0;
        centerOfGroup.y = 0;

        int minDistance = Integer.MAX_VALUE;

        int      count = 0;
        Position temp;
        for (final int memberId : members)
        {
            if (creatures.contains(memberId) && memberId != entityId)
            {
                temp = mPosition.get(memberId);

                if (temp == null)
                {
                    continue;
                }

                centerOfGroup.x += temp.x;
                centerOfGroup.y += temp.y;

                // we keep track of the closest visible group member, in case the "center of group" approach
                // fails
                final int tempDistance = Coords.distanceChebyshev(curPos.x, curPos.y, temp.x, temp.y);

                if (tempDistance < minDistance)
                {
                    minDistance = tempDistance;
                    closest = temp;
                }

                count++;
            }
        }

        if (count == 0)
            return 0f;

        centerOfGroup.x = Math.floorDiv(centerOfGroup.x, count);
        centerOfGroup.y = Math.floorDiv(centerOfGroup.y, count);

        // if we are at the centre already, this behaviour should not be selected
        if (centerOfGroup.x == curPos.x && centerOfGroup.y == curPos.y)
            return 0f;

        final long coord = Coords.packCoords(centerOfGroup.x, centerOfGroup.y);

        // if the center is not currently visible, there's no point
        if (!visibleCells.contains(coord))
        {
            centerOfGroup.set(closest);
        }

        final int dist = Coords.distanceChebyshev(curPos.x, curPos.y, centerOfGroup.x, centerOfGroup.y);

        if (dist < 2)
            return 0f;

        return (float) dist / sight;
    }

    @Override
    public float update()
    {
        final float speed = mSpeed.get(entityId).value;

        return sMovement.moveTo(entityId, speed, centerOfGroup);
    }
}
