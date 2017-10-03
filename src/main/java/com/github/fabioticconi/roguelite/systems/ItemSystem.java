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

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelite.components.Inventory;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.map.MultipleGrid;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Fabio Ticconi
 * Date: 03/10/17
 */
public class ItemSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(ItemSystem.class);

    ComponentMapper<Position>  mPos;
    ComponentMapper<Inventory> mInventory;

    @Wire
    MultipleGrid items;

    /**
     * Get the first item on the ground, if any, and return its Id.
     *
     * @param entityId
     * @return
     */
    public int get(final int entityId)
    {
        final Position  p = mPos.get(entityId);
        final Inventory i = mInventory.get(entityId);

        if (p == null || i == null)
        {
            log.warn("{} does not have the required composition", entityId);

            return -1;
        }

        final IntSet itemsHere = items.get(p.x, p.y);

        if (itemsHere.isEmpty())
            return -1;

        final int itemId = itemsHere.iterator().nextInt();

        if (itemId < 0)
        {
            log.warn("position {} has a item with Id=", p, itemId);

            return -1;
        }

        items.del(itemId, p.x, p.y);
        i.items.add(itemId);

        return itemId;
    }

    /**
     * Drop the first item on the ground, if you have any, and return its Id.
     *
     * @param entityId
     * @return
     */
    public int drop(final int entityId)
    {
        final Position  p = mPos.get(entityId);
        final Inventory i = mInventory.get(entityId);

        if (p == null || i == null)
        {
            log.warn("{} does not have the required composition", entityId);

            return -1;
        }

        if (i.items.isEmpty())
            return -1;

        // remove the last element
        final int itemId = i.items.remove(i.items.size() - 1);

        items.add(itemId, p.x, p.y);

        return itemId;
    }
}
