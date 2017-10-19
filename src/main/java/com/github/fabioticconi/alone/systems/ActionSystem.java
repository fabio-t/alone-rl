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
import com.artemis.ComponentType;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.alone.components.Dead;
import com.github.fabioticconi.alone.components.actions.Action;
import com.github.fabioticconi.alone.components.actions.ActionContext;

/**
 * Author: Fabio Ticconi
 * Date: 15/10/17
 */
public class ActionSystem extends DelayedIteratingSystem
{
    ComponentMapper<Action> mAction;

    public ActionSystem()
    {
        super(Aspect.all(Action.class).exclude(Dead.class));
    }

    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mAction.get(entityId).cooldown;
    }

    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mAction.get(entityId).cooldown -= accumulatedDelta;
    }

    @Override
    protected void processExpired(final int entityId)
    {
        final Action a = mAction.get(entityId);

        a.context.doAction();

        mAction.remove(entityId);
    }

    public float act(final ActionContext context)
    {
        if (context == null)
        {
            // TODO log warning
            return 0f;
        }

        final int actorId = context.actorId;

        if (actorId < 0)
        {
            // TODO log warning
            return 0f;
        }

        Action a = mAction.get(actorId);

        if (a != null)
        {
            if (a.context.equals(context))
            {
                // we are trying to do exactly the same action so we just stop here

                return a.cooldown;
            }
            else
            {
                mAction.remove(actorId);
            }
        }

        final boolean tryAct = context.tryAction();

        if (!tryAct)
        {
            // FIXME log something?

            return 0f;
        }

        if (context.delay <= 0f)
        {
            // do it now, don't even create a persistent Action
            context.doAction();
        }
        else
        {
            a = mAction.create(actorId);

            a.targets = context.targets;

            // inside the context there's the original delay, but we copy it instead of directly using that
            // so that we can decrease without modifying the context (useful if we need to know how much
            // time has already passed if something, eg an interruption, happens)
            a.set(context, context.delay);
        }

        return context.delay;
    }
}
