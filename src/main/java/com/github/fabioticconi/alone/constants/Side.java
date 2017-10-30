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

import java.util.Random;

/**
 * @author Fabio Ticconi
 */
public enum Side
{
    HERE(0, 0, "here"),
    N(0, -1, "north"),
    NE(1, -1, "northeast"),
    E(1, 0, "east"),
    SE(1, 1, "southeast"),
    S(0, 1, "south"),
    SW(-1, 1, "southwest"),
    W(-1, 0, "west"),
    NW(-1, -1, "northwest");

    public final int x;
    public final int y;
    public final String name;

    Side(final int x, final int y, final String name)
    {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public static Side getSide(final int fromX, final int fromY, final int toX, final int toY)
    {
        return getSideAt(toX - fromX, toY - fromY);
    }

    public static Side getSideAt(int x, int y)
    {
        x = Math.max(Math.min(x, 1), -1);
        y = Math.max(Math.min(y, 1), -1);

        if (x == 0)
        {
            if (y == -1)
                return N;

            if (y == 1)
                return S;

            return HERE;
        }
        else if (x == 1)
        {
            if (y == -1)
                return NE;

            if (y == 1)
                return SE;

            return E;
        }
        else
        {
            if (y == -1)
                return NW;

            if (y == 1)
                return SW;

            return W;
        }
    }

    public static Side getRandom()
    {
        final Random r = new Random();

        final int x = r.nextInt(3) - 1; // -1, 0 or 1
        final int y = r.nextInt(3) - 1;

        return getSideAt(x, y);
    }

    public Side inverse()
    {
        switch (this)
        {
            case N:
                return S;
            case E:
                return W;
            case S:
                return N;
            case W:
                return E;
            case NE:
                return SW;
            case SE:
                return NW;
            case SW:
                return NE;
            case NW:
                return SE;
            default:
                return HERE;
        }
    }
}
