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

/**
 *
 * @author Fabio Ticconi
 */
public enum Side
{
    HERE(0, 0), N(0, -1), E(1, 0), S(0, 1), W(-1, 0);

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
            default:
                return HERE;
        }
    }
}
