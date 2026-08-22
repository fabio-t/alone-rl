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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.attributes.*;
import com.github.fabioticconi.alone.constants.Options;
import com.github.fabioticconi.alone.constants.TerrainType;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Fabio Ticconi
 */
public class CreatureSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(CreatureSystem.class);

    ComponentMapper<Name>         mName;
    ComponentMapper<AI>           mAI;
    ComponentMapper<Position>     mPosition;
    ComponentMapper<Strength>     mStr;
    ComponentMapper<Agility>      mAgi;
    ComponentMapper<Constitution> mCon;
    ComponentMapper<Herbivore>    mHerbivore;
    ComponentMapper<Carnivore>    mCarnivore;

    GroupSystem sGroup;
    MapSystem   sMap;
    ItemSystem  sItems;
    MapSystem   map;

    PlayerManager pManager;

    @Wire
    Random r;

    @Wire
    ObjectMapper mapper;

    HashMap<String, CreatureTemplate> templates;

    boolean loaded = false;

    @Override
    protected void initialize()
    {
        try
        {
            loadTemplates();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    public HashMap<String, CreatureTemplate> getTemplates()
    {
        try
        {
            loadTemplates();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }

        return templates;
    }

    public void loadTemplates() throws IOException
    {
        final InputStream fileStream = new FileInputStream("data/creatures.yml");

        templates = mapper.readValue(fileStream, new TypeReference<HashMap<String, CreatureTemplate>>()
        {
        });

        for (final Map.Entry<String, CreatureTemplate> entry : templates.entrySet())
        {
            final CreatureTemplate temp = entry.getValue();
            temp.tag = entry.getKey();
        }
    }

    public void reset()
    {
        loaded = false;

        placeObjects();
    }

    /**
     * It instantiates an object of the given type and places at that Point.
     *
     * @param tag
     * @param p
     * @return
     */
    public int makeCreature(final String tag, final Point p)
    {
        return makeCreature(tag, p.x, p.y);
    }

    /**
     * It instantiates an object of the given type and places at that position.
     *
     * @param tag
     * @param x
     * @param y
     * @return
     */
    public int makeCreature(final String tag, final int x, final int y)
    {
        final int id = makeCreature(tag);

        if (id < 0)
            return id;

        final Point p = map.getFirstTotallyFree(x, y, -1);

        mPosition.create(id).set(p.x, p.y);

        map.obstacles.set(id, p.x, p.y);

        return id;
    }

    public int makeCreature(final String tag)
    {
        final CreatureTemplate template = templates.get(tag);

        if (template == null)
        {
            log.warn("Creature named {} doesn't exist", tag);
            return -1;
        }

        final int id = world.create();

        final EntityEdit edit = world.edit(id);

        edit.add(new Name(template.name, tag));

        if (template.strength != null)
            edit.add(template.strength);
        if (template.agility != null)
            edit.add(template.agility);
        if (template.constitution != null)
            edit.add(template.constitution);
        if (template.skin != null)
            edit.add(template.skin);
        if (template.sight != null)
            edit.add(template.sight);
        if (template.herbivore != null)
            edit.add(template.herbivore);
        if (template.carnivore != null)
            edit.add(template.carnivore);
        if (template.ai != null)
        {
            template.ai.cooldown = r.nextFloat() * AISystem.BASE_TICKTIME + 1.0f;
            edit.add(template.ai);
        }
        if (template.group != null)
            edit.add(template.group);
        if (template.sprite != null)
            edit.add(template.sprite);
        if (template.player != null)
            edit.add(template.player);
        if (template.inventory != null)
            edit.add(template.inventory);
        if (template.underwater != null)
            edit.add(template.underwater);

        edit.create(Alertness.class).value = 0.0f;

        makeDerivative(id);

        return id;
    }

    public void placeObjects()
    {
        if (loaded)
            return;

        loaded = true;

        int x;
        int y;

        // add player
        x = Options.MAP_SIZE_X / 2;
        y = Options.MAP_SIZE_Y / 2;
        int id = makeCreature("player", x, y);
        pManager.setPlayer(world.getEntity(id), "player");
        world.edit(id).add(new Name("You", "you"));

        // add a herd of buffalos
        int    groupId = sGroup.createGroup();
        IntBag group   = sGroup.getGroup(groupId);
        for (int i = 0; i < 4; i++)
        {
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;

            id = makeCreature("buffalo", x, y);
            world.edit(id).create(Group.class).groupId = groupId;
            group.add(id);
        }

        // add small, independent rabbits/hares
        for (int i = 0; i < 7; i++)
        {
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;

            makeCreature("rabbit", x, y);
        }

        // add a pack of wolves
        groupId = sGroup.createGroup();
        group = sGroup.getGroup(groupId);
        for (int i = 0; i < 5; i++)
        {
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;

            id = makeCreature("wolf", x, y);
            world.edit(id).create(Group.class).groupId = groupId;
            group.add(id);
        }

        // add solitary pumas
        for (int i = 0; i < 5; i++)
        {
            x = (Options.MAP_SIZE_X / 2) + r.nextInt(12) - 6;
            y = (Options.MAP_SIZE_Y / 2) + r.nextInt(12) - 6;

            makeCreature("puma", x, y);
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

                    makeCreature("fish", x, y);
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

                    // 1% of the trees are fallen remains
                    if (r.nextFloat() < 0.1f)
                    {
                        sItems.makeItem("trunk", x, y);

                        if (r.nextBoolean())
                            sItems.makeItem("branch", x, y);
                        if (r.nextBoolean())
                            sItems.makeItem("vine", x, y);
                    }
                    else
                    {
                        sItems.makeItem("tree", x, y);
                    }
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

        log.info("initialised");
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

    public static class CreatureTemplate
    {
        public String name;
        public String tag;

        public Strength     strength;
        public Agility      agility;
        public Constitution constitution;
        public Herbivore    herbivore;
        public Carnivore    carnivore;
        public Skin         skin;
        public Sight        sight;
        public AI           ai;
        public Group        group;
        public Sprite       sprite;
        public Player       player;
        public Inventory    inventory;
        public Underwater   underwater;
    }
}
