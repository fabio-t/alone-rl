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
package com.github.fabioticconi.roguelite.behaviours;

/**
 * The contract of a Behaviour is two-fold: on the one side,
 * it provides a function that evaluates the conditions for
 * this behaviour, returning a score.
 * <p>
 * On the other side, it must also provide an update function
 * that will be called each tick by entities using this behaviour.
 *
 * @author Fabio Ticconi
 */
public interface Behaviour
{
    /**
     * Evaluates the conditions for this behaviour.
     *
     * @return a [0,1] score representing the utility of this behaviour.
     */
    public float evaluate(int entityId);

    /**
     * Implements this behaviour. Will be called every tick.
     *
     * @return the minimum time the next AI tick can happen
     */
    public float update();
}
