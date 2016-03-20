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
package com.github.fabioticconi.roguelike.behaviours;

import java.util.EnumSet;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelike.components.Hunger;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Sight;
import com.github.fabioticconi.roguelike.components.Speed;
import com.github.fabioticconi.roguelike.constants.Cell;
import com.github.fabioticconi.roguelike.map.Map;
import com.github.fabioticconi.roguelike.systems.HungerSystem;

/**
 *
 * @author Fabio Ticconi
 */
public class GrazeBehaviour extends AbstractBehaviour
{
    ComponentMapper<Hunger>   mHunger;
    ComponentMapper<Sight>    mSight;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Speed>    mSpeed;

    HungerSystem              hungerSystem;

    @Wire
    Map                       map;

    Hunger                    hunger;

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class, Hunger.class).build(world);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelike.behaviours.Behaviour#evaluate(int)
     */
    @Override
    public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (notInterested(entityId))
            return 0f;

        hunger = mHunger.get(entityId);

        // 2^x - 1
        // this exponential function gives more importance to high
        // hunger values than to low hunger values
        return (float) (Math.pow(2d, hunger.value)) - 1f;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.fabioticconi.roguelike.behaviours.Behaviour#update()
     */
    @Override
    public float update()
    {
        final Position pos = mPosition.get(entityId);
        final int sight = mSight.get(entityId).value;

        final EnumSet<Cell> set = EnumSet.of(Cell.GRASS, Cell.HILL);

        final long key = map.getFirstOfType(pos.x, pos.y, sight, set);

        if (key == -1)
            return 0f;

        return hungerSystem.feed(entityId);
    }

}
