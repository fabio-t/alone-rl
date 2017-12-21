/*
 * Copyright (C) 2015-2017 Fabio Ticconi
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

package com.github.fabioticconi.alone.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalIteratingSystem;
import com.github.fabioticconi.alone.components.Dead;
import com.github.fabioticconi.alone.components.Health;

/**
 * Author: Fabio Ticconi
 * Date: 02/10/17
 */
public class HealthSystem extends IntervalIteratingSystem
{
    ComponentMapper<Health> mHealth;
    ComponentMapper<Dead>   mDead;

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

    public float damage(final int entityId, final float fraction)
    {
        final Health health = mHealth.get(entityId);

        // reduce health by a certain amount
        final float consumed = health.maxValue * fraction;

        health.value -= consumed;

        if (health.value <= 0f)
            mDead.create(entityId);

        return consumed;
    }
}
