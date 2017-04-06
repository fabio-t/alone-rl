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

import asciiPanel.AsciiPanel;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.managers.PlayerManager;
import com.github.fabioticconi.roguelite.components.Player;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Sight;
import com.github.fabioticconi.roguelite.components.Sprite;
import com.github.fabioticconi.roguelite.constants.Cell;
import com.github.fabioticconi.roguelite.map.EntityGrid;
import com.github.fabioticconi.roguelite.map.Map;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import java.awt.*;
import java.util.Set;

/**
 * @author Fabio Ticconi
 */
public class RenderSystem extends PassiveSystem
{
    ComponentMapper<Position> mPosition;
    ComponentMapper<Sprite>   mSprite;
    ComponentMapper<Sight>    mSight;

    @Wire Map        map;
    @Wire EntityGrid grid;

    PlayerManager pManager;

    Aspect aspect;

    @Override protected void initialize()
    {
        aspect = Aspect.all(Position.class, Player.class).build(world);
    }

    public void display(final AsciiPanel terminal)
    {
        // FIXME: hackish, very crappy but it should work
        int pID = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Position p     = mPosition.get(pID);
        final int      sight = mSight.get(pID).value;

        final int xmax = terminal.getWidthInCharacters();
        final int ymax = terminal.getHeightInCharacters();

        //         final int xmax = Options.TERMINAL_SIZE_X;
        //         final int ymax = Options.TERMINAL_SIZE_Y;

        final int halfcols = xmax / 2;
        final int halfrows = ymax / 2;

        int pos_x;
        int pos_y;

        Sprite sprite;

        Set<Integer> entities;

        final Set<Long> cells = map.getVisibleCells(p.x, p.y, sight);

        // FIXME we should just fill all as default,
        // and then just set the visible ones (a single for, not
        // a double one) by calling grid.getEntities(cells)
        // and getting the Position of those entities.
        // not sure if this is more efficient though,
        // technically the below getEntities doesn't allocate
        // a new set so it should be fine..
        for (int x = 0; x < xmax; x++)
        {
            for (int y = 0; y < ymax; y++)
            {
                pos_x = p.x + x - halfcols;
                pos_y = p.y + y - halfrows;

                final long key = pos_x | ((long) pos_y << 32);

                if (cells.contains(key))
                {
                    // render terrain
                    final Cell cell = map.get(pos_x, pos_y);

                    terminal.write(cell.c.getCharacter(), x, y, cell.c.getForegroundColor().toColor());

                    entities = grid.getEntities(pos_x, pos_y);

                    if (entities == null)
                    {
                        continue;
                    }

                    // render other visible entities
                    for (final int eID : entities)
                    {
                        sprite = mSprite.get(eID);

                        if (sprite != null)
                        {
                            terminal.write(sprite.c.getCharacter(), x, y, sprite.c.getForegroundColor().toColor());

                            // only show the first showable entity on each cell
                            break;
                        }
                    }
                }
                else
                {
                    terminal.write(' ', x, y, Color.darkGray);
                }
            }
        }
    }
}
