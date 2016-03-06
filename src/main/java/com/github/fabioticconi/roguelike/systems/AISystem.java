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
package com.github.fabioticconi.roguelike.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.github.fabioticconi.roguelike.components.AI;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Speed;

/**
 *
 * @author Fabio Ticconi
 */
public class AISystem extends BaseEntitySystem
{
    // TODO: for later, maybe it's better to use a DelayedIteratingSystem:
    // each AI will have a personal cooldown, updatable in various ways.
    // when it times out, the ai kicks off. I would put it to 10-20 seconds,
    // so that things keep moving enough without clogging the server.
    // First however we do it by updating all AIs together.

    /**
     * @param aspect
     */
    public AISystem()
    {
        super(Aspect.all(AI.class, Position.class, Speed.class));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.BaseSystem#processSystem()
     */
    @Override
    protected void processSystem()
    {

    }
}
