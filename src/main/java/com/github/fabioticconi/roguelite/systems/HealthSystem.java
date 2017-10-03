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
import com.github.fabioticconi.roguelite.components.Health;

/**
 * Author: Fabio Ticconi
 * Date: 02/10/17
 */
public class HealthSystem extends IntervalIteratingSystem
{
    ComponentMapper<Health> mHealth;

    public HealthSystem(final float interval)
    {
        super(Aspect.all(Health.class).exclude(Dead.class), interval);
    }

    @Override
    protected void process(final int entityId)
    {
        final Health health = mHealth.get(entityId);

        health.value = health.value + getIntervalDelta() * 0.1f;

        health.value = Math.min(health.value, health.maxValue);
    }

    public float consume(final int entityId, final float fraction)
    {
        final Health health = mHealth.get(entityId);

        // reduce stamina by a certain amount
        final float consumed = Math.min(health.maxValue * fraction, health.value);

        health.value -= consumed;

        return consumed;
    }
}
