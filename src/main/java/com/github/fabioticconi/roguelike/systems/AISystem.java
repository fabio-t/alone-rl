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
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Speed;
import com.github.fabioticconi.roguelike.components.commands.MoveCommand;
import com.github.fabioticconi.roguelike.constants.Side;

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

    ComponentMapper<AI>          mAI;
    ComponentMapper<MoveCommand> mMoveTo;
    ComponentMapper<Speed>       mSpeed;

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
        // random walking

        final MoveCommand m = mMoveTo.create(entityId);
        m.cooldown = mSpeed.get(entityId).speed;
        m.direction = Side.getRandom();
        movement.offerDelay(m.cooldown);

        final AI ai = mAI.get(entityId);
        ai.cooldown = (float) (r.nextGaussian() * BASE_TICKTIME) + BASE_TICKTIME;
        offerDelay(ai.cooldown);
    }
}
