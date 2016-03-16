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

import java.io.IOException;
import java.util.List;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalSystem;
import com.github.fabioticconi.roguelike.components.Player;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Sprite;
import com.github.fabioticconi.roguelike.constants.Cell;
import com.github.fabioticconi.roguelike.constants.Options;
import com.github.fabioticconi.roguelike.map.EntityGrid;
import com.github.fabioticconi.roguelike.map.Map;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.SimpleTerminalResizeListener;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;

/**
 *
 * @author Fabio Ticconi
 */
public class RenderSystem extends IntervalSystem
{
    ComponentMapper<Position> mPosition;
    ComponentMapper<Sprite>   mSprite;
    ComponentMapper<Player>   mPlayer;

    @Wire
    Map                       map;
    @Wire
    EntityGrid                grid;

    Terminal                  terminal;
    Screen                    screen;
    TextGraphics              graphics;

    /**
     * @param aspect
     */
    public RenderSystem(final float interval)
    {
        super(Aspect.all(Position.class, Player.class), interval);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.BaseSystem#initialize()
     */
    @Override
    protected void initialize()
    {
        super.initialize();

        final int size_x = Options.TERMINAL_SIZE_X;
        final int size_y = Options.TERMINAL_SIZE_Y;

        try
        {
            // FIXME: get terminal size from config
            final DefaultTerminalFactory factory = new DefaultTerminalFactory();
            factory.setInitialTerminalSize(new TerminalSize(size_x, size_y));
            factory.setTerminalEmulatorFontConfiguration(AWTTerminalFontConfiguration.newInstance(Options.FONT));
            terminal = factory.createTerminal();
            terminal.setCursorVisible(false);
            terminal.addResizeListener(new SimpleTerminalResizeListener(terminal.getTerminalSize()));
            screen = new TerminalScreen(terminal);

            screen.doResizeIfNecessary();

            screen.startScreen();
            screen.clear();

        } catch (final IOException e)
        {
            e.printStackTrace();

            throw new RuntimeException("failed rendering");
        }

        graphics = screen.newTextGraphics();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.artemis.BaseSystem#processSystem()
     */
    @Override
    protected void processSystem()
    {
        final int pID = subscription.getEntities().get(0);

        final Position p = mPosition.get(pID);
        final Sprite s = mSprite.get(pID);

        screen.doResizeIfNecessary();

        final TerminalSize tsize = screen.getTerminalSize();

        final int xmax = tsize.getColumns();
        final int ymax = tsize.getRows();

        // final int xmax = Options.TERMINAL_SIZE_X;
        // final int ymax = Options.TERMINAL_SIZE_Y;

        final int halfcols = xmax / 2;
        final int halfrows = ymax / 2;

        int pos_x;
        int pos_y;

        Sprite sprite;

        List<Integer> entities;

        for (int x = 0; x < xmax; x++)
        {
            for (int y = 0; y < ymax; y++)
            {
                pos_x = p.x + x - halfcols;
                pos_y = p.y + y - halfrows;

                // render terrain
                final Cell cell = map.get(pos_x, pos_y);
                graphics.setCharacter(x, y, cell.c);

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
                        graphics.setCharacter(x, y, sprite.c);

                        // only show the first showable entity on each cell
                        break;
                    }
                }
            }
        }

        // in the middle cell we always show the player sprite
        graphics.setCharacter(halfcols, halfrows, s.c);

        // for (int i = Options.OUTPUT_SIZE_Y - 2; i > r.nextInt(10); i--)
        // {
        // graphics.putString(xmax + 2, i, String.format("blablablasdjasdaskdj %d", i));
        // }

        // refresh the screen, and set the terminal ready for update
        try
        {
            screen.refresh();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }

        // setEnabled(false);
    }

    public KeyStroke getInput()
    {
        try
        {
            return screen.pollInput();
        } catch (final IOException e)
        {
            e.printStackTrace();

            return null;
        }
    }

    public void close()
    {
        // screen.clear();
        try
        {
            screen.stopScreen();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }
    }
}
