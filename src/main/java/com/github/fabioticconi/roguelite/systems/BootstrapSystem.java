/**
 * Copyright 2015 Fabio Ticconi
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.fabioticconi.roguelite.systems;

import com.artemis.BaseSystem;
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.artemis.managers.PlayerManager;
import com.github.fabioticconi.roguelite.behaviours.*;
import com.github.fabioticconi.roguelite.components.*;
import com.github.fabioticconi.roguelite.constants.Options;
import com.github.fabioticconi.roguelite.map.EntityGrid;
import com.github.fabioticconi.roguelite.map.Map;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Random;

/**
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

    GroupSystem sGroup;

    PlayerManager pManager;

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
        int        id   = world.create();
        EntityEdit edit = world.edit(id);
        edit.create(Player.class);
        x = Options.MAP_SIZE_X / 2;
        y = Options.MAP_SIZE_Y / 2;
        edit.add(new Position(x, y));
        edit.create(Sight.class).value = 18;
        edit.create(Speed.class).value = 0.1f;
        edit.create(Sprite.class).c = new TextCharacter('@').withForegroundColor(TextColor.ANSI.GREEN)
                                                            .withModifier(SGR.BOLD);
        grid.putEntity(id, x, y);
        pManager.setPlayer(world.getEntity(id), "player");
        System.out.println("setPlayer");

        // add group of short-sighted herbivores
        int    groupId = sGroup.createGroup();
        IntSet group   = sGroup.getGroup(groupId);
        for (int i = 0; i < 5; i++)
        {
            id = world.create();
            edit = world.edit(id);
            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(FleeBehaviour.class));
            ai.behaviours.add(world.getSystem(GrazeBehaviour.class));
            ai.behaviours.add(world.getSystem(FlockBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.add(new Position(x, y));
            edit.create(Herbivore.class);
            edit.create(Group.class).groupId = groupId;
            group.add(id);
            edit.create(Hunger.class).value = 0.5f;
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sight.class).value = 5;
            edit.create(Speed.class).value = r.nextFloat() * 1.0f;
            edit.create(Sprite.class).c = new TextCharacter('H').withForegroundColor(TextColor.ANSI.MAGENTA)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }

        // add un-grouped, longer-sighted herbivores
        for (int i = 0; i < 0; i++)
        {
            id = world.create();
            edit = world.edit(id);
            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(FleeBehaviour.class));
            ai.behaviours.add(world.getSystem(GrazeBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.add(new Position(x, y));
            edit.create(Herbivore.class);
            edit.create(Hunger.class).value = 0.5f;
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sight.class).value = 10;
            edit.create(Speed.class).value = r.nextFloat() * 1.0f;
            edit.create(Sprite.class).c = new TextCharacter('h').withForegroundColor(TextColor.ANSI.BLUE)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }

        // add a group of medium-sighted carnivores
        groupId = sGroup.createGroup();
        group = sGroup.getGroup(groupId);
        for (int i = 0; i < 0; i++)
        {
            id = world.create();
            edit = world.edit(id);
            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(ChaseBehaviour.class));
            ai.behaviours.add(world.getSystem(FlockBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.add(new Position(x, y));
            edit.create(Carnivore.class);
            edit.create(Group.class).groupId = groupId;
            group.add(id);
            edit.create(Hunger.class).value = 0.0f;
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sight.class).value = 7;
            edit.create(Speed.class).value = r.nextFloat() * 1.0f;
            edit.create(Sprite.class).c = new TextCharacter('c').withForegroundColor(TextColor.ANSI.RED)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }

        // add a few un-grouped, long-sighted carnivores
        for (int i = 0; i < 3; i++)
        {
            id = world.create();
            edit = world.edit(id);
            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(ChaseBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.add(new Position(x, y));
            edit.create(Carnivore.class);
            edit.create(Hunger.class).value = 0.0f;
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sight.class).value = 9;
            edit.create(Speed.class).value = r.nextFloat() * 1.0f;
            edit.create(Sprite.class).c = new TextCharacter('C').withForegroundColor(TextColor.ANSI.RED)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }

        System.out.println("Bootstrap done");
    }
}
