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

import com.artemis.ComponentMapper;
import com.github.fabioticconi.roguelite.components.Dead;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.attributes.Health;
import com.github.fabioticconi.roguelite.components.attributes.Strength;
import net.mostlyoriginal.api.system.core.PassiveSystem;

/**
 * Author: Fabio Ticconi
 * Date: 28/09/17
 */
public class AttackSystem extends PassiveSystem
{
    ComponentMapper<Strength> mStrength;
    ComponentMapper<Health>   mHealth;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Dead>     mDead;

    public float attack(final int entityId, final int targetId)
    {
        final Speed    cSpeed    = mSpeed.get(entityId);
        final Strength cStrength = mStrength.get(entityId);
        final Health   cHealth   = mHealth.get(targetId);

        if (cSpeed == null || cStrength == null || cHealth == null)
        {
            // TODO log a warning or error

            return 0f;
        }

        final int   strength = cStrength.value;
        final float health   = cHealth.value;

        // TODO implement dodge, plus we need to account for the player weapons eventually

        cHealth.value = health - strength;

        if (cHealth.value <= 0)
        {
            // TODO log info?
            mDead.create(targetId);

            // FIXME maybe it's better to simply remove the entity here, and create a corpse item instead.
        }

        return cSpeed.value;
    }
}
