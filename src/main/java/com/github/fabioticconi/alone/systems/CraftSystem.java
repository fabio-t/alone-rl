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
import com.artemis.utils.IntBag;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.constants.WeaponType;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Author: Fabio Ticconi
 * Date: 05/11/17
 */
public class CraftSystem extends PassiveSystem
{
    ComponentMapper<Inventory> mInventory;
    ComponentMapper<Name>      mName;
    ComponentMapper<Equip>     mEquip;

    @Wire
    ObjectMapper mapper;

    HashMap<String, CraftItem> recipes;

    @Override
    protected void initialize()
    {
        try
        {
            loadRecipes();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<String> getRecipeNames()
    {
        try
        {
            loadRecipes();

            return new ArrayList<>(recipes.keySet());
        } catch (final IOException e)
        {
            e.printStackTrace();

            return List.of();
        }
    }

    public HashMap<String, CraftItem> getRecipes()
    {
        try
        {
            loadRecipes();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }

        return recipes;
    }

    private void loadRecipes() throws IOException
    {
        final InputStream fileStream = new FileInputStream("data/crafting.yml");

        recipes = mapper.readValue(fileStream, new TypeReference<HashMap<String, CraftItem>>()
        {
        });

        for (final Map.Entry<String, CraftItem> entry : recipes.entrySet())
        {
            final CraftItem temp = entry.getValue();
            temp.tag = entry.getKey();

            for (final String tempSource : temp.source)
            {
                switch (tempSource)
                {
                    case "stone":
                    case "branch":
                    case "vine":
                    case "trunk":
                        break;

                    default:
                        if (!recipes.keySet().contains(tempSource))
                            throw new RuntimeException("unknown item in source field: " + tempSource);
                }
            }

            for (final String tempTool : temp.tools)
            {
                switch (tempTool)
                {
                    case "stone":
                    case "branch":
                    case "vine":
                    case "trunk":
                        break;

                    default:
                        if (!recipes.keySet().contains(tempTool))
                            throw new RuntimeException("unknown item in tools field: " + tempTool);
                }
            }

            // System.out.println(entry.getKey() + " | " + entry.getValue());
        }
    }

    public boolean craftItem(final int entityId, final CraftItem itemRecipe)
    {
        final Inventory items = mInventory.get(entityId);

        if (items == null)
            return false;

        final IntBag tempSources = new IntBag(itemRecipe.source.length);
        final IntBag tempTools   = new IntBag(itemRecipe.tools.length);

        final int[] data = items.items.getData();
        for (int i = 0, size = items.items.size(); i < size; i++)
        {
            final int itemId = data[i];

            // we might only want an equipped weapon
            if (!mName.has(itemId))
                continue;

            final Name name = mName.get(itemId);

            int ii = 0;
            while (ii < itemRecipe.source.length)
            {
                if (tempSources.get(ii) < 0 && name.tag.equals(itemRecipe.source[ii]))
                {
                    tempSources.set(ii, itemId);

                    break; // item can be used only once
                }

                ii++;
            }

            // if ii == source.length, then it means that the previous loop completed
            // without setting itemId as a "source". So it's OK to evaluate whether it
            // can be used as a "tool".

            ii = 0;
            while (ii < itemRecipe.tools.length)
            {
                if (tempTools.get(ii) < 0 && name.tag.equals(itemRecipe.tools[ii]))
                {
                    tempTools.set(ii, itemId);

                    break; // item can be used only once
                }

                ii++;
            }
        }

        if (tempSources.size() < itemRecipe.source.length || tempTools.size() < itemRecipe.tools.length)
            return false;

        // TODO: here we should first create the object (using the internal fields.. we are still missing
        // some, eg sprite, if is weapon, if is wearable..

        // TODO: then we must destroy the items in the "source" array. Make sure to remove them from the map
        // AND from the inventory too.

        return true;
    }

    public static class CraftItem
    {
        public String   name;
        public String   tag;
        public String[] source;
        public String[] tools;
        public int n = 1;

        public boolean wearable;
        public Weapon  weapon;
        public Sprite  sprite;

        @Override
        public String toString()
        {
            return "CraftItem{" + "name='" + name + '\'' + ", tag='" + tag + '\'' + ", source=" +
                   Arrays.toString(source) + ", tools=" + Arrays.toString(tools) + ", n=" + n + '}';
        }
    }
}
