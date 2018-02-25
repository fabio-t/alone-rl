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

package com.github.fabioticconi.alone.messages;

import com.github.fabioticconi.alone.constants.Side;

import java.awt.*;

/**
 * Author: Fabio Ticconi
 * Date: 01/11/17
 */
public class ThrowMsg extends AbstractMessage
{
    public final String item;
    public final Side   at;

    public ThrowMsg(final String item, final Side at)
    {
        this.item = item;
        this.at = at;
    }

    @Override
    public String format()
    {
        fgCol = Color.RED;

        return String.format("%s %s %s towards %s",
                             actor,
                             thirdPerson ? "throws" : "throw",
                             item.toLowerCase(),
                             at.toString());
    }
}
