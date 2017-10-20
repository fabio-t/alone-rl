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
import com.artemis.annotations.EntityId;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalIteratingSystem;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.utils.Coords;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fabio Ticconi
 */
public class HungerSystem extends IntervalIteratingSystem
{
    static final Logger log = LoggerFactory.getLogger(HungerSystem.class);

    ComponentMapper<Hunger>   mHunger;
    ComponentMapper<Health>   mHealth;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Corpse>   mCorpse;

    @Wire
    MultipleGrid items;

    public HungerSystem(final float interval)
    {
        super(Aspect.all(Hunger.class).exclude(Dead.class), interval);
    }

    @Override
    protected void process(final int entityId)
    {
        final Hunger h = mHunger.get(entityId);

        // increase hunger a little bit every tick
        h.value = h.value + getIntervalDelta() * 0.01f;

        h.value = Math.min(h.value, h.maxValue);
    }

    public EatAction devour(final int entityId, final int corpseId)
    {
        if (!mCorpse.has(corpseId))
        {
            log.warn("{} is trying to eat a not-corpse, {}", entityId, corpseId);

            return null;
        }

        final EatAction a = new EatAction();

        a.actorId = entityId;

        a.targets.add(corpseId);

        a.delay = 1f;

        return a;
    }

    public EatAction devourClosestCorpse(final int entityId)
    {
        final Position p = mPosition.get(entityId);

        final IntSet itemsClose = items.getWithinRadius(p.x, p.y, 1);

        for (final int foodId : itemsClose)
        {
            if (mCorpse.has(foodId))
            {
                final EatAction a = new EatAction();

                a.actorId = entityId;

                a.targets.add(foodId);

                a.delay = 1f;
                a.cost = 0.5f;

                return a;
            }
        }

        return null;
    }

    public FeedAction feed(final int entityId)
    {
        final FeedAction a = new FeedAction();

        a.actorId = entityId;

        a.delay = 1f;
        a.cost = 0.5f;

        return a;
    }

    public class EatAction extends ActionContext
    {
        @Override
        public boolean tryAction()
        {
            if (targets.size() != 1)
                return false;

            final int targetId = targets.get(0);

            if (targetId < 0)
                return false;

            final Position p1 = mPosition.get(actorId);
            final Position p2 = mPosition.get(targetId);

            if (Coords.distanceChebyshev(p1.x, p1.y, p2.x, p2.y) < 2 && mCorpse.has(targetId))
                return true;

            return false;
        }

        @Override
        public void doAction()
        {
            if (targets.size() != 1)
                return;

            final Hunger h = mHunger.get(actorId);

            if (h == null)
                return;

            final int targetId = targets.get(0);

            final Health health = mHealth.get(targetId);

            if (health == null)
                return;

            // remove 25% hunger (or less) and decrease food health accordingly

            final float food = Math.min(h.maxValue * 0.25f, h.value);

            h.value -= food;
            health.value -= food;

            if (health.value <= 0f)
            {
                // destroy food item, but also recover the wrongly-reduced hunger

                h.value -= health.value;

                final Position p = mPosition.get(targetId);
                items.del(targetId, p.x, p.y);
                world.delete(targetId);
            }
        }
    }

    public class FeedAction extends ActionContext
    {
        @Override
        public boolean tryAction()
        {
            return true;
        }

        @Override
        public void doAction()
        {
            if (!mHunger.has(actorId))
                return;

            final Hunger h = mHunger.get(actorId);

            // remove 25% hunger
            final float food = Math.min(h.maxValue * 0.25f, h.value);

            h.value -= food;
        }
    }
}
