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
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.Target;
import com.github.fabioticconi.alone.components.attributes.Sight;
import com.github.fabioticconi.alone.systems.ItemSystem;
import com.github.fabioticconi.alone.systems.MapSystem;
import com.github.fabioticconi.alone.systems.ThrowSystem;
import com.github.fabioticconi.alone.utils.Coords;
import rlforj.math.Point;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static com.github.fabioticconi.alone.constants.Side.*;

/**
 * Author: Fabio Ticconi
 * Date: 04/11/17
 */
public class LookScreen extends PlayScreen
{
    ComponentMapper<Target>   mTarget;
    ComponentMapper<Position> mPos;
    ComponentMapper<Sight>    mSight;

    ThrowSystem sThrow;
    MapSystem   map;
    ItemSystem  sItem;

    @Override
    public float handleKeys(final BitVector keys)
    {
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Position p = mPos.get(playerId);

        Target t = mTarget.get(playerId);

        if (t == null)
        {
            t = mTarget.create(playerId);

            t.pos.set(p);
        }

        if (keys.get(KeyEvent.VK_T))
        {
            keys.clear();

            screen.select(PlayScreen.class);

            final float cooldown = sAction.act(sThrow.throwAt(playerId));

            mTarget.remove(playerId);

            return cooldown;
        }
        else if (keys.get(KeyEvent.VK_ESCAPE) || keys.get(KeyEvent.VK_L))
        {
            keys.clear();

            screen.select(PlayScreen.class);

            mTarget.remove(playerId);

            return 0f;
        }
        else
        {
            final Position backup = new Position().set(t.pos);

            if (keys.get(KeyEvent.VK_UP))
                t.pos.add(N);
            if (keys.get(KeyEvent.VK_DOWN))
                t.pos.add(S);
            if (keys.get(KeyEvent.VK_LEFT))
                t.pos.add(W);
            if (keys.get(KeyEvent.VK_RIGHT))
                t.pos.add(E);

            final Sight sight = mSight.get(playerId);

            if (Coords.distancePseudoEuclidean(p.x, p.y, t.pos.x, t.pos.y) > sight.value ||
                map.getLineOfSight(p.x, p.y, t.pos.x, t.pos.y) == null)
                t.pos.set(backup);
            else
            {
                // TODO: log a message with a short description of what's there, eg:
                // "You see a WOLF on the grass/hill/mountain"
                // "You see a tree"
                // "You see a boulder"
                // "You see a stone on the ground"

                // msg.send(playerId, new Msg("see a "));
            }

            keys.clear();
        }

        return 0f;
    }

    @Override
    public void display(final AsciiPanel terminal)
    {
        // let's draw the usual PlayScreen first
        super.display(terminal);

        // then on top of it we draw the line between the player position and
        // the target

        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final Position pos = mPos.get(playerId);
        final Target   t   = mTarget.get(playerId);

        // the first time it displays, handleKeys probably hasn't been called yet
        if (t == null)
            return;

        final int xmax      = terminal.getWidthInCharacters();
        final int ymax      = terminal.getHeightInCharacters();
        final int panelSize = 8;

        final int playerX = xmax / 2;
        final int playerY = (ymax - panelSize) / 2;

        terminal.write('@', playerX, playerY, Color.BLACK, Color.LIGHT_GRAY);

        if (pos.equals(t.pos))
            return;

        final List<Point> los = map.getLineOfSight(pos.x, pos.y, t.pos.x, t.pos.y);

        // we skip the first point for that's always the starting point
        for (final Point p : los.subList(1, los.size()))
        {
            final int x = playerX + (p.x - pos.x);
            final int y = playerY + (p.y - pos.y);

            terminal.write(' ', x, y, Color.WHITE, Color.LIGHT_GRAY);
        }
    }
}
