/**
 * Copyright 2015 Fabio Ticconi
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

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.commands.MoveCommand;
import com.github.fabioticconi.roguelike.constants.Side;
import com.github.fabioticconi.roguelike.map.EntityGrid;
import com.github.fabioticconi.roguelike.map.Map;

/**
 *
 * @author Fabio Ticconi
 */
public class MovementSystem extends DelayedIteratingSystem
{
    ComponentMapper<Position>    mPosition;
    ComponentMapper<MoveCommand> mMoveTo;

    RenderSystem                 render;

    @Wire
    Map                          map;
    @Wire
    EntityGrid                   grid;

    /**
     * @param aspect
     */
    public MovementSystem()
    {
        super(Aspect.all(Position.class, MoveCommand.class));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#getRemainingDelay(int)
     */
    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mMoveTo.get(entityId).cooldown;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#processDelta(int, float)
     */
    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mMoveTo.get(entityId).cooldown -= accumulatedDelta;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.systems.DelayedIteratingSystem#processExpired(int)
     */
    @Override
    protected void processExpired(final int entityId)
    {
        final Position p = mPosition.get(entityId);

        final MoveCommand m = mMoveTo.get(entityId);
        // we should have a flag saying "keep going":
        // if set, when the timer expires we should reset it to the
        // creature's Speed.
        // m.cooldown = speed;
        mMoveTo.remove(entityId);

        final int new_x = p.x + m.direction.x;
        final int new_y = p.y + m.direction.y;

        if (!map.isBlockedAt(new_x, new_y))
        {
            grid.moveEntity(entityId, p.x, p.y, new_x, new_y);

            p.x = new_x;
            p.y = new_y;

            render.setEnabled(true);
        }
    }

    // Public API

    public float moveTo(final int entityId, final float speed, final Side direction)
    {
        final MoveCommand m = mMoveTo.create(entityId);

        m.cooldown = speed;
        m.direction = direction;

        offerDelay(m.cooldown);

        return m.cooldown;
    }
}
