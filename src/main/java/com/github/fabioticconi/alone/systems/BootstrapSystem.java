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

import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.artemis.managers.PlayerManager;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fabioticconi.alone.behaviours.*;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.attributes.*;
import com.github.fabioticconi.alone.constants.Cell;
import com.github.fabioticconi.alone.constants.Options;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;
import com.github.fabioticconi.alone.utils.Util;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * @author Fabio Ticconi
 */
public class BootstrapSystem extends PassiveSystem
{
    private final ClassLoader loader = getClass().getClassLoader();

    @Wire
    SingleGrid grid;

    @Wire
    MultipleGrid items;

    @Wire
    Random r;

    GroupSystem sGroup;
    MapSystem   sMap;
    TreeSystem  sTree;
    CrushSystem sCrush;

    PlayerManager pManager;

    @Override
    protected void initialize()
    {
        // this must be only run once
        // setEnabled(false);

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
        grid.set(id, x, y);
        pManager.setPlayer(world.getEntity(id), "player");
        edit.create(Inventory.class);
        edit.add(new Name("You"));

        // add a herd of buffalos
        int    groupId = sGroup.createGroup();
        IntSet group   = sGroup.getGroup(groupId);
        for (int i = 0; i < 4; i++)
        {
            // before doing anything, we must ensure the position is free!
            do
            {
                x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
                y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;
            } while (!grid.isEmpty(x, y));

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
            edit.add(new Name("A buffalo"));

            grid.set(id, x, y);
        }

        // add small, independent rabbits/hares
        for (int i = 0; i < 7; i++)
        {
            // before doing anything, we must ensure the position is free!
            do
            {
                x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
                y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;
            } while (!grid.isEmpty(x, y));

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
            edit.add(new Name("A rabbit"));

            grid.set(id, x, y);
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
            } while (!grid.isEmpty(x, y));

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
            edit.add(new Name("A wolf"));

            grid.set(id, x, y);
        }

        // add solitary pumas
        for (int i = 0; i < 5; i++)
        {
            // before doing anything, we must ensure the position is free!
            do
            {
                x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
                y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;
            } while (!grid.isEmpty(x, y));

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
            edit.add(new Name("A puma"));

            grid.set(id, x, y);
        }

        // add fish in the sea
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final Cell cell = sMap.get(x, y);

                // if ((cell.equals(Cell.DEEP_WATER) && r.nextGaussian() > 3f) ||
                //     (cell.equals(Cell.WATER) && r.nextGaussian() > 2.5f))
                if ((cell.equals(Cell.DEEP_WATER) && r.nextGaussian() > 5f) ||
                    (cell.equals(Cell.WATER) && r.nextGaussian() > 5f))
                {
                    if (!grid.isEmpty(x, y))
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
                    edit.add(new Name("A fish"));

                    grid.set(id, x, y);
                }
            }
        }

        // add random trees
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final Cell cell = sMap.get(x, y);

                if ((cell.equals(Cell.GRASS) && r.nextGaussian() > 3f) ||
                    (cell.equals(Cell.HILL_GRASS) && r.nextGaussian() > 2f) ||
                    (cell.equals(Cell.HILL) && r.nextGaussian() > 3f))
                {
                    if (!grid.isEmpty(x, y))
                        continue;

                    id = sTree.makeTree(x, y);

                    grid.set(id, x, y);
                }
            }
        }

        // add random boulders
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final Cell cell = sMap.get(x, y);

                if (((cell.equals(Cell.HIGH_MOUNTAIN)) && r.nextGaussian() > 3f) ||
                    (cell.equals(Cell.MOUNTAIN) && r.nextGaussian() > 2.5f) ||
                    (cell.equals(Cell.HILL) && r.nextGaussian() > 3f) ||
                    (cell.equals(Cell.HILL_GRASS) && r.nextGaussian() > 3.5f) ||
                    (cell.equals(Cell.GRASS) && r.nextGaussian() > 4f) ||
                    (cell.equals(Cell.GROUND) && r.nextGaussian() > 4f))
                {
                    if (!grid.isEmpty(x, y))
                        continue;

                    id = sCrush.makeBoulder(x, y);

                    grid.set(id, x, y);
                }
            }
        }

        // add random stones
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final Cell cell = sMap.get(x, y);

                if (((cell.equals(Cell.HIGH_MOUNTAIN)) && r.nextGaussian() > 3f) ||
                    (cell.equals(Cell.MOUNTAIN) && r.nextGaussian() > 2.5f) ||
                    (cell.equals(Cell.HILL) && r.nextGaussian() > 3f) ||
                    (cell.equals(Cell.HILL_GRASS) && r.nextGaussian() > 3.5f) ||
                    (cell.equals(Cell.GRASS) && r.nextGaussian() > 4f) ||
                    (cell.equals(Cell.GROUND) && r.nextGaussian() > 2.5f))
                {
                    if (!grid.isEmpty(x, y))
                        continue;

                    id = sCrush.makeStone(x, y);
                    items.add(id, x, y);
                }
            }
        }

        // add random trunks and branches
        for (x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final Cell cell = sMap.get(x, y);

                if ((cell.equals(Cell.GRASS) && r.nextGaussian() > 4f) ||
                    (cell.equals(Cell.HILL_GRASS) && r.nextGaussian() > 3f) ||
                    (cell.equals(Cell.HILL) && r.nextGaussian() > 4f))
                {
                    if (!grid.isEmpty(x, y))
                        continue;

                    switch (r.nextInt(3))
                    {
                        case 0:
                            id = sTree.makeTrunk(x, y);
                            items.add(id, x, y);
                            break;

                        case 1:
                            id = sTree.makeBranch(x, y);
                            items.add(id, x, y);
                            break;

                        case 2:
                            id = sTree.makeBranch(x, y);
                            items.add(id, x, y);
                            break;
                    }
                }
            }
        }

        System.out.println("Bootstrap done");
    }

    public void loadBody(final String filename, final EntityEdit edit) throws IOException
    {
        // final InputStream fileStream = loader.getResourceAsStream(filename);
        final InputStream fileStream = new FileInputStream(filename);

        final YAMLFactory factory = new YAMLFactory();
        final JsonParser  parser  = factory.createParser(fileStream);

        int     str       = 0, agi = 0, con = 0, skin = 0, sight = 0;
        boolean herbivore = false, carnivore = false;

        parser.nextToken(); // START_OBJECT

        while (parser.nextToken() != null)
        {
            final String name = parser.getCurrentName();

            if (name == null)
                break;

            parser.nextToken(); // get in value

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
            else if (name.equals("underwater"))
            {
                edit.create(Underwater.class);
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
}
