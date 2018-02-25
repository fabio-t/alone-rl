/*
 * Copyright (C) 2015-2017 Fabio Ticconi
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
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.artemis.managers.PlayerManager;
import com.artemis.utils.IntBag;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.github.fabioticconi.alone.behaviours.*;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.attributes.*;
import com.github.fabioticconi.alone.constants.Options;
import com.github.fabioticconi.alone.constants.TerrainType;
import com.github.fabioticconi.alone.utils.Util;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * @author Fabio Ticconi
 */
public class CreatureSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(CreatureSystem.class);

    ComponentMapper<Strength>     mStr;
    ComponentMapper<Agility>      mAgi;
    ComponentMapper<Constitution> mCon;
    ComponentMapper<Herbivore>    mHerbivore;
    ComponentMapper<Carnivore>    mCarnivore;

    @Wire
    Random r;

    GroupSystem sGroup;
    MapSystem   sMap;
    TreeSystem  sTree;
    CrushSystem sCrush;
    ItemSystem  sItems;
    MapSystem   map;

    PlayerManager pManager;

    boolean loaded = false;

    public void reset()
    {
        loaded = false;

        placeObjects();
    }

    public void placeObjects()
    {
        if (loaded)
            return;

        loaded = true;

        int x;
        int y;

        // add player
        int        id   = world.create();
        EntityEdit edit = world.edit(id);

        // load the player's data
        try
        {
            loadBody("data/player.yml", edit);
        } catch (final IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        edit.create(Player.class);
        x = Options.MAP_SIZE_X / 2;
        y = Options.MAP_SIZE_Y / 2;
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set('@', Color.WHITE);
        map.obstacles.set(id, x, y);
        pManager.setPlayer(world.getEntity(id), "player");
        edit.create(Inventory.class);
        edit.add(new Name("You", "you"));

        // add a herd of buffalos
        int    groupId = sGroup.createGroup();
        IntBag group   = sGroup.getGroup(groupId);
        for (int i = 0; i < 4; i++)
        {
            // before doing anything, we must ensure the position is free!
            do
            {
                x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
                y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;
            } while (!map.obstacles.isEmpty(x, y));

            id = world.create();
            edit = world.edit(id);

            try
            {
                loadBody("data/creatures/buffalo.yml", edit);
            } catch (final IOException e)
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
            edit.create(Position.class).set(x, y);
            edit.create(Group.class).groupId = groupId;
            group.add(id);
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sprite.class).set('b', Util.BROWN.darker().darker());
            // edit.create(Sprite.class).set(Character.forDigit(id, 10), Util.BROWN.darker().darker());
            edit.add(new Name("A big buffalo", "buffalo"));

            map.obstacles.set(id, x, y);
        }

        // add small, independent rabbits/hares
        for (int i = 0; i < 7; i++)
        {
            // before doing anything, we must ensure the position is free!
            do
            {
                x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
                y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;
            } while (!map.obstacles.isEmpty(x, y));

            id = world.create();
            edit = world.edit(id);

            try
            {
                loadBody("data/creatures/rabbit.yml", edit);
            } catch (final IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(FleeBehaviour.class));
            ai.behaviours.add(world.getSystem(GrazeBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            edit.create(Position.class).set(x, y);
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sprite.class).set('r', Color.LIGHT_GRAY);
            edit.add(new Name("A cute rabbit", "rabbit"));

            map.obstacles.set(id, x, y);
        }

        // add a pack of wolves
        groupId = sGroup.createGroup();
        group = sGroup.getGroup(groupId);
        for (int i = 0; i < 5; i++)
        {
            // before doing anything, we must ensure the position is free!
            do
            {
                x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
                y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;
            } while (!map.obstacles.isEmpty(x, y));

            id = world.create();
            edit = world.edit(id);

            try
            {
                loadBody("data/creatures/wolf.yml", edit);
            } catch (final IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(ChaseBehaviour.class));
            ai.behaviours.add(world.getSystem(FlockBehaviour.class));
            ai.behaviours.add(world.getSystem(ScavengeBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            edit.create(Position.class).set(x, y);
            edit.create(Group.class).groupId = groupId;
            group.add(id);
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sprite.class).set('w', Color.DARK_GRAY);
            // edit.create(Sprite.class).set(Character.forDigit(id, 10), Color.DARK_GRAY);
            edit.add(new Name("A ferocious wolf", "wolf"));

            map.obstacles.set(id, x, y);
        }

        // add solitary pumas
        for (int i = 0; i < 5; i++)
        {
            // before doing anything, we must ensure the position is free!
            do
            {
                x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
                y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;
            } while (!map.obstacles.isEmpty(x, y));

            id = world.create();
            edit = world.edit(id);

            try
            {
                loadBody("data/creatures/puma.yml", edit);
            } catch (final IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
            ai.behaviours.add(world.getSystem(ChaseBehaviour.class));
            ai.behaviours.add(world.getSystem(ScavengeBehaviour.class));
            ai.behaviours.add(world.getSystem(WanderBehaviour.class));
            edit.add(ai);
            edit.create(Position.class).set(x, y);
            edit.create(Alertness.class).value = 0.0f;
            edit.create(Sprite.class).set('p', Util.BROWN.darker().darker());
            // edit.create(Sprite.class).set(Character.forDigit(id, 10), Util.BROWN.darker());
            edit.add(new Name("A strong puma", "puma"));

            map.obstacles.set(id, x, y);
        }

        // add fish in the sea
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final MapSystem.Cell cell = sMap.get(x, y);

                if (cell.type.equals(TerrainType.WATER) && r.nextGaussian() > 5f)
                {
                    if (!map.obstacles.isEmpty(x, y))
                        continue;

                    id = world.create();
                    edit = world.edit(id);

                    try
                    {
                        loadBody("data/creatures/fish.yml", edit);
                    } catch (final IOException e)
                    {
                        e.printStackTrace();
                        System.exit(1);
                    }

                    final AI ai = new AI(r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f);
                    ai.behaviours.add(world.getSystem(UnderwaterBehaviour.class));
                    ai.behaviours.add(world.getSystem(FleeFromActionBehaviour.class));
                    edit.add(ai);
                    edit.create(Position.class).set(x, y);
                    edit.create(Alertness.class).value = 0.0f;
                    edit.create(Sprite.class).set('f', Color.CYAN.darker());
                    edit.add(new Name("A colorful fish", "fish"));

                    map.obstacles.set(id, x, y);
                }
            }
        }

        // add random trees
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final MapSystem.Cell cell = sMap.get(x, y);

                if ((cell.type.equals(TerrainType.GRASS) && r.nextGaussian() > 2.5f) ||
                    (cell.type.equals(TerrainType.LAND) && r.nextGaussian() > 3f))
                {
                    if (!map.obstacles.isEmpty(x, y))
                        continue;

                    sItems.makeItem("tree", x, y);
                }
            }
        }

        // add random boulders
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final MapSystem.Cell cell = sMap.get(x, y);

                if ((cell.type.equals(TerrainType.GRASS) && r.nextGaussian() > 3.5f) ||
                    (cell.type.equals(TerrainType.LAND) && r.nextGaussian() > 3f))
                {
                    if (!map.obstacles.isEmpty(x, y))
                        continue;

                    sItems.makeItem("boulder", x, y);
                }
            }
        }

        // add random stones
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final MapSystem.Cell cell = sMap.get(x, y);

                if ((cell.type.equals(TerrainType.GRASS) && r.nextGaussian() > 3f) ||
                    (cell.type.equals(TerrainType.LAND) && r.nextGaussian() > 2.5f))
                {
                    if (!map.items.isEmpty(x, y))
                        continue;

                    sItems.makeItem("stone", x, y);
                }
            }
        }

        // add random trunks and branches
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final MapSystem.Cell cell = sMap.get(x, y);

                if ((cell.type.equals(TerrainType.GRASS) && r.nextGaussian() > 3f) ||
                    (cell.type.equals(TerrainType.LAND) && r.nextGaussian() > 3.5f))
                {
                    if (!map.items.isEmpty(x, y))
                        continue;

                    sItems.makeItem("trunk", x, y);
                    sItems.makeItem("branch", x, y);
                    sItems.makeItem("vine", x, y);
                }
            }
        }

        log.info("initialised");
    }

    public void loadBody(final String filename, final EntityEdit edit) throws IOException
    {
        // final InputStream fileStream = loader.getResourceAsStream(filename);
        final InputStream fileStream = new FileInputStream(filename);

        final YAMLFactory factory = new YAMLFactory();
        final YAMLParser  parser  = factory.createParser(fileStream);

        int     str       = 0, agi = 0, con = 0, skin = 0, sight = 0;
        boolean herbivore = false, carnivore = false;

        parser.nextToken(); // START_OBJECT

        while (parser.nextToken() != null)
        {
            final String name = parser.getCurrentName();

            if (name == null)
                break;

            parser.nextToken(); // get value for this "name"

            switch (name)
            {
                case "strength":
                    str = parser.getIntValue();
                    edit.create(Strength.class).value = Util.clamp(str, -2, 2);
                    break;
                case "agility":
                    agi = parser.getIntValue();
                    edit.create(Agility.class).value = Util.clamp(agi, -2, 2);
                    break;
                case "constitution":
                    con = parser.getIntValue();
                    edit.create(Constitution.class).value = Util.clamp(con, -2, 2);
                    break;
                case "skin":
                    skin = parser.getIntValue();
                    edit.create(Skin.class).value = Util.clamp(skin, -2, 2);
                    break;
                case "sight":
                    sight = parser.getIntValue();
                    edit.create(Sight.class).value = Util.clamp(sight, 1, 18);
                    break;
                case "herbivore":
                    herbivore = parser.getBooleanValue();

                    if (herbivore)
                        edit.create(Herbivore.class);
                    break;
                case "carnivore":
                    carnivore = parser.getBooleanValue();

                    if (carnivore)
                        edit.create(Carnivore.class);
                    break;
                case "underwater":
                    edit.create(Underwater.class);
                    break;
            }
        }

        // Secondary Attributes
        final int size = Math.round((con - agi) / 2f);
        edit.create(Size.class).set(size);
        edit.create(Stamina.class).set((5 + str + con) * 100); // FIXME for debug, reduce/tweak later
        edit.create(Speed.class).set((con - str - agi + 6) / 12f);
        edit.create(Health.class).set((con + 3) * 10);

        // Tertiary Attributes

        // a fish does not need to eat, for now
        if (herbivore || carnivore)
            edit.create(Hunger.class).set(0f, (size / 2f) + 2f);
    }

    public void makeDerivative(final int id)
    {
        final Strength     str = mStr.get(id);
        final Agility      agi = mAgi.get(id);
        final Constitution con = mCon.get(id);

        final EntityEdit edit = world.edit(id);

        // Secondary Attributes
        final int size = Math.round((con.value - agi.value) / 2f);
        edit.create(Size.class).set(size);
        edit.create(Stamina.class).set((5 + str.value + con.value) * 100); // FIXME for debug, reduce/tweak later
        edit.create(Speed.class).set((con.value - str.value - agi.value + 6) / 12f);
        edit.create(Health.class).set((con.value + 3) * 10);

        // Tertiary Attributes

        // a fish does not need to eat, for now
        if (mHerbivore.has(id) || mCarnivore.has(id))
            edit.create(Hunger.class).set(0f, (size / 2f) + 2f);
    }
}
