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

import com.github.fabioticconi.roguelite.components.Position;
import rlforj.math.Point2I;

import java.util.List;

/**
 * Author: Fabio Ticconi
 * Date: 08/10/17
 */
public class ThrowAction extends DelayedAction
{
    public List<Point2I> path;

    public ThrowAction()
    {
        set(0f, null, 0f);
    }

    public ThrowAction(final float cooldown, final List<Point2I> path, final float cost)
    {
        set(cooldown, path, cost);
    }

    public void set(final float cooldown, final List<Point2I> path, final float cost)
    {
        set(cooldown, -1, cost);

        this.path = path;
    }
}
