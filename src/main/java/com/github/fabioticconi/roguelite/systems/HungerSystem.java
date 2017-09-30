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
package com.github.fabioticconi.roguelite.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalIteratingSystem;
import com.github.fabioticconi.roguelite.components.Dead;
import com.github.fabioticconi.roguelite.components.Hunger;

/**
 * @author Fabio Ticconi
 */
public class HungerSystem extends IntervalIteratingSystem
{
    ComponentMapper<Hunger> mHunger;

    /**
     * @param interval
     */
    public HungerSystem(final float interval)
    {
        super(Aspect.all(Hunger.class).exclude(Dead.class), interval);
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

        h.value = Math.min(h.value, h.maxValue);

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
