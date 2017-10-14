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

import asciiPanel.AsciiPanel;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.managers.PlayerManager;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.attributes.Sight;
import com.github.fabioticconi.alone.constants.Cell;
import com.github.fabioticconi.alone.map.MapSystem;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;
import it.unimi.dsi.fastutil.ints.IntSet;
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
    ComponentMapper<Hunger>   mHunger;
    ComponentMapper<Health>   mHealth;
    ComponentMapper<Stamina>  mStamina;

    MapSystem map;

    @Wire
    SingleGrid   grid;
    @Wire
    MultipleGrid items;

    PlayerManager pManager;

    public void display(final AsciiPanel terminal)
    {
        // FIXME: hackish, very crappy
        final int pID = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Position p     = mPosition.get(pID);
        final int      sight = mSight.get(pID).value;

        final int xmax = terminal.getWidthInCharacters();
        final int ymax = terminal.getHeightInCharacters();

        final int halfcols = xmax / 2;
        final int halfrows = ymax / 2;

        int posX;
        int posY;

        Sprite sprite;
        Size   size;

        final LongSet cells = map.getVisibleCells(p.x, p.y, sight);

        for (int x = 0; x < xmax; x++)
        {
            for (int y = 0; y < ymax; y++)
            {
                posX = p.x + x - halfcols;
                posY = p.y + y - halfrows;

                final long key = posX | ((long) posY << 32);

                if (map.contains(posX, posY))
                {
                    // render terrain
                    final Cell cell = map.get(posX, posY);
                    Color      tileFg;

                    if (cells.contains(key))
                    {
                        tileFg = cell.col;
                    }
                    else
                    {
                        tileFg = cell.col.darker().darker().darker();
                    }

                    terminal.write(cell.c, x, y, tileFg);

                    if (cells.contains(key))
                    {
                        // then render the items
                        final IntSet entities = items.get(key);

                        // we actually only render the first (renderable) item
                        for (final int firstItemId : entities)
                        {
                            if (firstItemId < 0)
                                continue;

                            sprite = mSprite.get(firstItemId);

                            if (sprite == null)
                                continue;

                            terminal.write(sprite.c, x, y, sprite.col);

                            break;
                        }
                    }

                    // on top, render the obstacles/trees/walls etc

                    final int entityId = grid.get(posX, posY);

                    if (entityId >= 0)
                    {
                        sprite = mSprite.get(entityId);

                        if (sprite == null)
                            continue;

                        if (!cells.contains(key) && !sprite.shadowView)
                            continue;
                        else if (cells.contains(key))
                        {
                            tileFg = sprite.col;
                        }
                        else
                        {
                            tileFg = sprite.col.darker().darker().darker();
                        }

                        size = mSize.get(entityId);

                        final char c = (size != null && size.value > 0) ? Character.toUpperCase(sprite.c) : sprite.c;

                        terminal.write(c, x, y, tileFg);
                    }
                }
                else
                {
                    // pure black outside boundaries
                    terminal.write(' ', x, y);
                }
            }
        }

        final Hunger  hunger  = mHunger.get(pID);
        final Health  health  = mHealth.get(pID);
        final Stamina stamina = mStamina.get(pID);

        // hunger bar
        terminal.write('[', 0, 0, Color.ORANGE.darker());
        int x;
        for (x = 1; x < 11; x++)
        {
            if (x <= hunger.value * 10f / hunger.maxValue)
                terminal.write('=', x, 0, Color.ORANGE.darker());
            else
                terminal.write(' ', x, 0, Color.ORANGE.darker());
        }
        terminal.write(']', x, 0, Color.ORANGE.darker());

        // health bar
        terminal.write('[', 0, 1, Color.RED);
        for (x = 1; x < 11; x++)
        {
            if (x <= health.value * 10f / health.maxValue)
                terminal.write('=', x, 1, Color.RED);
            else
                terminal.write(' ', x, 1, Color.RED);
        }
        terminal.write(']', x, 1, Color.RED);

        // stamina bar
        terminal.write('[', 0, 2, Color.YELLOW);
        for (x = 1; x < 11; x++)
        {
            if (x <= stamina.value * 10f / stamina.maxValue)
                terminal.write('=', x, 2, Color.YELLOW);
            else
                terminal.write(' ', x, 2, Color.YELLOW);
        }
        terminal.write(']', x, 2, Color.YELLOW);
    }
}
