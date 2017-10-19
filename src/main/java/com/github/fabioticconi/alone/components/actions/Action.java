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
import com.artemis.utils.IntBag;

/**
 * Author: Fabio Ticconi
 * Date: 15/10/17
 */
public class Action extends Component
{
    public ActionContext context;
    public float         cooldown;

    @EntityId public IntBag targets;

    public Action()
    {
        
    }

    public Action(final ActionContext context, final float cooldown)
    {
        set(context, cooldown);
    }

    public void set(final ActionContext context, final float cooldown)
    {
        this.context = context;
        this.cooldown = cooldown;
    }
}
