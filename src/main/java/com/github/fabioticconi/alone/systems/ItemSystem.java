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
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.constants.WeaponType;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.messages.DropMsg;
import com.github.fabioticconi.alone.messages.EquipMsg;
import com.github.fabioticconi.alone.messages.GetMsg;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

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
    ComponentMapper<Armour>    mArmour;
    ComponentMapper<Name>      mName;
    ComponentMapper<Obstacle>  mObstacle;

    MessageSystem msg;
    MapSystem     map;

    @Wire
    ObjectMapper mapper;

    HashMap<String, ItemTemplate> templates;

    @Override
    protected void initialize()
    {
        try
        {
            loadTemplates();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    public HashMap<String, ItemTemplate> getTemplates()
    {
        try
        {
            loadTemplates();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }

        return templates;
    }

    public void loadTemplates() throws IOException
    {
        final InputStream fileStream = new FileInputStream("data/items.yml");

        templates = mapper.readValue(fileStream, new TypeReference<HashMap<String, ItemTemplate>>()
        {
        });

        for (final Map.Entry<String, ItemTemplate> entry : templates.entrySet())
        {
            final ItemTemplate temp = entry.getValue();
            temp.tag = entry.getKey();
        }
    }

    /**
     * It instantiates an object of the given type and places at that Point.
     *
     * @param tag
     * @param p
     * @return
     */
    public int makeItem(final String tag, final Point p)
    {
        return makeItem(tag, p.x, p.y);
    }

    /**
     * It instantiates an object of the given type and places at that position.
     *
     * @param tag
     * @param x
     * @param y
     * @return
     */
    public int makeItem(final String tag, final int x, final int y)
    {
        final int id = makeItem(tag);

        if (id < 0)
            return id;

        final Point p = map.getFirstTotallyFree(x, y, -1);

        mPos.create(id).set(p.x, p.y);

        if (mObstacle.has(id))
            map.obstacles.set(id, p.x, p.y);
        else
            map.items.set(id, p.x, p.y);

        return id;
    }

    public int makeItem(final String tag)
    {
        try
        {
            loadTemplates();

            final ItemTemplate template = templates.get(tag);

            if (template == null)
            {
                log.warn("Item named {} doesn't exist", tag);
                return -1;
            }

            final int id = world.create();

            final EntityEdit edit = world.edit(id);

            edit.add(new Name(template.name, tag));

            // TODO find a way to do this dynamically. Right now I cannot figure out a way
            // of deserialising an array of Components, because it's an abstract class that I cannot annotate.
            if (template.wearable != null)
                edit.add(template.wearable);
            if (template.weapon != null)
                edit.add(template.weapon);
            if (template.sprite != null)
                edit.add(template.sprite);
            if (template.obstacle != null)
                edit.add(template.obstacle);
            if (template.crushable != null)
                edit.add(template.crushable);
            if (template.cuttable != null)
                edit.add(template.cuttable);

            return id;
        } catch (final IOException e)
        {
            e.printStackTrace();

            return -1;
        }
    }

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

    public int getArmour(final int entityId)
    {
        return getArmour(entityId, true);
    }

    public int getArmour(final int entityId, final boolean onlyEquipped)
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
            if (!mArmour.has(itemId) || (onlyEquipped && !mEquip.has(itemId)))
                continue;

            return itemId;
        }

        return -1;
    }

    public int getItem(final int entityId, final String tag, final boolean onlyEquipped)
    {
        final Inventory items = mInventory.get(entityId);

        if (items == null)
            return -1;

        final int[] data = items.items.getData();
        for (int i = 0, size = items.items.size(); i < size; i++)
        {
            final int itemId = data[i];

            // we might only want an equipped item
            if (!mName.has(itemId) || (onlyEquipped && !mEquip.has(itemId)))
                continue;

            final Name name = mName.get(itemId);

            if (name.tag.equals(tag))
                return itemId;
        }

        return -1;
    }

    public int getWeapon(final int entityId)
    {
        return getWeapon(entityId, true);
    }

    public int getWeapon(final int entityId, final boolean onlyEquipped)
    {
        return getWeapon(entityId, EnumSet.allOf(WeaponType.class), onlyEquipped);
    }

    public int getWeapon(final int entityId, final EnumSet<WeaponType> weaponTypes, final boolean onlyEquipped)
    {
        final Inventory items = mInventory.get(entityId);

        if (items == null)
            return -1;

        final int[] data = items.items.getData();
        for (int i = 0, size = items.items.size(); i < size; i++)
        {
            final int itemId = data[i];

            // we might only want an equipped weapon
            if (!mWeapon.has(itemId) || (onlyEquipped && !mEquip.has(itemId)))
                continue;

            final Weapon weapon = mWeapon.get(itemId);

            if (weaponTypes.contains(weapon.damageType))
                return itemId;
        }

        return -1;
    }

    public static class ItemTemplate
    {
        public String name;
        public String tag;

        public Wearable  wearable;
        public Weapon    weapon;
        public Sprite    sprite;
        public Obstacle  obstacle;
        public Crushable crushable;
        public Cuttable  cuttable;
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
