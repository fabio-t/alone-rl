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
package com.github.fabioticconi.roguelite.systems;

import asciiPanel.AsciiPanel;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.managers.PlayerManager;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Sprite;
import com.github.fabioticconi.roguelite.components.attributes.Sight;
import com.github.fabioticconi.roguelite.components.attributes.Size;
import com.github.fabioticconi.roguelite.constants.Cell;
import com.github.fabioticconi.roguelite.map.MapSystem;
import com.github.fabioticconi.roguelite.map.SingleGrid;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import java.awt.*;

/**
 * @author Fabio Ticconi
 */
public class RenderSystem extends PassiveSystem
{
    ComponentMapper<Position> mPosition;
    ComponentMapper<Sprite>   mSprite;
    ComponentMapper<Sight>    mSight;
    ComponentMapper<Size>     mSize;

    @Wire
    MapSystem  sMap;
    @Wire
    SingleGrid grid;

    PlayerManager pManager;

    public void display(final AsciiPanel terminal)
    {
        // FIXME: hackish, very crappy but it should work
        final int pID = pManager.getEntitiesOfPlayer("player").get(0).getId();

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
        Size   size;

        final LongSet cells = sMap.getVisibleCells(p.x, p.y, sight);

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
                    final Cell cell = sMap.get(pos_x, pos_y);

                    terminal.write(cell.c, x, y, cell.col);

                    final int entityId = grid.get(pos_x, pos_y);

                    if (entityId >= 0)
                    {
                        sprite = mSprite.get(entityId);
                        size = mSize.get(entityId);

                        if (sprite != null)
                        {
                            final char c = (size != null && size.value > 0) ?
                                               Character.toUpperCase(sprite.c) :
                                               sprite.c;

                            terminal.write(c, x, y, sprite.col);
                        }
                    }
                }
                else
                {
                    terminal.write(' ', x, y, Color.DARK_GRAY);
                }
            }
        }
    }
}
