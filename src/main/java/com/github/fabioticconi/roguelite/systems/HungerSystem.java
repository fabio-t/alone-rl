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
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalIteratingSystem;
import com.github.fabioticconi.roguelite.components.Dead;
import com.github.fabioticconi.roguelite.components.Health;
import com.github.fabioticconi.roguelite.components.Hunger;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.map.MultipleGrid;

/**
 * @author Fabio Ticconi
 */
public class HungerSystem extends IntervalIteratingSystem
{
    ComponentMapper<Hunger>   mHunger;
    ComponentMapper<Health>   mHealth;
    ComponentMapper<Position> mPosition;

    @Wire
    MultipleGrid items;

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

        // increase hunger a little bit every tick
        h.value = h.value + getIntervalDelta() * 0.01f;

        h.value = Math.min(h.value, h.maxValue);
    }

    // Public API

    // TODO this should be dependent on "interval" or on some value as argument
    public float feed(final int entityId)
    {
        if (!mHunger.has(entityId))
            return 0f;

        final Hunger h = mHunger.get(entityId);

        // remove 25% hunger
        h.value = h.value - h.maxValue * 0.25f;

        h.value = Math.max(h.value, 0f);

        return h.value;
    }

    public float devour(final int entityId, final int foodId)
    {
        final Hunger h = mHunger.get(entityId);

        if (h == null)
            return 0f;

        final Health health = mHealth.get(foodId);

        if (health == null)
            return 0f;

        // remove 25% hunger (or less) and decrease corpse health accordingly

        final float food = Math.min(h.maxValue * 0.25f, h.value);

        h.value -= food;
        health.value -= food;

        System.out.println(entityId + " eats " + food + " (hunger: " + h.value + ")");

        if (health.value <= 0f)
        {
            // destroy food item, but also recover the wrongly-reduced hunger

            h.value -= health.value;

            final Position p = mPosition.get(foodId);
            items.del(foodId, p.x, p.y);
            world.delete(foodId);
        }

        return food;
    }
}
