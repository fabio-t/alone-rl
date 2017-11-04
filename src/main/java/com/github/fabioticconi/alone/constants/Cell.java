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
package com.github.fabioticconi.alone.constants;

import com.github.fabioticconi.alone.utils.Util;

import java.awt.*;

/**
 * @author Fabio Ticconi
 */
public enum Cell
{
    EMPTY(),
    DEEP_WATER('=', Color.BLUE.darker().darker().darker()),
    WATER('~', Color.BLUE.brighter()),
    SAND((char) 250, Color.ORANGE),
    GROUND((char) 250, Color.ORANGE.darker().darker()),
    GRASS((char) 250, Color.GREEN.darker().darker().darker()),
    HILL_GRASS('^', Color.GREEN.darker().darker().darker()),
    HILL('^', Util.BROWN),
    MOUNTAIN('^', Util.BROWN.darker()),
    HIGH_MOUNTAIN('^', Color.GRAY);

    public final char  c;
    public final Color col;
    public final Color bg;

    Cell()
    {
        this.c = ' ';
        this.col = Color.BLACK;
        this.bg = Color.BLACK;
    }

    Cell(final char c)
    {
        this.c = c;
        this.col = Color.BLACK;
        this.bg = Color.BLACK;
    }

    Cell(final char c, final Color color)
    {
        this.c = c;
        // this.col = color;
        // this.bg = Color.BLACK;
        this.col = color.darker();
        this.bg = color;
    }
}
