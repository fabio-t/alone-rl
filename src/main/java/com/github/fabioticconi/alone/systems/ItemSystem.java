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
import com.github.fabioticconi.alone.components.Inventory;
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.map.MultipleGrid;
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

            final IntSet itemsHere = items.get(p.x, p.y);

            if (itemsHere.isEmpty())
                return;

            final int itemId = itemsHere.iterator().nextInt();

            if (itemId < 0)
            {
                log.warn("position {} has a item with Id=", p, itemId);

                return;
            }

            mPos.remove(itemId);

            items.del(itemId, p.x, p.y);
            i.items.add(itemId);
        }
    }

    public class DropAction extends ActionContext
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

            if (i.items.isEmpty())
                return;

            // remove the last element
            final int itemId = i.items.remove(i.items.size() - 1);

            items.add(itemId, p.x, p.y);

            mPos.create(itemId).set(p.x, p.y);
        }
    }

    public GetAction get(final int entityId)
    {
        final GetAction a = new GetAction();

        a.actorId = entityId;

        return a;
    }

    public DropAction drop(final int entityId)
    {
        final DropAction a = new DropAction();

        a.actorId = entityId;

        return a;
    }
}
