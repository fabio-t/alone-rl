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

package com.github.fabioticconi.alone.screens;

import asciiPanel.AsciiPanel;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.managers.PlayerManager;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.Main;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.attributes.Sight;
import com.github.fabioticconi.alone.constants.Cell;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.map.MapSystem;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;
import com.github.fabioticconi.alone.messages.AbstractMessage;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.systems.*;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Stack;

public class PlayScreen extends AbstractScreen
{
    static final Logger log = LoggerFactory.getLogger(PlayScreen.class);

    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Player>   mPlayer;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Sprite>   mSprite;
    ComponentMapper<Sight>    mSight;
    ComponentMapper<Size>     mSize;
    ComponentMapper<Hunger>   mHunger;
    ComponentMapper<Health>   mHealth;
    ComponentMapper<Stamina>  mStamina;

    ActionSystem sAction;
    BumpSystem   sBump;
    ItemSystem   sItems;
    HungerSystem sHunger;
    ThrowSystem  sThrow;
    MapSystem    map;
    MessageSystem msg;

    @Wire
    SingleGrid   grid;
    @Wire
    MultipleGrid items;

    PlayerManager pManager;

    // FIXME only for debug..
    private float savedSpeed = -1f;

    public float handleKeys(final BitVector keys)
    {
        // FIXME: hackish, very crappy but it should work
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Stamina stamina = mStamina.get(playerId);

        if (keys.get(KeyEvent.VK_UP))
        {
            if (stamina.exhausted)
            {
                msg.send(playerId, new CannotMsg("move", "while exhausted"));

                return 0f;
            }

            if (keys.get(KeyEvent.VK_LEFT))
            {
                // northwest
                return sBump.bumpAction(playerId, Side.NW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // northeast
                return sBump.bumpAction(playerId, Side.NE);
            }
            else
            {
                // north
                return sBump.bumpAction(playerId, Side.N);
            }
        }
        else if (keys.get(KeyEvent.VK_DOWN))
        {
            if (stamina.exhausted)
            {
                msg.send(playerId, new CannotMsg("move", "while exhausted"));

                return 0f;
            }

            if (keys.get(KeyEvent.VK_LEFT))
            {
                // southwest
                return sBump.bumpAction(playerId, Side.SW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // southeast
                return sBump.bumpAction(playerId, Side.SE);
            }
            else
            {
                // south
                return sBump.bumpAction(playerId, Side.S);
            }
        }
        else if (keys.get(KeyEvent.VK_RIGHT))
        {
            if (stamina.exhausted)
            {
                msg.send(playerId, new CannotMsg("move", "while exhausted"));

                return 0f;
            }

            // east
            return sBump.bumpAction(playerId, Side.E);
        }
        else if (keys.get(KeyEvent.VK_LEFT))
        {
            if (stamina.exhausted)
            {
                msg.send(playerId, new CannotMsg("move", "while exhausted"));

                return 0f;
            }

            // west
            return sBump.bumpAction(playerId, Side.W);
        }
        else if (keys.get(KeyEvent.VK_G))
        {
            keys.clear(KeyEvent.VK_G);

            return sAction.act(sItems.get(playerId));
        }
        else if (keys.get(KeyEvent.VK_D))
        {
            keys.clear(KeyEvent.VK_D);

            return sAction.act(sItems.drop(playerId));
        }
        else if (keys.get(KeyEvent.VK_E))
        {
            keys.clear(KeyEvent.VK_E);

            return sAction.act(sHunger.devourClosestCorpse(playerId));
        }
        else if (keys.get(KeyEvent.VK_T))
        {
            keys.clear(KeyEvent.VK_T);

            return sAction.act(sThrow.throwWeapon(playerId));
        }
        else if (keys.get(KeyEvent.VK_L))
        {
            keys.clear(KeyEvent.VK_L);

            // TODO: this is targeting: it should be visible (eg, an overimposed X or border or something..)
            // so that the player can either look (just press enter) or throw (press t)

            return 0f;
        }
        else if (keys.get(KeyEvent.VK_ESCAPE))
        {
            // TODO should probably return the menu instead?

            Main.keepRunning = false;
        }
        else if (keys.get(KeyEvent.VK_F1))
        {
            keys.clear(KeyEvent.VK_F1);
            if (savedSpeed == -1f)
                savedSpeed = mSpeed.get(playerId).value;
            mSpeed.get(playerId).value = 0f; // FIXME to remove later, only for debug
        }
        else if (keys.get(KeyEvent.VK_F2))
        {
            keys.clear(KeyEvent.VK_F2);

            if (savedSpeed >= 0f)
                mSpeed.create(playerId).value = savedSpeed; // FIXME to remove later, only for debug
        }
        else if (keys.get(KeyEvent.VK_SPACE))
        {
            if (keys.get(KeyEvent.VK_CONTROL))
            {
                // Ctrl+Space means we are toggling the real-time mode
                Main.realtime = !Main.realtime;
                // as well as toggling the pause, of course
                Main.paused = !Main.paused;

                keys.clear(KeyEvent.VK_CONTROL);
            }
            else
            {
                // Space alone has two meanings:

                // in real-time mode, space means pause/unpause the game, while
                // in turn-based mode, space means unpause the game until SPACE is released
                Main.paused = Main.realtime && !Main.paused;
            }

            keys.clear(KeyEvent.VK_SPACE);
        }

        return 0f;
    }

    public void display(final AsciiPanel terminal)
    {
        // FIXME: hackish, very crappy
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Player   player = mPlayer.get(playerId);
        final Position p      = mPosition.get(playerId);
        final int      sight  = mSight.get(playerId).value;

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
                    final Cell  cell = map.get(posX, posY);
                    Color       tileFg;
                    final Color tileBg;

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

        // small panel: combat log
        final Stack<AbstractMessage> messages = player.messages;
        for (int i = 1; i <= panelSize; i++)
        {
            if (messages.size() < i)
            {
                terminal.clear(' ', 0, ymax - i, xmax, 1, Color.WHITE, Color.BLACK);

                continue;
            }

            final AbstractMessage msg = messages.get(messages.size() - i);

            if (msg.distance > sight)
            {
                terminal.clear(' ', 0, ymax - i, xmax, 1, Color.WHITE, Color.BLACK);

                continue;
            }

            String smsg = msg.format();

            if (smsg.length() >= xmax)
                smsg = smsg.substring(0, xmax-1);

            terminal.write(smsg, 0, ymax-i, msg.fgCol, msg.bgCol);

            if (smsg.length() < xmax)
                terminal.clear( ' ', smsg.length(), ymax-i, xmax-smsg.length(), 1, Color.WHITE, Color.BLACK);
        }

        // FIXME: this is crap, inefficient, horrible. Must change the Stack with a better data structure.
        for (int i = 0; i < messages.size() - panelSize; i++)
        {
            messages.remove(i);
        }

        // player.messages.clear();
    }
}
