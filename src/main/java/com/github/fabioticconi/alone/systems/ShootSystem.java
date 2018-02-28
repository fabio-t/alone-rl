/*
 * Copyright (C) 2015-2018 Fabio Ticconi
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
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.components.attributes.Agility;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.constants.WeaponType;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.messages.ShootMsg;
import com.github.fabioticconi.alone.utils.Coords;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point;

import java.util.EnumSet;
import java.util.List;

/**
 * Author: Fabio Ticconi
 * Date: 28/02/18
 */
public class ShootSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(ThrowSystem.class);

    ComponentMapper<Inventory> mInventory;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Target>    mTarget;
    ComponentMapper<Position>  mPos;
    ComponentMapper<Path>      mPath;
    ComponentMapper<Strength>  mStrength;
    ComponentMapper<Agility>   mAgility;
    ComponentMapper<Name>      mName;
    ComponentMapper<Equip>     mEquip;

    StaminaSystem sStamina;
    BumpSystem    sBump;
    ItemSystem    sItem;
    MessageSystem msg;
    MapSystem     map;

    public ActionContext shootAt(final int actorId)
    {
        final ShootAction a = new ShootAction();

        a.actorId = actorId;

        return a;
    }

    public class ShootAction extends ActionContext
    {
        public List<Point> path;

        @Override
        public boolean tryAction()
        {
            final Target t = mTarget.get(actorId);

            if (t == null)
            {
                msg.send(actorId, new CannotMsg("shoot", "without a target"));

                return false;
            }

            final Position p = mPos.get(actorId);

            if (p.equals(t.pos))
            {
                msg.send(actorId, new CannotMsg("shoot", "here!"));

                return false;
            }

            final Inventory inventory = mInventory.get(actorId);

            if (inventory == null)
                return false;

            // FIXME: bow is not a weapon, and arrows are a special type of weapon so we can't use this
            // function

            final int weaponId = sItem.getWeapon(actorId, EnumSet.allOf(WeaponType.class), true);

            if (weaponId < 0)
            {
                msg.send(actorId, new CannotMsg("shoot", "without a weapon equipped"));

                return false;
            }

            // shooting point-blank
            if (Coords.distanceChebyshev(p.x, p.y, t.pos.x, t.pos.y) == 1)
            {
                // TODO the arrow must be shot
            }
            else
            {
                path = map.getLineOfSight(p.x, p.y, t.pos.x, t.pos.y);

                if (path == null || path.size() < 2)
                {
                    log.warn("path not found or empty");

                    return false;
                }

                // first index is the current position
                path.remove(0);
            }

            // TODO: add only arrow
            targets.add(weaponId);

            // target position is not included
            path.add(new Point(t.pos.x, t.pos.y));

            delay = 0.5f;
            cost = 1.5f;

            msg.send(actorId, new ShootMsg(mName.get(weaponId).name, Side.getSide(p.x, p.y, t.pos.x, t.pos.y)));

            return true;
        }

        @Override
        public void doAction()
        {
            if (targets.size() != 1)
                return;

            final int arrowId = targets.get(0);

            final Point p2 = path.get(0);

            if (map.isFree(p2.x, p2.y))
            {
                final Inventory inventory = mInventory.get(actorId);

                // we are throwing away the arrow, so we don't have it anymore
                inventory.items.removeValue(arrowId);

                // how long does it take the arrow to move one step?
                final float cooldown = 0.01f; // faster than throwing

                mSpeed.create(arrowId).set(cooldown);
                mPath.create(arrowId).set(cooldown, path);
                mPos.create(arrowId).set(p2.x, p2.y);

                // strength and agility of shooter are passed on to arrow.
                // effects: the arrow will hit more likely with high agility, and do more damage with high strength.
                mStrength.create(arrowId).value = mStrength.get(actorId).value;
                mAgility.create(arrowId).value = mAgility.get(actorId).value;

                // at this point it really happened: the weapon is flying at its new position.
                // it's an obstacle, so it will bump against whatever it finds
                map.obstacles.set(arrowId, p2.x, p2.y);
            }
            else
            {
                // TODO: must shoot at point-blank
            }

            sStamina.consume(actorId, cost);
        }
    }
}
