package com.github.fabioticconi.roguelike.systems;

import com.artemis.ComponentMapper;
import com.artemis.utils.Bag;
import com.artemis.utils.IntDeque;
import com.github.fabioticconi.roguelike.components.Group;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.mostlyoriginal.api.system.core.PassiveSystem;

public class GroupSystem extends PassiveSystem
{
    ComponentMapper<Group> mGroup;

    Bag<IntSet>            groups;

    IntDeque               recycling;

    public GroupSystem()
    {
        groups = new Bag<IntSet>();

        recycling = new IntDeque();
    }

    /**
     * Creates a new, empty group.
     *
     * @return the id of the group
     */
    public int createGroup()
    {
        final IntSet newGroup = new IntArraySet();

        int newId;
        if (recycling.isEmpty())
        {
            newId = groups.size();
        } else
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
    public IntSet removeGroup(final int groupId)
    {
        final IntSet group = groups.get(groupId);

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
    public IntSet removeGroupStrict(final int groupId)
    {
        final IntSet group = removeGroup(groupId);

        for (final int entityId : group)
        {
            mGroup.remove(entityId);
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
    public IntSet getGroup(final int groupId)
    {
        return groups.get(groupId);
    }

    /**
     * Creates a new group and adds the entity to it. Also takes care of adding
     * the Group component.
     *
     * @param entityId
     */
    public void addToGroup(final int entityId)
    {
        addToGroup(entityId, createGroup());
    }

    /**
     * Adds the entity to the specified group. Also takes care of adding the
     * Group component.
     *
     * @param entityId
     * @param groupId
     */
    public void addToGroup(final int entityId, final int groupId)
    {
        if (groups.isIndexWithinBounds(groupId))
        {
            final IntSet group = groups.get(groupId);

            group.add(entityId);

            mGroup.create(entityId).groupId = groupId;
        }

        throw new RuntimeException("groupId not valid");
    }

    /**
     * Removes the entity from the specified group. Also takes care of removing
     * the Group component.
     *
     * @param entityId
     * @param groupId
     */
    public void removeFromGroup(final int entityId, final int groupId)
    {
        if (groups.isIndexWithinBounds(groupId))
        {
            final IntSet group = groups.get(groupId);

            group.remove(entityId);

            mGroup.remove(entityId);
        }

        throw new RuntimeException("groupId not valid");
    }
}
