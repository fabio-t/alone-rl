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
package com.github.fabioticconi.roguelike.systems;

import com.artemis.Aspect;
import com.artemis.Aspect.Builder;
import com.artemis.BaseEntitySystem;

/**
 *
 * @author Fabio Ticconi
 */
public class PlayerInputSystem extends BaseEntitySystem
{
    /**
     * @param aspect
     */
    public PlayerInputSystem(final Builder aspect)
    {
        super(Aspect.all());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.artemis.BaseSystem#processSystem()
     */
    @Override
    protected void processSystem()
    {
        // TODO:
    }
}
