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
package com.github.fabioticconi.roguelike.systems;

import java.util.Random;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelike.behaviours.Behaviour;
import com.github.fabioticconi.roguelike.components.AI;

/**
 *
 * @author Fabio Ticconi
 */
public class AISystem extends DelayedIteratingSystem
{
    // time, in microseconds, around which a each creature should
    // be updated here
    public static final float BASE_TICKTIME = 3.0f;

    @Wire
    Random                    r;

    ComponentMapper<AI>       mAI;

    /**
     * General processing of AIs. Evaluates the best current strategy and
     * applies it.
     */
    public AISystem()
    {
        super(Aspect.all(AI.class));
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

        // FIXME re-evaluate if we need cooldownModifier
        // it should be behaviour-specific or an entity-specific,
        // or both..
        final float cooldownModifier = 1.0f;

        final AI ai = mAI.get(entityId);

        float maxScore = 0f;
        Behaviour bestBehaviour = null;

        for (final Behaviour behaviour : ai.behaviours)
        {
            final float temp = behaviour.evaluate(entityId);

            System.out.println(entityId + ": " + behaviour.getClass().getSimpleName() + " (" + temp + ")");

            if (temp > maxScore)
            {
                maxScore = temp;
                bestBehaviour = behaviour;
            }
        }

        if (bestBehaviour != null && maxScore > 0f)
        {
            // System.out.println(entityId + ": " +
            // bestBehaviour.getClass().getSimpleName() + " (" + maxScore +
            // ")");

            actionCooldown = bestBehaviour.update();
            ai.activeBehaviour = bestBehaviour;
        }

        ai.cooldown = (r.nextFloat() * BASE_TICKTIME + BASE_TICKTIME) * cooldownModifier;
        if (ai.cooldown < actionCooldown)
        {
            ai.cooldown = actionCooldown * 1.5f;
        }
        offerDelay(ai.cooldown);
    }
}
