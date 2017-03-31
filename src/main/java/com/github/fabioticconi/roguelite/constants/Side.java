/**
 * Copyright 2015 Fabio Ticconi
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.fabioticconi.roguelite.constants;

import java.util.Random;

/**
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
        } else if (x == 1)
        {
            if (y == -1)
                return NE;

            if (y == 1)
                return SE;

            return E;
        } else
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
