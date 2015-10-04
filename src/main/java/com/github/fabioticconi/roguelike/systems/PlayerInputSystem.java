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
import com.github.fabioticconi.roguelike.App;
import com.github.fabioticconi.roguelike.components.MoveTo;
import com.github.fabioticconi.roguelike.components.Player;
import com.github.fabioticconi.roguelike.components.Position;
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
    ComponentMapper<Position> mPosition;
    ComponentMapper<MoveTo>   mMoveTo;

    RenderSystem   render;
    MovementSystem movement;

    @Wire
    Map map;

    /**
     * @param aspect
     */
    public PlayerInputSystem()
    {
        super(Aspect.all(Player.class, Position.class));
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

        final MoveTo m;

        final KeyStroke k = render.getInput();

        if (k == null)
            return;

        switch (k.getKeyType())
        {
            case Escape:
                // TODO: more termination stuff I guess
                App.keepRunning = false;
                render.close();
                break;
            case ArrowDown:
                m = mMoveTo.create(pID);

                m.cooldown = m.speed;
                m.direction = Side.S;

                movement.offerDelay(m.cooldown);

                break;
            case ArrowUp:
                m = mMoveTo.create(pID);

                m.cooldown = m.speed;
                m.direction = Side.N;

                movement.offerDelay(m.cooldown);

                break;
            case ArrowLeft:
                m = mMoveTo.create(pID);

                m.cooldown = m.speed;
                m.direction = Side.W;

                movement.offerDelay(m.cooldown);

                break;
            case ArrowRight:
                m = mMoveTo.create(pID);

                m.cooldown = m.speed;
                m.direction = Side.E;

                movement.offerDelay(m.cooldown);

                break;
            case Character:
                final Position p = mPosition.get(pID);

                switch (k.getCharacter())
                {
                    case 'W':
                    case 'w':
                        map.set(p.x, p.y, Cell.WALL);
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
