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

import java.awt.*;

/**
 * Author: Fabio Ticconi
 * Date: 04/11/17
 */
public class Msg extends AbstractMessage
{
    final String msg;

    public Msg(final String msg)
    {
        this.msg = msg;
    }

    @Override
    public String format()
    {
        if (!"You".equals(actor))
            fgCol = Color.GRAY;

        return String.format("%s %s", actor, msg);
    }
}
