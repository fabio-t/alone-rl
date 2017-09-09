/**
 * Copyright 2015 Fabio Ticconi
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fabioticconi.roguelite.constants;

import java.awt.*;

/**
 * @author Fabio Ticconi
 */
public enum Cell
{
    EMPTY(' '),
    GROUND('.'),
    WALL('#'),
    OPEN_DOOR('/'),
    CLOSED_DOOR('+'),
    DEEP_WATER('=', Color.BLUE.darker()),
    WATER('=', Color.BLUE),
    SAND(',', Color.ORANGE),
    GRASS(',', Color.GREEN),
    HILL('^', Color.GREEN.darker()),
    MOUNTAIN('^', Color.ORANGE.darker()),
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
