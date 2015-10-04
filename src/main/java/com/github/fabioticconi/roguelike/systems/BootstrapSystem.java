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

import com.artemis.BaseSystem;
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelike.components.Player;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.map.Map;

/**
 *
 * @author Fabio Ticconi
 */
public class BootstrapSystem extends BaseSystem
{
    @Wire
    Map map;

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.BaseSystem#processSystem()
     */
    @Override
    protected void processSystem()
    {
        final int pID = world.create();
        final EntityEdit edit = world.edit(pID);
        edit.create(Player.class);
        edit.add(new Position(map.maxX / 2, map.maxY / 2));

        // this must be only run once
        setEnabled(false);
    }
}
