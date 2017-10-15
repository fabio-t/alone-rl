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

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.constants.Cell;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.map.MapSystem;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fabio Ticconi
 */
public class MovementSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(MovementSystem.class);

    ComponentMapper<Position>   mPosition;
    ComponentMapper<Speed>      mSpeed;
    ComponentMapper<Obstacle>   mObstacle;

    MapSystem     sMap;
    StaminaSystem sStamina;

    @Wire
    SingleGrid grid;

    @Wire
    MultipleGrid items;

    public class MoveAction extends ActionContext
    {
        public Side direction = Side.HERE;

        @Override
        public boolean tryAction()
        {
            if (direction.equals(Side.HERE))
                return false;

            final Position p     = mPosition.get(actorId);
            final Speed    speed = mSpeed.get(actorId);

            final int newX = p.x + direction.x;
            final int newY = p.y + direction.y;

            if (!sMap.isFree(newX, newY))
                return false;

            final Cell cell = sMap.get(newX, newY);

            switch (cell)
            {
                case HILL:
                case HILL_GRASS:
                    cost = 1.25f;

                    break;

                case MOUNTAIN:
                    cost = 1.5f;
                    break;

                case HIGH_MOUNTAIN:
                case WATER:
                    cost = 2f;
                    break;

                case DEEP_WATER:
                    cost = 3f;
                    break;

                default:
                    cost = 1f;
            }

            delay = speed.value * cost;

            return true;
        }

        @Override
        public void doAction()
        {
            final Position   p = mPosition.get(actorId);

            final int newX = p.x + direction.x;
            final int newY = p.y + direction.y;

            if (!mObstacle.has(actorId))
            {
                // it's a moving item
                items.move(actorId, p.x, p.y, newX, newY);

                p.x = newX;
                p.y = newY;

                // it doesn't have stamina
            }
            else if (sMap.isFree(newX, newY))
            {
                final int id = grid.move(p.x, p.y, newX, newY);

                if (id >= 0)
                {
                    log.error("entity {} was at the new position {}", id, p);

                    return;
                }

                p.x = newX;
                p.y = newY;

                sStamina.consume(actorId, cost);
            }
        }
    }

    public MoveAction move(final int entityId, final Side direction)
    {
        if (direction.equals(Side.HERE))
            return null;

        final MoveAction a = new MoveAction();

        a.actorId = entityId;
        a.direction = direction;

        return a;
    }
}
