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
package com.github.fabioticconi.roguelike.components;

import com.artemis.Component;
import com.github.fabioticconi.roguelike.constants.Side;

/**
 *
 * @author Fabio Ticconi
 */
public class MoveTo extends Component
{
    /**
     * "Speed" is actually the delay before we can move.
     * So lowest is fastest, highest is slowest.
     */
    public float speed;
    public float cooldown;
    public Side  direction;

    public MoveTo()
    {
        speed = 0.0f;
        cooldown = 0.0f;

        direction = Side.HERE;
    }

    public MoveTo(final float speed, final Side direction)
    {
        this.speed = speed;
        cooldown = speed;

        this.direction = direction;
    }
}
