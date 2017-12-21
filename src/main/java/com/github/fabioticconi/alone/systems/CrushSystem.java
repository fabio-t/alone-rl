/*
 * Copyright (C) 2015-2017 Fabio Ticconi
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
import com.github.fabioticconi.alone.components.Crushable;
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.Speed;
import com.github.fabioticconi.alone.components.Weapon;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.constants.WeaponType;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.messages.CrushMsg;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

/**
 * Author: Fabio Ticconi
 * Date: 07/10/17
 */
public class CrushSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(CrushSystem.class);

    ComponentMapper<Crushable> mCrush;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Strength>  mStr;
    ComponentMapper<Position>  mPos;
    ComponentMapper<Weapon>    mWeapon;

    StaminaSystem sStamina;
    ItemSystem    sItem;
    MessageSystem msg;
    MapSystem     map;

    public CrushAction crush(final int entityId, final int targetId)
    {
        final CrushAction c = new CrushAction();

        c.actorId = entityId;

        c.targets.add(targetId);

        return c;
    }

    public class CrushAction extends ActionContext
    {
        @Override
        public boolean tryAction()
        {
            if (targets.size() != 1)
                return false;

            final int targetId = targets.get(0);

            if (targetId < 0 || !mCrush.has(targetId))
                return false;

            final int hammerId = sItem.getWeapon(actorId, EnumSet.of(WeaponType.BLUNT), true);

            if (hammerId < 0)
            {
                msg.send(actorId, targetId, new CannotMsg("crush", "without a blunt weapon"));

                return false;
            }

            final Weapon weapon = mWeapon.get(hammerId);

            if (weapon.damage < 3)
            {
                msg.send(actorId, targetId, new CannotMsg("crush", "with such a weak weapon"));

                return false;
            }

            targets.add(hammerId);

            // FIXME further adjust delay and cost using the hammer power
            delay = mSpeed.get(actorId).value;
            cost = delay / (mStr.get(actorId).value + 3f);

            return true;
        }

        @Override
        public void doAction()
        {
            if (targets.size() != 2)
                return;

            final int targetId = targets.get(0);

            msg.send(actorId, targetId, new CrushMsg());

            final Position p = mPos.get(targetId);

            // from a tree we get a trunk and two branches
            map.obstacles.del(p.x, p.y);
            world.delete(targetId);

            for (int i = 0; i < 3; i++)
            {
                sItem.makeItem("stone", p.x, p.y);
            }

            // consume a fixed amount of stamina
            sStamina.consume(actorId, cost);
        }
    }
}
