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
import com.github.fabioticconi.roguelite.components.Stamina;

/**
 * Author: Fabio Ticconi
 * Date: 03/10/17
 */
public class StaminaSystem extends IntervalIteratingSystem
{
    ComponentMapper<Stamina> mStamina;

    public StaminaSystem(final float interval)
    {
        super(Aspect.all(Stamina.class).exclude(Dead.class), interval);
    }

    @Override
    protected void process(final int entityId)
    {
        final Stamina stamina = mStamina.get(entityId);

        final float delta = 2f * getIntervalDelta();

        // TODO: stamina re-generation should be in a separated component (so this system becomes
        // a DelayedIteratingSystem) so that it can be personalised and, if needed removed)

        if (stamina.exhausted)
            stamina.exhausted = false;
        else if (stamina.value <= 0f)
            stamina.exhausted = true;

        stamina.value = Math.min(stamina.value + delta, stamina.maxValue);
    }

    public float consume(final int entityId, final float amount)
    {
        final Stamina stamina = mStamina.get(entityId);

        if (stamina == null)
            return -1f;

        // reduce stamina by a certain amount
        final float consumed = Math.min(amount, stamina.value);

        stamina.value -= consumed;

        return consumed;
    }
}
