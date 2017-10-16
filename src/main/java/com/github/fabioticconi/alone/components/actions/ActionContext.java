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

package com.github.fabioticconi.alone.components.actions;

import com.artemis.Component;
import com.artemis.annotations.EntityId;

/**
 * Author: Fabio Ticconi
 * Date: 15/10/17
 */
public abstract class ActionContext extends Component
{
    @EntityId
    public int actorId = -1;

    public float cost;
    public float delay;

    public ActionContext()
    {

    }

    public ActionContext set(final int actorId, final float cost, final float delay)
    {
        this.actorId = actorId;
        this.cost = cost;
        this.delay = delay;

        return this;
    }

    public ActionContext set(final int actorId, final float cost)
    {
        return set(actorId, cost, 0f);
    }

    public ActionContext set(final int actorId)
    {
        return set(actorId, 0f, 0f);
    }

    public abstract boolean tryAction();

    public abstract void doAction();

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final ActionContext that = (ActionContext) o;

        return actorId == that.actorId;
    }

    public boolean canJoin(final ActionContext context)
    {
        return equals(context);
    }
}