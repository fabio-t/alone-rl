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

import java.util.List;
import java.util.Random;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelike.components.AI;
import com.github.fabioticconi.roguelike.components.Carnivore;
import com.github.fabioticconi.roguelike.components.Fear;
import com.github.fabioticconi.roguelike.components.Herbivore;
import com.github.fabioticconi.roguelike.components.Hunger;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Sight;
import com.github.fabioticconi.roguelike.components.Speed;
import com.github.fabioticconi.roguelike.components.commands.MoveCommand;
import com.github.fabioticconi.roguelike.constants.Side;
import com.github.fabioticconi.roguelike.map.EntityGrid;
import com.github.fabioticconi.roguelike.map.Map;

/**
 *
 * @author Fabio Ticconi
 */
public class AISystem extends DelayedIteratingSystem
{
    // time, in microseconds, around which a each creature should
    // be updated here
    public static final float    BASE_TICKTIME = 5.0f;

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
    @Wire
    Map                          map;

    ComponentMapper<AI>          mAI;
    ComponentMapper<Position>    mPosition;
    ComponentMapper<Speed>       mSpeed;
    ComponentMapper<Sight>       mSight;

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

        super(Aspect.all(AI.class, Position.class, Speed.class, Sight.class));
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
        final Position pos = mPosition.get(entityId);
        final float speed = mSpeed.get(entityId).value;
        final int sight = mSight.get(entityId).value;

        float actionCooldown = 0.0f;
        float cooldownModifier = 1.0f;

        if (mHerbivore.has(entityId))
        {
            float fear = mFear.get(entityId).value;

            // FIXME this should be finer-grained: closer predators should raise fear more
            // than far away predators. The creature should also run the opposite direction of the
            // closest predator, as long as it's a free exit.

            int firstPredatorId = 0;
            final List<Integer> creatures = grid.getClosestEntities(pos.x, pos.y, sight);

            for (final int creatureId : creatures)
            {
                if (mCarnivore.has(creatureId))
                {
                    fear *= 1.1f;

                    if (firstPredatorId == 0)
                    {
                        firstPredatorId = creatureId;
                    }
                }
            }
            fear = Math.max(fear, 1.0f);

            final float hunger = mHunger.get(entityId).value;

            // fear is more important than hunger, however TERRIBLE hunger will slow the prey
            // down and might make it stay put even in front of a predator
            if (r.nextFloat() < (fear - hunger / 2f))
            {
                // FIXME flee not only in a FREE direction, but in a predator-FREE direction
                // (complicated: we have the list of creatures, ordered by circles, but we need
                // to look deeply into them to find out if there's a truly free exit somewhere)

                Side direction;

                if (firstPredatorId > 0)
                {
                    final Position predatorPos = mPosition.get(firstPredatorId);

                    // if the first predator is in the same cell, flee randomly
                    // where it's open
                    if (predatorPos.x == pos.x && predatorPos.y == pos.y)
                    {
                        direction = map.getFreeExitRandomised(pos.x, pos.y);
                    }
                    else
                    {
                        // otherwise, flee in the direction opposite to the predator

                        direction = Side.getSideAt(predatorPos.x - pos.x, predatorPos.y - pos.y);

                        System.out.println(direction);
                    }
                }
                else
                {
                    direction = map.getFreeExitRandomised(pos.x, pos.y);
                }

                actionCooldown = movement.moveTo(entityId, speed, direction);
            }

            cooldownModifier = 1.0f - fear;
        }
        else if (mCarnivore.has(entityId))
        {
            boolean chasing = false;

            // look around in a concentric spiral and stop at the entities found
            // first, ie in the closest circle (including the current position)
            final List<Integer> creatures = grid.getClosestEntities(pos.x, pos.y, sight);

            for (final int creatureId : creatures)
            {
                if (mHerbivore.has(creatureId))
                {
                    final Position preyPos = mPosition.get(creatureId);

                    chasing = true;

                    actionCooldown = movement.moveTo(entityId, speed,
                                                     Side.getSideAt(pos.x - preyPos.x, pos.y - preyPos.y));

                    break;
                }
            }

            // if there aren't any preys around, move randomly towards the first open exit
            if (!chasing)
            {
                actionCooldown = movement.moveTo(entityId, speed, map.getFreeExitRandomised(pos.x, pos.y));
            }

            final float hunger = mHunger.get(entityId).value;

            cooldownModifier = 1.0f - hunger;
        }
        else
        {
            // random walking
            actionCooldown = movement.moveTo(entityId, speed, map.getFreeExitRandomised(pos.x, pos.y));
        }

        final AI ai = mAI.get(entityId);
        ai.cooldown = (r.nextFloat() * BASE_TICKTIME + BASE_TICKTIME) * cooldownModifier;
        if (ai.cooldown < actionCooldown)
        {
            ai.cooldown = actionCooldown * 1.5f;
        }
        offerDelay(ai.cooldown);
    }
}
