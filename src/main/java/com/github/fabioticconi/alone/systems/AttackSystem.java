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

package com.github.fabioticconi.alone.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.alone.components.Dead;
import com.github.fabioticconi.alone.components.Health;
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.Speed;
import com.github.fabioticconi.alone.components.actions.AttackAction;
import com.github.fabioticconi.alone.components.attributes.Agility;
import com.github.fabioticconi.alone.components.attributes.Skin;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Author: Fabio Ticconi
 * Date: 28/09/17
 */
public class AttackSystem extends DelayedIteratingSystem
{
    static final Logger log = LoggerFactory.getLogger(AttackSystem.class);

    ComponentMapper<AttackAction> mAttack;
    ComponentMapper<Strength>     mStrength;
    ComponentMapper<Agility>      mAgility;
    ComponentMapper<Health>       mHealth;
    ComponentMapper<Skin>         mSkin;
    ComponentMapper<Speed>        mSpeed;
    ComponentMapper<Dead>         mDead;

    @Wire
    Random r;

    StaminaSystem sStamina;

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
        final AttackAction attack = mAttack.get(entityId);

        // whatever the outcome, this action must be removed
        mAttack.remove(entityId);

        final int targetId = attack.targetId;

        if (targetId < 0)
        {
            log.info("target does not exist");
            return;
        }

        final Strength cStrength = mStrength.get(entityId);
        final Agility  cAgility  = mAgility.get(entityId);

        final Agility tAgility = mAgility.get(targetId);
        final Health  tHealth  = mHealth.get(targetId);
        final Skin    tSkin    = mSkin.get(targetId);

        // whether it hits or not, both attacker and defender get a penalty to their stamina
        // (fixed, small cost for the defender)
        final float cost = attack.cost;
        sStamina.consume(entityId, cost);
        sStamina.consume(entityId, 0.25f);

        final float toHit = Util.ensureRange((cAgility.value - tAgility.value + 4) / 8f, 0.05f, 0.95f);

        if (r.nextFloat() < toHit)
        {
            final float damage = Math.max(((cStrength.value + 2) - tSkin.value), 1f);

            tHealth.value -= damage;

            log.info("{} hits {} for D={} (H={})", entityId, targetId, damage, tHealth.value);

            if (tHealth.value <= 0)
            {
                log.info("{} is killed by {}", targetId, entityId);

                mDead.create(targetId);
            }
        }
    }

    public float attack(final int entityId, final int targetId)
    {
        final Speed cSpeed = mSpeed.get(entityId);
        final float speed  = cSpeed.value;

        final AttackAction attack = mAttack.create(entityId);

        if (attack.targetId == targetId)
        {
            // if already attacking same target, don't reset the timer
            return attack.cooldown;
        }

        // it's harder than walking

        final float cost = 1.5f;

        final float cooldown = speed * cost;

        attack.set(cooldown, targetId, cost);

        offerDelay(cooldown);

        return cooldown;
    }
}
