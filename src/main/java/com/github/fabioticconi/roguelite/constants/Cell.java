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
package com.github.fabioticconi.roguelite.constants;

import com.github.fabioticconi.roguelite.utils.Util;

import java.awt.*;

/**
 * @author Fabio Ticconi
 */
public enum Cell
{
    EMPTY(),
    DEEP_WATER('=', Color.BLUE.darker().darker()),
    WATER('~', Color.BLUE.brighter().brighter()),
    SAND('.', Color.ORANGE.brighter()),
    GROUND('.', Color.ORANGE.darker()),
    GRASS(',', Color.GREEN.darker().darker()),
    HILL_GRASS('^', Color.GREEN.darker().darker()),
    HILL('^', Util.BROWN),
    MOUNTAIN('^', Util.BROWN.brighter()),
    HIGH_MOUNTAIN('^', Color.GRAY.brighter());

    public final char  c;
    public final Color col;

    Cell()
    {
        c = ' ';
        col = Color.BLACK;
    }

    Cell(final char c)
    {
        this.c = c;
        col = Color.BLACK;
    }

    Cell(final char c, final Color color)
    {
        this.c = c;
        this.col = color;
    }
}
