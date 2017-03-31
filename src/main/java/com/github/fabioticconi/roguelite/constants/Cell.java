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

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.TextColor.RGB;

/**
 * @author Fabio Ticconi
 */
public enum Cell
{
    EMPTY, GROUND('.'), WALL('#'), OPEN_DOOR('/'), CLOSED_DOOR('+'), DEEP_WATER('=', ANSI.BLUE), WATER('=', ANSI.BLUE,
                                                                                                       SGR.BOLD), SAND(
        ',', ANSI.YELLOW, SGR.BOLD), GRASS(',', ANSI.GREEN, SGR.CROSSED_OUT), HILL('^', ANSI.GREEN), MOUNTAIN('^',
                                                                                                              ANSI.YELLOW), HIGH_MOUNTAIN(
        '^', SGR.BOLD);

    public final TextCharacter c;

    Cell()
    {
        c = new TextCharacter(' ');
    }

    Cell(final char c)
    {
        this.c = new TextCharacter(c);
    }

    Cell(final char c, final RGB color)
    {
        this.c = new TextCharacter(c).withForegroundColor(color);
    }

    Cell(final char c, final ANSI color)
    {
        this.c = new TextCharacter(c).withForegroundColor(color);
    }

    Cell(final char c, final SGR modifier)
    {
        this.c = new TextCharacter(c).withModifier(modifier);
    }

    Cell(final char c, final RGB color, final SGR modifier)
    {
        this.c = new TextCharacter(c).withForegroundColor(color).withModifier(modifier);
    }

    Cell(final char c, final ANSI color, final SGR modifier)
    {
        this.c = new TextCharacter(c).withForegroundColor(color).withModifier(modifier);
    }
}
