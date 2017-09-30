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
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelite.components.Dead;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.components.actions.AttackAction;
import com.github.fabioticconi.roguelite.components.attributes.Health;
import com.github.fabioticconi.roguelite.components.attributes.Strength;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Fabio Ticconi
 * Date: 28/09/17
 */
public class AttackSystem extends DelayedIteratingSystem
{
    static final Logger log = LoggerFactory.getLogger(AttackSystem.class);

    ComponentMapper<AttackAction> mAttack;
    ComponentMapper<Strength>     mStrength;
    ComponentMapper<Health>       mHealth;
    ComponentMapper<Speed>        mSpeed;
    ComponentMapper<Dead>         mDead;

    public AttackSystem()
    {
        super(Aspect.all(Position.class, AttackAction.class).exclude(Dead.class));
    }

    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mAttack.get(entityId).cooldown;
    }

    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mAttack.get(entityId).cooldown -= accumulatedDelta;
    }

    @Override
    protected void processExpired(final int entityId)
    {
        final AttackAction cAttack   = mAttack.get(entityId);
        final Strength     cStrength = mStrength.get(entityId);

        // whatever the outcome, this action must be removed
        mAttack.remove(entityId);

        final int targetId = cAttack.targetId;

        if (targetId < 0)
        {
            log.info("target does not exist");
            return;
        }

        final Health cHealth = mHealth.get(targetId);

        if (cStrength == null || cHealth == null)
        {
            log.error("wrong entity composition");

            return;
        }

        final int   strength = cStrength.value;
        final float health   = cHealth.value;

        cHealth.value = health - strength*2;

        log.info("E:{} T:{} H:{}", entityId, targetId, cHealth.value);

        if (cHealth.value <= 0)
        {
            log.info("entity {} is killed by {}", targetId, entityId);

            mDead.create(entityId);
        }
    }

    public float attack(final int entityId, final int targetId)
    {
        final Speed cSpeed = mSpeed.get(entityId);
        final float speed  = cSpeed.value;

        final AttackAction cAttack = mAttack.create(entityId);

        if (cAttack.targetId == targetId)
        {
            // if already attacking same target, don't reset the timer
            return cAttack.cooldown;
        }

        cAttack.targetId = targetId;
        cAttack.cooldown = speed;

        offerDelay(cAttack.cooldown);

        return speed;
    }
}
