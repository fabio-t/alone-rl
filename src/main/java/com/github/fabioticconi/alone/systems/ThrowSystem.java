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
import com.github.fabioticconi.alone.components.attributes.Agility;
import com.github.fabioticconi.alone.components.attributes.Sight;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.map.MapSystem;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;
import com.github.fabioticconi.alone.utils.Coords;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Fabio Ticconi
 * Date: 08/10/17
 */
public class ThrowSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(ThrowSystem.class);

    ComponentMapper<Inventory> mInventory;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Weapon>    mWeapon;
    ComponentMapper<Position>  mPos;
    ComponentMapper<Sight>     mSight;
    ComponentMapper<Path>      mPath;
    ComponentMapper<Strength>  mStrength;
    ComponentMapper<Agility>   mAgility;

    MapSystem map;

    StaminaSystem sStamina;
    BumpSystem    sBump;

    @Wire
    SingleGrid obstacles;

    @Wire
    MultipleGrid items;

    public ThrowAction throwWeapon(final int entityId)
    {
        final ThrowAction t = new ThrowAction();

        t.actorId = entityId;

        return t;
    }

    public class ThrowAction extends ActionContext
    {
        public List<Point> path;

        @Override
        public boolean tryAction()
        {
            final Inventory inventory = mInventory.get(actorId);

            if (inventory == null)
                return false;

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

                final Position p     = mPos.get(actorId);
                final Sight    sight = mSight.get(actorId);

                // among the visible creatures, only keep the closest one
                final IntSet creatures = obstacles.getEntities(map.getVisibleCells(p.x, p.y, sight.value));
                final IntSet closest   = obstacles.getClosestEntities(p.x, p.y, sight.value);
                creatures.retainAll(closest);

                final int targetId = creatures.iterator().nextInt();

                if (targetId < 0)
                    return false;

                final Position targetPos = mPos.get(targetId);

                // it's close enough to strike
                if (Coords.distanceChebyshev(p.x, p.y, targetPos.x, targetPos.y) == 1)
                {
                    // it's only one step away, no point calculating line of sight
                    path = new ArrayList<>(1);
                }
                else
                {
                    path = map.getLineOfSight(p.x, p.y, targetPos.x, targetPos.y);

                    // first index is the current position
                    path.remove(0);

                    if (path == null || path.size() < 2)
                    {
                        log.warn("path not found or empty");

                        return false;
                    }
                }

                // adding weapon
                targets.add(itemId);

                // target position is not included
                path.add(new Point(targetPos.x, targetPos.y));

                delay = 1f;
                cost = 1.5f;

                return true;
            }

            log.info("{} cannot throw: no suitable weapon", actorId);

            return false;
        }

        @Override
        public void doAction()
        {
            if (targets.size() != 1)
                return;

            final int weaponId = targets.get(0);

            final Point newP = path.get(0);

            if (map.isFree(newP.x, newP.y))
            {
                final Inventory inventory = mInventory.get(actorId);

                // we are throwing away the weapon, so we don't have it anymore
                inventory.items.removeValue(weaponId);

                // how long does it take the object to move one step?
                // TODO should be based on thrower's strength and weapon characteristics, maybe
                final float cooldown = 0.05f;

                mSpeed.create(weaponId).set(cooldown);
                mPath.create(weaponId).set(cooldown, path);
                mPos.create(weaponId).set(newP.x, newP.y);

                // Now, it's exactly as if we were wielding the object. Later it should be more complicated,
                // eg reduce strength proportionally to length (eg, -1 each 3 or 4 steps) and set agility to
                // 0, maybe, so the victim's agility counts more.
                mStrength.create(weaponId).value = mStrength.get(actorId).value;
                mAgility.create(weaponId).value = mAgility.get(actorId).value;

                // at this point it really happened: the weapon is flying at its new position
                // obstacles.set(targetId, newP.x, newP.y);
                items.add(weaponId, newP.x, newP.y);
            }
            else
            {
                // FIXME decide on a better way to handle this
                // for now, if there's something at the first step then we don't actually throw, but
                // consider this a bump action of the THROWER.

                final Position p = mPos.get(actorId);

                sBump.bumpAction(actorId, Side.getSide(p.x, p.y, newP.x, newP.y));
            }

            sStamina.consume(actorId, 1.5f);
        }
    }
}
