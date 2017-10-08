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

package com.github.fabioticconi.roguelite.components.actions;

import com.artemis.Component;
import com.artemis.annotations.EntityId;

/**
 * Author: Fabio Ticconi
 * Date: 08/10/17
 */
public abstract class DelayedAction extends Component
{
    public float cooldown;

    @EntityId
    public int targetId;

    public float cost;

    public DelayedAction()
    {
        set(0f, -1, 0f);
    }

    public DelayedAction(final float cooldown, final int targetId, final float cost)
    {
        set(cooldown, targetId, cost);
    }

    public void set(final float cooldown, final int targetId, final float cost)
    {
        this.cooldown = cooldown;
        this.targetId = targetId;
        this.cost = cost;
    }
}
