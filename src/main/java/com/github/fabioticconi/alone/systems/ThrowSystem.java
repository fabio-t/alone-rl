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
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ThrowAction;
import com.github.fabioticconi.alone.components.attributes.Agility;
import com.github.fabioticconi.alone.components.attributes.Sight;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.map.MapSystem;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;
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
    ComponentMapper<Position>    mPos;
    ComponentMapper<Sight>       mSight;
    ComponentMapper<Path>        mPath;
    ComponentMapper<Strength>    mStrength;
    ComponentMapper<Agility>     mAgility;

    MapSystem     map;

    StaminaSystem sStamina;
    BumpSystem    sBump;

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

        mThrow.remove(entityId);

        final int targetId = t.targetId;

        // something might have happened to the item..
        if (targetId < 0)
        {
            log.warn("item being thrown by {} has disappeared", entityId);
            return;
        }

        final Point2I newP = t.path.get(0);

        if (map.isFree(newP.x, newP.y))
        {
            final Inventory inventory = mInventory.get(entityId);

            // we are throwing away the weapon, so we don't have it anymore
            inventory.items.removeValue(targetId);

            // how long does it take the object to move one step?
            // TODO should be based on thrower's strength and weapon characteristics, maybe
            final float cooldown = 0.1f;

            mSpeed.create(targetId).set(cooldown);
            mPath.create(targetId).set(cooldown, t.path);
            mPos.create(targetId).set(newP.x, newP.y);

            // Now, it's exactly as if we were wielding the object. Later it should be more complicated,
            // eg reduce strength proportionally to length (eg, -1 each 3 or 4 steps) and set agility to
            // 0, maybe, so the victim's agility counts more.
            mStrength.create(targetId).value = mStrength.get(entityId).value;
            mAgility.create(targetId).value = mAgility.get(entityId).value;

            // at this point it really happened: the weapon is flying at its new position
            // obstacles.set(targetId, newP.x, newP.y);
            items.add(targetId, newP.x, newP.y);
        }
        else
        {
            // FIXME decide on a better way to handle this
            // for now, if there's something at the first step then we don't actually throw, but
            // consider this a bump action of the THROWER.

            final Position p = mPos.get(entityId);

            sBump.bumpAction(entityId, Side.getSide(p.x, p.y, newP.x, newP.y));
        }

        sStamina.consume(entityId, 1.5f);
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

            final Position p     = mPos.get(entityId);
            final Sight    sight = mSight.get(entityId);

            // among the visible creatures, only keep the closest one
            final IntSet creatures = obstacles.getEntities(map.getVisibleCells(p.x, p.y, sight.value));
            final IntSet closest   = obstacles.getClosestEntities(p.x, p.y, sight.value);
            creatures.retainAll(closest);
            final int targetId = creatures.iterator().nextInt();

            final Position targetPos = mPos.get(targetId);

            final List<Point2I> path = map.getLineOfSight(p.x, p.y, targetPos.x, targetPos.y);

            if (path == null || path.size() < 2)
            {
                log.warn("path not found or empty");

                return 0f;
            }

            // first index is the current position
            path.remove(0);

            final ThrowAction t = mThrow.create(entityId);

            final float cooldown = 1f;

            t.set(cooldown, itemId, 1.5f, path);

            offerDelay(cooldown);

            return cooldown;
        }

        log.info("{} cannot throw: no suitable weapon", entityId);

        return 0f;
    }
}
