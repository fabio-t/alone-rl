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
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelite.behaviours.Behaviour;
import com.github.fabioticconi.roguelite.components.AI;
import com.github.fabioticconi.roguelite.components.Alertness;
import com.github.fabioticconi.roguelite.components.Dead;
import com.github.fabioticconi.roguelite.components.Stamina;

import java.util.Random;

/**
 * @author Fabio Ticconi
 */
public class AISystem extends DelayedIteratingSystem
{
    // time, in millis, around which a each creature should
    // be updated here
    public static final float BASE_TICKTIME = 3.0f;

    @Wire
    Random r;

    ComponentMapper<AI>        mAI;
    ComponentMapper<Alertness> mAlert;
    ComponentMapper<Stamina>   mStamina;

    /**
     * General processing of AIs. Evaluates the best current strategy and
     * applies it.
     */
    public AISystem()
    {
        super(Aspect.all(AI.class, Alertness.class, Stamina.class).exclude(Dead.class));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#getRemainingDelay(int)
     */
    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mAI.get(entityId).cooldown;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#processDelta(int, float)
     */
    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mAI.get(entityId).cooldown -= accumulatedDelta;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#processExpired(int)
     */
    @Override
    protected void processExpired(final int entityId)
    {
        // System.out.println("eId: " + entityId);

        float actionCooldown = 0.0f;

        // Alertness can be modified by many things. It's net effect here is
        // as a "cooldown modifier", influencing how often a creature's AI ticks
        final float alertness = 1f - mAlert.get(entityId).value;

        final AI ai = mAI.get(entityId);

        final Stamina stamina = mStamina.get(entityId);

        // if we are exhausted, we'll skip this turn's AI
        if (stamina.exhausted)
        {
            ai.cooldown = (r.nextFloat() * BASE_TICKTIME + BASE_TICKTIME) * alertness;

            offerDelay(ai.cooldown);

            return;
        }

        float     maxScore      = Float.MIN_VALUE;
        Behaviour bestBehaviour = null;

        for (final Behaviour behaviour : ai.behaviours)
        {
            final float temp = behaviour.evaluate(entityId);

            // System.out.println(entityId + ": " + behaviour.getClass().getSimpleName() + " (" + temp + ")");

            if (temp > maxScore)
            {
                maxScore = temp;
                bestBehaviour = behaviour;
            }
        }

        if (bestBehaviour != null && maxScore > 0f)
        {
            System.out.println(entityId + ": " + bestBehaviour.getClass().getSimpleName() + " (" + maxScore + ")");

            actionCooldown = bestBehaviour.update();
            ai.activeBehaviour = bestBehaviour;
        }

        ai.cooldown = (r.nextFloat() * BASE_TICKTIME + BASE_TICKTIME) * alertness;

        if (ai.cooldown < actionCooldown)
        {
            ai.cooldown = actionCooldown * 1.5f;
        }

        offerDelay(ai.cooldown);
    }
}
