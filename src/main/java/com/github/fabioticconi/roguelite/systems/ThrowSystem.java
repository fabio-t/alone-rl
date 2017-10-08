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
import com.github.fabioticconi.roguelite.components.Inventory;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.Weapon;
import com.github.fabioticconi.roguelite.components.actions.ThrowAction;
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.map.MultipleGrid;
import com.github.fabioticconi.roguelite.map.SingleGrid;
import com.github.fabioticconi.roguelite.utils.Coords;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point2I;

import java.util.List;

/**
 * Author: Fabio Ticconi
 * Date: 08/10/17
 */
public class ThrowSystem extends DelayedIteratingSystem
{
    static final Logger log = LoggerFactory.getLogger(ThrowSystem.class);

    ComponentMapper<ThrowAction> mThrow;
    ComponentMapper<Inventory>   mInventory;
    ComponentMapper<Speed>       mSpeed;
    ComponentMapper<Weapon>      mWeapon;
    ComponentMapper<Position>    mPosition;
    ComponentMapper<Sight>       mSight;

    MapSystem map;
    StaminaSystem sStamina;

    @Wire
    SingleGrid obstacles;

    @Wire
    MultipleGrid items;

    public ThrowSystem()
    {
        super(Aspect.all(ThrowAction.class));
    }

    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mThrow.get(entityId).cooldown;
    }

    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mThrow.get(entityId).cooldown -= accumulatedDelta;
    }

    @Override
    protected void processExpired(final int entityId)
    {
        final ThrowAction t = mThrow.get(entityId);

        final Position p = mPosition.get(entityId);

        final Point2I newP = t.path.remove(0);

        // TODO: if the new cell is not empty, then we have to handle the collision (if not a live obstacle
        // then the item simply stops there)

        // we don't use 'move' because it doesn't like when it's not present at the start position
        items.del(entityId, p.x, p.y);
        items.add(entityId, newP.x, newP.y);

        p.set(newP.x, newP.y);

        if (t.path.isEmpty())
        {
            mThrow.remove(entityId);
        }
        else
        {
            final float speed = mSpeed.get(entityId).value;

            t.cooldown = speed;

            offerDelay(speed);
        }
    }

    public float throwSomethingAtClosestEnemy(final int entityId)
    {
        final Inventory inventory = mInventory.get(entityId);

        if (inventory == null)
            return 0f;

        final int[] data = inventory.items.getData();
        for (int i = 0; i < inventory.items.size(); i++)
        {
            final int itemId = data[i];

            if (itemId < 0)
            {
                // TODO: we could flag inventory as "dirty", and then use a system for periodic cleanup.

                continue;
            }

            final Weapon weapon = mWeapon.get(itemId);

            if (weapon == null || !weapon.canThrow)
                continue;

            final Position p = mPosition.get(entityId);
            final Sight sight = mSight.get(entityId);

            // among the visible creatures, only keep the closest one
            final IntSet creatures = obstacles.getEntities(map.getVisibleCells(p.x, p.y, sight.value));
            final IntSet closest = obstacles.getClosestEntities(p.x, p.y, sight.value);
            creatures.retainAll(closest);
            final int targetId = creatures.iterator().nextInt();

            final Position targetPos = mPosition.get(targetId);

            final List<Point2I> path = map.getLineOfSight(p.x, p.y, targetPos.x, targetPos.y);

            if (path == null || path.size() < 2)
            {
                log.warn("path not found or empty");

                return 0f;
            }

            // first index is the current position
            path.remove(0);

            // FIXME: as said later, this should actually be set on entityId (ie, the thrower)
            final ThrowAction t = mThrow.create(itemId);

            // we set here the time the object will take to fly one step
            final float cooldown = 0.1f;
            mSpeed.create(itemId).set(cooldown);
            t.set(cooldown, path, 0f);

            offerDelay(cooldown);

            // the weapon must be removed from the inventory and put on the ground
            inventory.items.remove(i);
            mPosition.create(itemId).set(p.x, p.y);
            items.add(itemId, p.x, p.y);

            sStamina.consume(entityId, 1.5f);

            // FIXME: this was a lazy approach. Later, the ThrowAction will refer to the ACT OF THROWING,
            // so that the weapon is only thrown at the expiration of the cooldown.
            // At that point, the thrown object will have to be handled through a PathSystem which will be
            // akin to movement but will automatically perform steps.

            return 1.5f;
        }

        log.info("{} cannot throw: no suitable weapon", entityId);

        return 0f;
    }
}
