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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fabioticconi.roguelite.behaviours.*;
import com.github.fabioticconi.roguelite.components.*;
import com.github.fabioticconi.roguelite.components.attributes.*;
import com.github.fabioticconi.roguelite.constants.Options;
import com.github.fabioticconi.roguelite.map.EntityGrid;
import com.github.fabioticconi.roguelite.map.Map;
import com.github.fabioticconi.roguelite.utils.Util;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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

        // add player
        int        id   = world.create();
        EntityEdit edit = world.edit(id);

        // load the player's data
        try
        {
            loadBody("player.yaml", edit);
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        edit.create(Player.class);
        x = Options.MAP_SIZE_X / 2;
        y = Options.MAP_SIZE_Y / 2;
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).c = new TextCharacter('@').withForegroundColor(TextColor.ANSI.GREEN)
                                                            .withModifier(SGR.BOLD);
        grid.putEntity(id, x, y);
        pManager.setPlayer(world.getEntity(id), "player");
        System.out.println("setPlayer");

        // add a herd of buffalos
        int    groupId = sGroup.createGroup();
        IntSet group   = sGroup.getGroup(groupId);
        for (int i = 0; i < 5; i++)
        {
            id = world.create();
            edit = world.edit(id);

            try
            {
                loadBody("buffalo.yaml", edit);
            } catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(FleeBehaviour.class));
            ai.behaviours.add(world.getSystem(GrazeBehaviour.class));
            ai.behaviours.add(world.getSystem(FlockBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.create(Position.class).set(x, y);
            edit.create(Group.class).groupId = groupId;
            group.add(id);
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sprite.class).c = new TextCharacter('H').withForegroundColor(TextColor.ANSI.MAGENTA)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }

        // add small, independent rabbits/hares
        for (int i = 0; i < 0; i++)
        {
            id = world.create();
            edit = world.edit(id);

            try
            {
                loadBody("rabbit.yaml", edit);
            } catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(FleeBehaviour.class));
            ai.behaviours.add(world.getSystem(GrazeBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.create(Position.class).set(x, y);
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sprite.class).c = new TextCharacter('h').withForegroundColor(TextColor.ANSI.BLUE)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }

        // add a pack of wolves
        groupId = sGroup.createGroup();
        group = sGroup.getGroup(groupId);
        for (int i = 0; i < 0; i++)
        {
            id = world.create();
            edit = world.edit(id);

            try
            {
                loadBody("wolf.yaml", edit);
            } catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(ChaseBehaviour.class));
            ai.behaviours.add(world.getSystem(FlockBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.create(Position.class).set(x, y);
            edit.create(Group.class).groupId = groupId;
            group.add(id);
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sprite.class).c = new TextCharacter('c').withForegroundColor(TextColor.ANSI.RED)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }

        // add solitary pumas
        for (int i = 0; i < 3; i++)
        {
            id = world.create();
            edit = world.edit(id);

            try
            {
                loadBody("puma.yaml", edit);
            } catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(ChaseBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(10) - 5;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(10) - 5;
            edit.create(Position.class).set(x, y);
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sprite.class).c = new TextCharacter('C').withForegroundColor(TextColor.ANSI.RED)
                                                                .withModifier(SGR.BOLD);

            grid.putEntity(id, x, y);
        }

        System.out.println("Bootstrap done");
    }

    public void loadBody(final String filename, final EntityEdit edit) throws IOException
    {
        final YAMLFactory factory = new YAMLFactory();
        final JsonParser  parser  = factory.createParser(new File(filename));

        int str = 0, agi = 0, con = 0, skin = 0, sight = 0;
        boolean herbivore = false, carnivore = false;

        while (parser.nextToken() != null)
        {
            final String name = parser.getCurrentName();

            if (name.equals("strength"))
            {
                str = parser.getIntValue();
                edit.create(Strength.class).value = Util.ensureRange(str, -2, 2);
            }
            else if (name.equals("agility"))
            {
                agi = parser.getIntValue();
                edit.create(Agility.class).value = Util.ensureRange(agi, -2, 2);
            }
            else if (name.equals("constitution"))
            {
                con = parser.getIntValue();
                edit.create(Constitution.class).value = Util.ensureRange(con, -2, 2);
            }
            else if (name.equals("skin"))
            {
                skin = parser.getIntValue();
                edit.create(Skin.class).value = Util.ensureRange(skin, -2, 2);
            }
            else if (name.equals("sight"))
            {
                sight = parser.getIntValue();
                edit.create(Sight.class).value = Util.ensureRange(sight, 1, 18);
            }
            else if (name.equals("herbivore"))
            {
                herbivore = parser.getBooleanValue();

                if (herbivore)
                    edit.create(Herbivore.class);
            }
            else if (name.equals("carnivore"))
            {
                carnivore = parser.getBooleanValue();

                if (carnivore)
                    edit.create(Carnivore.class);
            }
        }

        // TODO check if neither herbivore nor carnivore? player is currently as such, for testing

        // Secondary Attributes
        final int size = Math.round((con - agi) / 2f);
        edit.create(Size.class).value = size;
        edit.create(Stamina.class).value = 5 + str + con;
        edit.create(Speed.class).value = (con - str - agi + 6) / 12f;

        // Tertiary Attributes
        edit.create(Hunger.class).value = (size / 2f) + 2f;
    }
}
