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
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.components.attributes.Agility;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.constants.WeaponType;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.messages.ThrowMsg;
import com.github.fabioticconi.alone.utils.Coords;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point;

import java.util.EnumSet;
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

    public ActionContext throwAt(final int actorId)
    {
        final ThrowAction t = new ThrowAction();

        t.actorId = actorId;

        return t;
    }

    public class ThrowAction extends ActionContext
    {
        public List<Point> path;

        @Override
        public boolean tryAction()
        {
            final Target t = mTarget.get(actorId);

            if (t == null)
            {
                msg.send(actorId, new CannotMsg("throw", "without a target"));

                return false;
            }

            final Position p = mPos.get(actorId);

            if (p.equals(t.pos))
            {
                msg.send(actorId, new CannotMsg("throw", "here!"));

                return false;
            }

            final Inventory inventory = mInventory.get(actorId);

            if (inventory == null)
                return false;

            final int weaponId = sItem.getWeapon(actorId, EnumSet.allOf(WeaponType.class), true);

            if (weaponId < 0)
            {
                msg.send(actorId, new CannotMsg("throw", "without a weapon equipped"));

                return false;
            }

            // it's close enough to strike, so we transform this in a bump action
            if (Coords.distanceChebyshev(p.x, p.y, t.pos.x, t.pos.y) == 1)
            {
                sBump.bumpAction(actorId, Side.getSide(p.x, p.y, t.pos.x, t.pos.y));

                return false;
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

            // adding weapon
            targets.add(weaponId);

            // target position is not included
            path.add(new Point(t.pos.x, t.pos.y));

            delay = 0.5f;
            cost = 1.5f;

            msg.send(actorId, new ThrowMsg(mName.get(weaponId).name, Side.getSide(p.x, p.y, t.pos.x, t.pos.y)));

            return true;
        }

        @Override
        public void doAction()
        {
            if (targets.size() != 1)
                return;

            final int weaponId = targets.get(0);

            final Point p2 = path.get(0);

            if (map.isFree(p2.x, p2.y))
            {
                final Inventory inventory = mInventory.get(actorId);

                // we are throwing away the weapon, so we don't have it anymore
                inventory.items.removeValue(weaponId);

                // how long does it take the object to move one step?
                // TODO should be based on thrower's strength and weapon characteristics, maybe
                final float cooldown = 0.05f;

                mSpeed.create(weaponId).set(cooldown);
                mPath.create(weaponId).set(cooldown, path);
                mPos.create(weaponId).set(p2.x, p2.y);

                // it's not equipped anymore
                mEquip.remove(weaponId);

                // strength and agility of thrower are passed on to the thrown weapon.
                // effects: the weapon will hit more likely with high agility, and do more damage with high strength.
                mStrength.create(weaponId).value = mStrength.get(actorId).value;
                mAgility.create(weaponId).value = mAgility.get(actorId).value;

                // at this point it really happened: the weapon is flying at its new position.
                // it's an obstacle, so it will bump against whatever it finds
                map.obstacles.set(weaponId, p2.x, p2.y);
            }
            else
            {
                // FIXME decide on a better way to handle this
                // for now, if there's something at the first step then we don't actually throw, but
                // consider this a bump action of the THROWER.

                final Position p = mPos.get(actorId);

                sBump.bumpAction(actorId, Side.getSide(p.x, p.y, p2.x, p2.y));
            }

            sStamina.consume(actorId, cost);
        }
    }
}
