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
package com.github.fabioticconi.roguelite.components.actions;

import com.artemis.Component;
import com.github.fabioticconi.roguelite.constants.Side;

/**
 * @author Fabio Ticconi
 */
public class MoveAction extends Component
{
    /**
     * "Speed" is actually the delay before we can move.
     * So lowest is fastest, highest is slowest.
     */
    public float cooldown;
    public Side  direction;
    public float staminaCost;

    public MoveAction()
    {
        cooldown = 0f;
        direction = Side.HERE;
        staminaCost = 0f;
    }

    public MoveAction(final float speed, final Side direction, final float staminaCost)
    {
        this.cooldown = speed;
        this.direction = direction;
        this.staminaCost = staminaCost;
    }
}
