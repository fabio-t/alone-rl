/**
 * Copyright 2016 Fabio Ticconi
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
package com.github.fabioticconi.roguelike.behaviours;

/**
 * The contract of a Behaviour is two-fold: on the one side,
 * it provides a function that evaluates the conditions for
 * this behaviour, returning a score.
 *
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
