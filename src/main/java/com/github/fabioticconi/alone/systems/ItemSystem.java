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
import com.github.fabioticconi.alone.constants.WeaponType;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.messages.DropMsg;
import com.github.fabioticconi.alone.messages.EquipMsg;
import com.github.fabioticconi.alone.messages.GetMsg;
import com.github.fabioticconi.alone.screens.AbstractScreen;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point;

import java.util.EnumSet;

/**
 * Author: Fabio Ticconi
 * Date: 03/10/17
 */
public class ItemSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(ItemSystem.class);

    ComponentMapper<Position>  mPos;
    ComponentMapper<Inventory> mInventory;
    ComponentMapper<Weapon>    mWeapon;
    ComponentMapper<Equip>     mEquip;
    ComponentMapper<Wearable>  mWearable;

    MessageSystem msg;
    MapSystem     map;

    public GetAction get(final int actorId)
    {
        final GetAction a = new GetAction();

        a.actorId = actorId;

        return a;
    }

    public DropAction drop(final int actorId, final int targetId)
    {
        final DropAction a = new DropAction();

        a.actorId = actorId;
        a.targets.add(targetId);

        return a;
    }

    public EquipAction equip(final int actorId, final int targetId)
    {
        final EquipAction a = new EquipAction();

        a.actorId = actorId;
        a.targets.add(targetId);

        return a;
    }

    int getWeapon(final int entityId, final EnumSet<WeaponType> weaponTypes, final boolean onlyEquipped)
    {
        final Inventory items = mInventory.get(entityId);

        if (items == null)
            return -1;

        final int[] data = items.items.getData();
        for (int i = 0, size = items.items.size(); i < size; i++)
        {
            final int itemId = data[i];

            if (itemId < 0)
            {
                // TODO: we could flag inventory as "dirty", and then use a system for periodic cleanup.

                continue;
            }

            // we might only want an equipped weapon
            if (!mWeapon.has(itemId) || (onlyEquipped && !mEquip.has(itemId)))
                continue;

            final Weapon weapon = mWeapon.get(itemId);

            if (weaponTypes.contains(weapon.damageType))
                return itemId;
        }

        return -1;
    }

    public class GetAction extends ActionContext
    {
        @Override
        public boolean tryAction()
        {
            return true;
        }

        @Override
        public void doAction()
        {
            final Position  p = mPos.get(actorId);
            final Inventory i = mInventory.get(actorId);

            if (p == null || i == null)
            {
                log.warn("{} does not have the required composition", actorId);

                return;
            }

            if (i.items.size() >= AbstractScreen.Letter.values().length)
            {
                msg.send(actorId, new CannotMsg("get", "anything, your hands are full"));
                return;
            }

            if (map.items.isEmpty(p.x, p.y))
            {
                msg.send(actorId, new CannotMsg("get", "anything here"));
                return;
            }

            final int itemId = map.items.get(p.x, p.y);

            if (itemId < 0)
            {
                log.warn("position {} has a item with Id=", p, itemId);

                return;
            }

            map.items.del(p.x, p.y);
            i.items.add(itemId);

            mPos.remove(itemId);

            msg.send(actorId, itemId, new GetMsg());
        }
    }

    public class DropAction extends ActionContext
    {
        @Override
        public boolean tryAction()
        {
            return targets.size() == 1;
        }

        @Override
        public void doAction()
        {
            if (targets.size() != 1)
                return;

            final Position  p = mPos.get(actorId);
            final Inventory i = mInventory.get(actorId);

            if (p == null || i == null)
            {
                log.warn("{} does not have the required composition", actorId);

                return;
            }

            final int itemId = targets.get(0);

            if (i.items.removeValue(itemId))
            {
                final Point p2 = map.getFirstTotallyFree(p.x, p.y, -1);

                if (p2 == null)
                {
                    i.items.add(itemId);

                    msg.send(actorId, itemId, new CannotMsg("drop", "- there is no free space!"));

                    return;
                }

                // if it was equipped, we must remove that status
                mEquip.remove(itemId);

                map.items.set(itemId, p2.x, p2.y);

                mPos.create(itemId).set(p2.x, p2.y);

                msg.send(actorId, itemId, new DropMsg());
            }
            else
            {
                msg.send(actorId, new CannotMsg("drop", "what you don't have"));
            }
        }
    }

    private class EquipAction extends ActionContext
    {
        @Override
        public boolean tryAction()
        {
            return targets.size() == 1;
        }

        @Override
        public void doAction()
        {
            if (targets.size() != 1)
                return;

            final Position  p = mPos.get(actorId);
            final Inventory i = mInventory.get(actorId);

            if (p == null || i == null)
            {
                log.warn("{} does not have the required composition", actorId);

                return;
            }

            final int targetId = targets.get(0);

            if (!i.items.contains(targetId))
            {
                msg.send(actorId, new CannotMsg("equip", "what you don't have"));
            }
            else if (!mWearable.has(targetId))
            {
                msg.send(actorId, targetId, new CannotMsg("equip", ""));
            }
            else if (mEquip.has(targetId))
            {
                mEquip.remove(targetId);

                msg.send(actorId, targetId, new EquipMsg(true));
            }
            else
            {
                mEquip.create(targetId);

                msg.send(actorId, targetId, new EquipMsg(false));
            }
        }
    }
}
