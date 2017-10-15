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
import com.artemis.annotations.EntityId;
import com.github.fabioticconi.alone.components.Crushable;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import net.mostlyoriginal.api.system.core.PassiveSystem;

/**
 * Author: Fabio Ticconi
 * Date: 07/10/17
 */
public class CrushSystem extends PassiveSystem
{
    ComponentMapper<Crushable> mCrushable;

    public CrushAction crush(final int entityId, final int targetId)
    {
        final CrushAction c = new CrushAction();

        c.actorId = entityId;
        c.targetId = targetId;

        return c;
    }

    public class CrushAction extends ActionContext
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
            return super.equals(o) && targetId == ((CrushAction) o).targetId;
        }
    }
}
