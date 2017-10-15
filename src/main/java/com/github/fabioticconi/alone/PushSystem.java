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

package com.github.fabioticconi.alone;

import com.artemis.ComponentMapper;
import com.artemis.annotations.EntityId;
import com.github.fabioticconi.alone.components.Pushable;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import net.mostlyoriginal.api.system.core.PassiveSystem;

/**
 * Author: Fabio Ticconi
 * Date: 07/10/17
 */
public class PushSystem extends PassiveSystem
{
    ComponentMapper<Pushable> mPushable;

    public PushAction push(final int entityId, final int toPushId)
    {
        final PushAction p = new PushAction();

        p.actorId = entityId;
        p.targetId = toPushId;

        return p;
    }

    public class PushAction extends ActionContext
    {
        @EntityId
        public int targetId = -1;

        @Override
        public boolean tryAction()
        {
            return false;
        }

        @Override
        public void doAction()
        {

        }

        @Override
        public boolean equals(final Object o)
        {
            return super.equals(o) && targetId == ((PushAction) o).targetId;
        }
    }
}
