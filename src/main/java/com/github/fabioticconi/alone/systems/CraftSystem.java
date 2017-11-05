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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.github.fabioticconi.alone.screens.CraftItemScreen;
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
    HashMap<String, CraftItem> recipes;

    public CraftSystem() throws IOException
    {
        loadRecipes();
    }

    public List<String> getRecipeNames()
    {
        if (recipes == null || recipes.isEmpty())
            return List.of();

        return new ArrayList<>(recipes.keySet());
    }

    private void loadRecipes() throws IOException
    {
        // TODO we can actually instantiate the factory and mapper in the Main and inject/Wire them

        final InputStream  fileStream = new FileInputStream("data/crafting.yml");
        final YAMLFactory  factory    = new YAMLFactory();
        final ObjectMapper mapper     = new ObjectMapper(factory);

        recipes = mapper.readValue(fileStream, new TypeReference<HashMap<String, CraftItem>>(){});

        for (final Map.Entry<String, CraftItem> entry : recipes.entrySet())
        {
            System.out.println(entry.getKey() + " | " + entry.getValue());
        }
    }

    public static class CraftItem
    {
        public String name;
        public String source;
        public String tool;
        public int n = 1;

        @Override
        public String toString()
        {
            return "CraftItem{" + "name='" + name + '\'' + ", source='" + source + '\'' + ", tool='" + tool + '\'' +
                   ", n=" + n + '}';
        }
    }
}
