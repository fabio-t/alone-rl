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
import com.github.fabioticconi.alone.messages.Message;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import java.awt.*;

/**
 * @author Fabio Ticconi
 */
public class RenderSystem extends PassiveSystem
{
    ComponentMapper<Player>   mPlayer;
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
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Player player  = mPlayer.get(playerId);
        final Position p     = mPosition.get(playerId);
        final int      sight = mSight.get(playerId).value;

        final int xmax = terminal.getWidthInCharacters();
        final int ymax = terminal.getHeightInCharacters();

        final int panelSize = 8;

        final int halfcols = xmax / 2;
        final int halfrows = (ymax - panelSize) / 2;

        int posX;
        int posY;

        Sprite sprite;
        Size   size;

        final LongSet cells = map.getVisibleCells(p.x, p.y, sight);

        for (int x = 0; x < xmax; x++)
        {
            for (int y = 0; y < ymax-panelSize; y++)
            {
                posX = p.x + x - halfcols;
                posY = p.y + y - halfrows;

                final long key = posX | ((long) posY << 32);

                if (map.contains(posX, posY))
                {
                    // render terrain
                    final Cell cell = map.get(posX, posY);
                    Color      tileFg;
                    final Color      tileBg;

                    if (cells.contains(key))
                    {
                        tileFg = cell.col;
                        tileBg = cell.bg;
                    }
                    else
                    {
                        tileFg = cell.col.darker().darker().darker();
                        tileBg = cell.bg.darker().darker().darker();
                    }

                    terminal.write(cell.c, x, y, tileFg, tileBg);

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

                            terminal.write(sprite.c, x, y, sprite.col, tileBg);

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

                        terminal.write(c, x, y, tileFg, tileBg);
                    }
                }
                else
                {
                    // pure black outside boundaries
                    terminal.write(' ', x, y);
                }
            }
        }

        // title:
        terminal.writeCenter("ALONE", 1);

        final Hunger  hunger  = mHunger.get(playerId);
        final Health  health  = mHealth.get(playerId);
        final Stamina stamina = mStamina.get(playerId);

        int x;

        final int yoff = 3;

        // health bar
        terminal.write('[', 0, yoff, Color.RED);
        for (x = 1; x < 11; x++)
        {
            if (x <= health.value * 10f / health.maxValue)
                terminal.write('=', x, yoff, Color.RED);
            else
                terminal.write(' ', x, yoff, Color.RED);
        }
        terminal.write(']', x, yoff, Color.RED);

        // stamina bar
        terminal.write('[', 0, yoff+1, Color.YELLOW);
        for (x = 1; x < 11; x++)
        {
            if (x <= stamina.value * 10f / stamina.maxValue)
                terminal.write('=', x, yoff+1, Color.YELLOW);
            else
                terminal.write(' ', x, yoff+1, Color.YELLOW);
        }
        terminal.write(']', x, yoff+1, Color.YELLOW);

        // hunger bar
        terminal.write(']', xmax-1, yoff, Color.ORANGE.darker());
        for (x = 1; x < 11; x++)
        {
            if (x <= hunger.value * 10f / hunger.maxValue)
                terminal.write('=', xmax-x-1, yoff, Color.ORANGE.darker());
            else
                terminal.write(' ', xmax-x-1, yoff, Color.ORANGE.darker());
        }
        terminal.write('[', xmax-x-1, yoff, Color.ORANGE.darker());

        // small panel
        for (int i = 1; i <= panelSize; i++)
        {
            if (player.messages.size() < i)
                terminal.clear(' ', 0, ymax-i, xmax, 1, Color.WHITE, Color.BLACK);
            else
            {
                final Message msg = player.messages.get(player.messages.size() - i);
                String smsg = msg.format();

                if (smsg.length() > xmax)
                    smsg = smsg.substring(0, xmax);

                terminal.write(smsg, 0, ymax-i, Color.WHITE, Color.BLACK);

                if (smsg.length() < xmax)
                    terminal.clear( ' ', smsg.length(), ymax-i, xmax-smsg.length(), 1, Color.WHITE, Color.BLACK);
            }
        }

        // player.messages.clear();
    }
}
