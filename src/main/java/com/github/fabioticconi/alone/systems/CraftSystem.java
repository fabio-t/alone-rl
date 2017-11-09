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

import com.artemis.annotations.Wire;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            System.out.println(entry.getKey() + " | " + entry.getValue());
        }
    }

    public static class CraftItem
    {
        public String   name;
        public String[] source;
        public String[] tools;
        public int n = 1;

        @Override
        public String toString()
        {
            return "consumes:" + Arrays.toString(source) + "|tools:" + Arrays.toString(tools);
        }
    }
}
