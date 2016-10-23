/**
 * Copyright 2016 Fabio Ticconi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.fabioticconi.roguelite.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalIteratingSystem;
import com.github.fabioticconi.roguelite.components.Hunger;

/**
 *
 * @author Fabio Ticconi
 */
public class HungerSystem extends IntervalIteratingSystem
{
    ComponentMapper<Hunger> mHunger;

    /**
     * @param aspect
     * @param interval
     */
    public HungerSystem(final float interval)
    {
        super(Aspect.all(Hunger.class), interval);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.IntervalIteratingSystem#process(int)
     */
    @Override
    protected void process(final int entityId)
    {
        final Hunger h = mHunger.get(entityId);

        h.value *= 1.01f;

        h.value = Math.min(h.value, 1.0f);

        // TODO: we need a Feeding component, triggered by a player action
        // or by the AI, that tells us if we have food available - in which
        // case,
        // hunger should decrease, not grow
    }

    // Public API

    // TODO this should be dependent on "interval" or on some delta as argument
    public float feed(final int entityId)
    {
        if (!mHunger.has(entityId))
            return 0f;

        final Hunger h = mHunger.get(entityId);

        h.value *= 0.9f;

        return h.value;
    }
}
