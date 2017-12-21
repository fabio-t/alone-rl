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
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import com.artemis.utils.IntDeque;
import com.github.fabioticconi.alone.components.Group;
import net.mostlyoriginal.api.system.core.PassiveSystem;

public class GroupSystem extends PassiveSystem
{
    ComponentMapper<Group> mGroup;

    Bag<IntBag> groups;

    IntDeque recycling;

    public GroupSystem()
    {
        groups = new Bag<>();

        recycling = new IntDeque();
    }

    /**
     * Creates a new, empty group.
     *
     * @return the id of the group
     */
    public int createGroup()
    {
        final IntBag newGroup = new IntBag(5);

        final int newId;
        if (recycling.isEmpty())
        {
            newId = groups.size();
        }
        else
        {
            newId = recycling.popFirst();
        }

        groups.set(newId, newGroup);

        return newId;
    }

    /**
     * Removes the specified group and returns it, without touching the entities
     * inside. NB: after this call, the groupId should be considered recycled by
     * other new groups; so the entities should quickly be cleared of the Group
     * component.
     *
     * @param groupId
     * @return
     */
    public IntBag removeGroup(final int groupId)
    {
        final IntBag group = groups.get(groupId);

        groups.set(groupId, null);
        recycling.add(groupId);

        return group;
    }

    /**
     * Removes the specified group and returns it, after having also cleared out
     * the Group component from each of the entities inside.
     *
     * @param groupId
     * @return
     */
    public IntBag removeGroupStrict(final int groupId)
    {
        final IntBag group = removeGroup(groupId);

        for (int i = 0, size = group.size(); i < size; i++)
        {
            mGroup.remove(group.get(i));
        }

        return group;
    }

    /**
     * Returns the specified group. Does not check boundaries, so it will throw
     * an exception if the groupId is not valid.
     *
     * @param groupId
     * @return the specified group or null
     */
    public IntBag getGroup(final int groupId)
    {
        return groups.get(groupId);
    }

    /**
     * Creates a new group and adds the entity to it. Also takes care of adding
     * the Group component.
     *
     * @param entityId
     * @return the id of the new group
     */
    public int addToGroup(final int entityId)
    {
        return addToGroup(entityId, createGroup());
    }

    /**
     * Adds the entity to the specified group. Also takes care of adding the
     * Group component.
     *
     * @param entityId
     * @param groupId
     * @return same as groupId
     */
    public int addToGroup(final int entityId, final int groupId)
    {
        final IntBag group = groups.get(groupId);

        group.add(entityId);

        mGroup.create(entityId).groupId = groupId;

        return groupId;
    }

    /**
     * Removes the entity from the specified group. If the groupIds don't match it doesn't do anything.
     * Same thing if the entity does not belong to any group.
     * Also takes care of removing the Group component.
     *
     * @param entityId
     * @param groupId
     */
    public void removeFromGroup(final int entityId, final int groupId)
    {
        final Group g = mGroup.get(entityId);

        if (g == null || g.groupId != groupId)
            return;

        final IntBag group = groups.get(groupId);

        group.remove(entityId);

        mGroup.remove(entityId);
    }

    /**
     * Removes the entity from any group it belongs to. If it doesn't belong to any group,
     * it doesn't do anything.
     * Also takes care of removing the Group component.
     *
     * @param entityId
     */
    public void removeFromGroup(final int entityId)
    {
        final Group g = mGroup.get(entityId);

        if (g == null)
            return;

        final IntBag group = groups.get(g.groupId);

        group.remove(entityId);

        mGroup.remove(entityId);
    }
}
