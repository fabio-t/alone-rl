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
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelike.Roguelike;
import com.github.fabioticconi.roguelike.components.Player;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Speed;
import com.github.fabioticconi.roguelike.components.commands.MoveCommand;
import com.github.fabioticconi.roguelike.constants.Side;
import com.github.fabioticconi.roguelike.map.Cell;
import com.github.fabioticconi.roguelike.map.Map;
import com.googlecode.lanterna.input.KeyStroke;

/**
 *
 * @author Fabio Ticconi
 */
public class PlayerInputSystem extends BaseEntitySystem
{
    ComponentMapper<Position>    mPosition;
    ComponentMapper<Speed>       mSpeed;
    ComponentMapper<MoveCommand> mMoveTo;

    RenderSystem                 render;
    MovementSystem               movement;

    @Wire
    Map                          map;

    /**
     *
     */
    public PlayerInputSystem()
    {
        super(Aspect.all(Player.class, Position.class, Speed.class));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.BaseSystem#processSystem()
     */
    @Override
    protected void processSystem()
    {
        final int pID = subscription.getEntities().get(0);

        final float speed = mSpeed.get(pID).speed;

        final MoveCommand m;

        final KeyStroke k = render.getInput();

        if (k == null)
            return;

        switch (k.getKeyType())
        {
            case Escape:
                // TODO: more termination stuff I guess
                Roguelike.keepRunning = false;
                render.close();
                break;
            case ArrowDown:
                m = mMoveTo.create(pID);

                m.cooldown = speed;
                m.direction = Side.S;

                movement.offerDelay(m.cooldown);

                break;
            case ArrowUp:
                m = mMoveTo.create(pID);

                m.cooldown = speed;
                m.direction = Side.N;

                movement.offerDelay(m.cooldown);

                break;
            case ArrowLeft:
                m = mMoveTo.create(pID);

                m.cooldown = speed;
                m.direction = Side.W;

                movement.offerDelay(m.cooldown);

                break;
            case ArrowRight:
                m = mMoveTo.create(pID);

                m.cooldown = speed;
                m.direction = Side.E;

                movement.offerDelay(m.cooldown);

                break;
            case Character:
                final Position p = mPosition.get(pID);

                switch (k.getCharacter())
                {
                    case '#':
                        map.set(p.x, p.y, Cell.WALL);
                        break;
                    case '+':
                        map.set(p.x, p.y, Cell.CLOSED_DOOR);
                        break;
                    case '/':
                        map.set(p.x, p.y, Cell.OPEN_DOOR);
                        break;
                    default:
                        break;
                }

                break;
            default:
                break;
        }
    }
}
