/**
 * Copyright 2015 Fabio Ticconi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fabioticconi.roguelike.constants;

import java.util.Random;

/**
 *
 * @author Fabio Ticconi
 */
public enum Side
{
    HERE(0, 0), N(0, -1), NE(1, -1), E(1, 0), SE(1, 1), S(0, 1), SW(-1, 1), W(-1, 0), NW(-1, -1);

    public final int x;
    public final int y;

    Side(final int x, final int y)
    {
        this.x = x;
        this.y = y;
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

    public static Side getSideAt(int x, int y)
    {
        x = Math.max(Math.min(x, 1), -1);
        y = Math.max(Math.min(x, 1), -1);

        if (x == 0)
        {
            if (y == 0)
                return HERE;

            if (y == -1)
                return N;

            if (y == 1)
                return S;
        }
        else if (x == 1)
            return E;
        else
            return W;

        // should never happen thanks to the first two lines
        return null;
    }

    public static Side getRandom()
    {
        final Random r = new Random();

        int x = r.nextInt(3) - 1; // -1, 0 or 1
        int y = r.nextInt(3) - 1;

        if (x == 1 && y == 1)
        {
            x = y = 0;
        }

        return getSideAt(x, y);
    }
}
