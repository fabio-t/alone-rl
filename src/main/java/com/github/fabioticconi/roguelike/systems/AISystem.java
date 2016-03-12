/**
 * Copyright 2016 Fabio Ticconi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fabioticconi.roguelike.systems;

import java.util.Random;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelike.components.AI;
import com.github.fabioticconi.roguelike.components.Carnivore;
import com.github.fabioticconi.roguelike.components.Herbivore;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Speed;
import com.github.fabioticconi.roguelike.components.commands.MoveCommand;
import com.github.fabioticconi.roguelike.components.internal_states.Fear;
import com.github.fabioticconi.roguelike.components.internal_states.Hunger;
import com.github.fabioticconi.roguelike.constants.Side;
import com.github.fabioticconi.roguelike.map.EntityGrid;

/**
 *
 * @author Fabio Ticconi
 */
public class AISystem extends DelayedIteratingSystem
{
    // time, in microseconds, around which a each creature should
    // be updated here
    public static final float    BASE_TICKTIME = 5000000.0f;

    // TODO: later, the behaviour should be data-driven,
    // with behaviour trees or something similar;
    // at that point, this system will load the active entities'
    // script and evalute if their current behaviour needs to be changed or not.
    // Also, this should receive messages that could influence the AIs, and make
    // according changes to the entities' AI state (ie, interrupt a "food searching" behaviour
    // for a "flee enemy at all costs" behaviour)

    @Wire
    Random                       r;
    @Wire
    EntityGrid                   grid;

    ComponentMapper<AI>          mAI;
    ComponentMapper<Position>    mPosition;
    ComponentMapper<Speed>       mSpeed;

    ComponentMapper<MoveCommand> mMoveTo;
    ComponentMapper<Herbivore>   mHerbivore;
    ComponentMapper<Carnivore>   mCarnivore;
    ComponentMapper<Hunger>      mHunger;
    ComponentMapper<Fear>        mFear;

    MovementSystem               movement;

    /**
     *
     */
    public AISystem()
    {
        // TODO: this should only require AI, as some behaviours might not be
        // linked to position/movement, of course.
        // We'll see when we get there.

        super(Aspect.all(AI.class, Position.class, Speed.class));
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
        float actionCooldown = 0.0f;
        float cooldownModifier = 1.0f;

        if (mHerbivore.has(entityId))
        {
            float fear = mFear.get(entityId).value;

            // FIXME this should be finer-grained: closer predators should raise fear more
            // than far away predators. For now it's fine.
            final Position pos = mPosition.get(entityId);
            for (final int creatureId : grid.getEntitiesWithinRadius(pos.x, pos.y, 3))
            {
                if (mCarnivore.has(creatureId))
                {
                    fear += 0.1f;
                }
            }

            final float hunger = mHunger.get(entityId).value;

            // fear is more important than hunger, however TERRIBLE hunger will slow the prey
            // down and might make it stay put even in front of a predator
            if (r.nextFloat() < (fear - hunger / 2f))
            {
                // FIXME flee in a FREE direction, not randomly!

                actionCooldown = randomWalk(entityId);
            }

            cooldownModifier = 1.0f - fear;
        }
        else if (mCarnivore.has(entityId))
        {

        }
        else
        {
            // random walking
            actionCooldown = randomWalk(entityId);
        }

        final AI ai = mAI.get(entityId);
        ai.cooldown = (r.nextFloat() * BASE_TICKTIME / 1000.0f + BASE_TICKTIME) * cooldownModifier;
        if (ai.cooldown < actionCooldown)
        {
            ai.cooldown = actionCooldown * 2.0f;
        }
        offerDelay(ai.cooldown);
    }

    // AI FUNCTIONS
    private float randomWalk(final int entityId)
    {
        final MoveCommand m = mMoveTo.create(entityId);
        m.cooldown = mSpeed.get(entityId).speed;
        m.direction = Side.getRandom();
        movement.offerDelay(m.cooldown);

        return m.cooldown;
    }
}
