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

package com.github.fabioticconi.alone.components;

import com.artemis.Component;
import rlforj.math.Point;

import java.util.List;

/**
 * Author: Fabio Ticconi
 * Date: 11/10/17
 */
public class Path extends Component
{
    public float         cooldown;
    public List<Point> steps;

    public Path()
    {

    }

    public Path(final float cooldown, final List<Point> steps)
    {
        set(cooldown, steps);
    }

    public void set(final float cooldown, final List<Point> steps)
    {
        this.cooldown = cooldown;
        this.steps = steps;
    }
}
