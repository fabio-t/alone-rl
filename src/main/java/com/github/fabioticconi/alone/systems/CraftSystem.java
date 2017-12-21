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
import com.artemis.annotations.Wire;
import com.artemis.utils.IntBag;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fabioticconi.alone.components.Inventory;
import com.github.fabioticconi.alone.components.Name;
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

    ItemSystem sItems;

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

    public void loadRecipes() throws IOException
    {
        final InputStream fileStream = new FileInputStream("data/crafting.yml");

        recipes = mapper.readValue(fileStream, new TypeReference<HashMap<String, CraftItem>>()
        {
        });

        // reload item templates
        sItems.loadTemplates();

        for (final Map.Entry<String, CraftItem> entry : recipes.entrySet())
        {
            final CraftItem temp = entry.getValue();
            temp.tag = entry.getKey();

            for (final String tempSource : temp.sources)
            {
                if (!sItems.templates.keySet().contains(tempSource))
                    throw new RuntimeException("unknown item in sources field: " + tempSource);
            }

            for (final String tempTool : temp.tools)
            {
                if (!sItems.templates.keySet().contains(tempTool))
                    throw new RuntimeException("unknown item in tools field: " + tempTool);
            }
        }
    }

    public int craftItem(final int entityId, final CraftItem itemRecipe)
    {
        final Inventory inv = mInventory.get(entityId);

        if (inv == null)
            return -1;

        final IntBag tempSources = new IntBag(itemRecipe.sources.length);
        Arrays.fill(tempSources.getData(), -1);
        final IntBag tempTools = new IntBag(itemRecipe.tools.length);
        Arrays.fill(tempTools.getData(), -1);

        final int[] data = inv.items.getData();
        for (int i = 0, size = inv.items.size(); i < size; i++)
        {
            final int itemId = data[i];

            if (!mName.has(itemId))
                continue;

            final Name name = mName.get(itemId);

            int         ii      = 0;
            final int[] sources = tempSources.getData();
            while (ii < itemRecipe.sources.length)
            {
                System.out.println("sources " + ii);
                System.out.println(name.tag + " | " + itemRecipe.sources[ii]);
                System.out.println(sources[ii]);
                if (sources[ii] < 0 && name.tag.equals(itemRecipe.sources[ii]))
                {
                    tempSources.set(ii, itemId);

                    break; // source items can only be "used" once
                }

                ii++;
            }

            // if ii == sources.length, then it means that the previous loop completed
            // without setting itemId as a "source". So it's OK to evaluate whether it
            // can be used as a "tool".
            // conversely, if ii < sources.length then itemId is a "source" and we cannot use it
            // as tool.
            if (ii < sources.length)
                continue;

            ii = 0;
            final int[] tools = tempTools.getData();
            while (ii < itemRecipe.tools.length)
            {
                System.out.println("tools " + ii);
                System.out.println(name.tag + " | " + itemRecipe.tools[ii]);
                System.out.println(tools[ii]);
                if (tools[ii] < 0 && name.tag.equals(itemRecipe.tools[ii]))
                {
                    tempTools.set(ii, itemId);

                    break; // tool items can only be used once
                }

                ii++;
            }
        }

        if (tempSources.size() < itemRecipe.sources.length || tempTools.size() < itemRecipe.tools.length)
            return -1;

        final int id = sItems.makeItem(itemRecipe.tag);

        if (id < 0)
            return -1;

        for (final int sourceId : tempSources.getData())
        {
            // destroying source items
            world.delete(sourceId);
            inv.items.removeValue(sourceId);
        }

        return id;
    }

    public static class CraftItem
    {
        public String   tag;
        public String[] sources;
        public String[] tools;

        @Override
        public String toString()
        {
            return "CraftItem{" + "tag='" + tag + '\'' + ", sources=" + Arrays.toString(sources) + ", tools=" +
                   Arrays.toString(tools) + '}';
        }
    }
}
