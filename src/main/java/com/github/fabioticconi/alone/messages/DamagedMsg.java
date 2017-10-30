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

import com.github.fabioticconi.alone.constants.Side;

/**
 * Author: Fabio Ticconi
 * Date: 29/10/17
 */
public class DamagedMsg extends AbstractMessage
{
    public final String attacker;
    public final float  dmg;
    public final float  remaining;

    public DamagedMsg(final String attacker, final float dmg, final float remaining, final int distance, final Side direction)
    {
        super(distance, direction);

        this.attacker = attacker;
        this.dmg = dmg;
        this.remaining = remaining;
    }

    @Override
    public String format()
    {
        return String.format("%s HITS you for %.2f (%.2f) (%s)", attacker, dmg, remaining, direction.toString());
    }
}
