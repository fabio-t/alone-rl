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

import java.util.Random;

import com.artemis.BaseSystem;
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelike.components.AI;
import com.github.fabioticconi.roguelike.components.Carnivore;
import com.github.fabioticconi.roguelike.components.Herbivore;
import com.github.fabioticconi.roguelike.components.Player;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Speed;
import com.github.fabioticconi.roguelike.components.Sprite;
import com.github.fabioticconi.roguelike.components.internal_states.Fear;
import com.github.fabioticconi.roguelike.components.internal_states.Hunger;
import com.github.fabioticconi.roguelike.constants.Options;
import com.github.fabioticconi.roguelike.map.EntityGrid;
import com.github.fabioticconi.roguelike.map.Map;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;

/**
 *
 * @author Fabio Ticconi
 */
public class BootstrapSystem extends BaseSystem
{
    @Wire
    Map        map;
    @Wire
    EntityGrid grid;
    @Wire
    Random     r;

    AISystem   ai;

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.BaseSystem#processSystem()
     */
    @Override
    protected void processSystem()
    {
        // this must be only run once
        setEnabled(false);

        int x;
        int y;

        // add player at the center of the map
        int id = world.create();
        EntityEdit edit = world.edit(id);
        edit.create(Player.class);
        x = Options.MAP_SIZE_X / 2;
        y = Options.MAP_SIZE_Y / 2;
        edit.add(new Position(x, y));
        edit.create(Speed.class).speed = 0.1f;
        edit.create(Sprite.class).c = new TextCharacter('@').withForegroundColor(TextColor.ANSI.GREEN)
                                                            .withModifier(SGR.BOLD);
        grid.putEntity(id, x, y);

        // add a few hervibores
        for (int i = 0; i < 10; i++)
        {
            id = world.create();
            edit = world.edit(id);
            edit.add(new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1000.0f));
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.add(new Position(x, y));
            edit.create(Herbivore.class);
            edit.create(Hunger.class).value = 0.0f;
            edit.create(Fear.class).value = 0.0f;
            edit.create(Speed.class).speed = r.nextFloat() * 1000000.0f;
            edit.create(Sprite.class).c = new TextCharacter('H').withForegroundColor(TextColor.ANSI.BLUE)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }

        // add a few carnivores
        for (int i = 0; i < 3; i++)
        {
            id = world.create();
            edit = world.edit(id);
            edit.add(new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1000.0f));
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.add(new Position(x, y));
            edit.create(Carnivore.class);
            edit.create(Hunger.class).value = 0.0f;
            edit.create(Speed.class).speed = r.nextFloat() * 500000.0f;
            edit.create(Sprite.class).c = new TextCharacter('C').withForegroundColor(TextColor.ANSI.RED)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }
    }
}
