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
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.alone.behaviours.*;
import com.github.fabioticconi.alone.components.AI;
import com.github.fabioticconi.alone.components.Alertness;
import com.github.fabioticconi.alone.components.Dead;
import com.github.fabioticconi.alone.components.Stamina;

import java.util.HashMap;
import java.util.Random;

/**
 * @author Fabio Ticconi
 */
public class AISystem extends DelayedIteratingSystem
{
    // time, in millis, around which a each creature should
    // be updated here
    public static final float BASE_TICKTIME = 0.5f;

    @Wire
    Random r;

    ComponentMapper<AI>        mAI;
    ComponentMapper<Alertness> mAlert;
    ComponentMapper<Stamina>   mStamina;

    HashMap<String, Behaviour> behaviours;

    /**
     * General processing of AIs. Evaluates the best current strategy and
     * applies it.
     */
    public AISystem()
    {
        super(Aspect.all(AI.class, Alertness.class, Stamina.class).exclude(Dead.class));

        behaviours = new HashMap<>();
    }

    @Override
    protected void initialize()
    {
        behaviours.put("flee", world.getSystem(FleeBehaviour.class));
        behaviours.put("chase", world.getSystem(ChaseBehaviour.class));
        behaviours.put("fleefromaction", world.getSystem(FleeFromActionBehaviour.class));
        behaviours.put("flock", world.getSystem(FlockBehaviour.class));
        behaviours.put("graze", world.getSystem(GrazeBehaviour.class));
        behaviours.put("scavenge", world.getSystem(ScavengeBehaviour.class));
        behaviours.put("underwater", world.getSystem(UnderwaterBehaviour.class));
        behaviours.put("wander", world.getSystem(WanderBehaviour.class));
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
        final AI ai = mAI.get(entityId);
        ai.cooldown -= accumulatedDelta;
        ai.time -= accumulatedDelta; // time before the action is completed
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#processExpired(int)
     */
    @Override
    protected void processExpired(final int entityId)
    {
        // Alertness can be modified by many things. It's net effect here is
        // as a "cooldown modifier", influencing how often a creature's AI ticks
        // FIXME right now this is not used at all
        final float alertness = 1f - mAlert.get(entityId).value;

        final AI ai = mAI.get(entityId);

        if (ai.time <= 0f)
        {
            // whatever action had been issued before, it's definitely finished now,
            // so we clean this up
            ai.score = Float.MIN_VALUE;
            ai.activeBehaviour = null;
        }

        final Stamina stamina = mStamina.get(entityId);

        // if we are exhausted, we'll skip this turn's AI
        if (stamina.exhausted)
        {
            ai.cooldown = (r.nextFloat() * BASE_TICKTIME + 0.5f) * alertness;

            offerDelay(ai.cooldown);

            return;
        }

        // if any behaviour can beat the active score, it means it's urgent and we accept
        // that we might be interrupting a currently-running action
        float     maxScore      = ai.score;
        Behaviour bestBehaviour = null;

        for (final String bName : ai.behaviours)
        {
            final Behaviour behaviour = behaviours.get(bName);

            final float temp = behaviour.evaluate(entityId);

            if (temp > maxScore)
            {
                maxScore = temp;
                bestBehaviour = behaviour;
            }
        }

        // run the new behaviour and update the context
        if (bestBehaviour != null && maxScore > 0f)
        {
            // System.out.println(entityId + ": " + bestBehaviour.getClass().getSimpleName() + " (" + maxScore + ")");

            ai.time = bestBehaviour.update();
            ai.activeBehaviour = bestBehaviour;
            ai.score = maxScore;
        }

        // whatever the outcome of the above, the next tick is still randomised

        ai.cooldown = (r.nextFloat() * BASE_TICKTIME + 0.5f) * alertness;

        offerDelay(ai.cooldown);
    }
}
