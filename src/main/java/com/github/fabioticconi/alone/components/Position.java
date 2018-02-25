/*
 * Copyright (C) 2015-2017 Fabio Ticconi
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
import com.github.fabioticconi.alone.constants.Side;

/**
 * @author Fabio Ticconi
 */
public class Position extends Component
{
    public int x;
    public int y;

    public Position()
    {

    }

    public Position(final int x, final int y)
    {
        this.x = x;
        this.y = y;
    }

    public Position set(final int x, final int y)
    {
        this.x = x;
        this.y = y;

        return this;
    }

    public Position set(final Side side)
    {
        return set(side.x, side.y);
    }

    public Position set(final Position p)
    {
        return set(p.x, p.y);
    }

    public Position add(final int x, final int y)
    {
        this.x += x;
        this.y += y;

        return this;
    }

    public Position add(final Side side)
    {
        return add(side.x, side.y);
    }

    public Position add(final Position p)
    {
        return add(p.x, p.y);
    }

    @Override
    public String toString()
    {
        return String.format("(%d,%d)", x, y);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final Position position = (Position) o;

        if (x != position.x)
            return false;
        return y == position.y;
    }

    @Override
    public int hashCode()
    {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
