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

import java.awt.*;

/**
 * @author Fabio Ticconi
 */
public class Sprite extends Component
{
    public char    c;
    public Color   col;
    public boolean shadowView;

    public Sprite()
    {
        this('?', Color.WHITE, false);
    }

    public Sprite(final char c)
    {
        this(c, Color.WHITE, false);
    }

    public Sprite(final char c, final Color col)
    {
        this(c, col, false);
    }

    public Sprite(final char c, final String hexCol)
    {
        this(c, Color.decode(hexCol), false);
    }

    public Sprite(final char c, final Color col, final boolean shadowView)
    {
        this.c = c;
        this.col = col;
        this.shadowView = shadowView;
    }

    public void set(final char c, final Color col)
    {
        set(c, col, false);
    }

    public void set(final char c, final Color col, final boolean shadowView)
    {
        this.c = c;
        this.col = col;
        this.shadowView = shadowView;
    }
}
