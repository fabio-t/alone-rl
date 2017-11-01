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

package com.github.fabioticconi.alone.messages;

import java.awt.*;

/**
 * Author: Fabio Ticconi
 * Date: 01/11/17
 */
public class ThrowMsg extends AbstractMessage
{
    public final String item;

    public ThrowMsg(final String item)
    {
        this.item = item;
    }

    @Override
    public String format()
    {
        fgCol = Color.RED;

        return String.format("%s %s %s at %s (%s)",
                             actor,
                             thirdPerson ? "throws" : "throw",
                             item.toLowerCase(),
                             target.toLowerCase(),
                             direction.name);
    }
}
