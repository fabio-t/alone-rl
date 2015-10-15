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

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelike.components.Player;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.constants.Options;
import com.github.fabioticconi.roguelike.map.Cell;
import com.github.fabioticconi.roguelike.map.Map;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;

/**
 *
 * @author Fabio Ticconi
 */
public class RenderSystem extends BaseEntitySystem
{
    public static final TextCharacter PLAYER = new TextCharacter('@').withForegroundColor(TextColor.ANSI.GREEN)
                                                                     .withModifier(SGR.BOLD);

    ComponentMapper<Position> mPosition;
    ComponentMapper<Player>   mPlayer;

    @Wire
    Map map;

    Terminal     terminal;
    Screen       screen;
    TextGraphics graphics;

    /**
     * @param aspect
     */
    public RenderSystem()
    {
        super(Aspect.all(Position.class, Player.class));
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

        try
        {
            // FIXME: get terminal size from config
            final DefaultTerminalFactory factory = new DefaultTerminalFactory();
            factory.setInitialTerminalSize(new TerminalSize(Options.TERMINAL_SIZE_X, Options.TERMINAL_SIZE_Y));
            factory.setSwingTerminalFrameFontConfiguration(SwingTerminalFontConfiguration.newInstance(Options.FONT));
            terminal = factory.createTerminal();
            screen = new TerminalScreen(terminal);

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

        final TerminalSize tsize = screen.getTerminalSize();

        final int halfcols = tsize.getColumns() / 2;
        final int halfrows = tsize.getRows() / 2;

        final int xmax = tsize.getColumns();
        final int ymax = tsize.getRows();

        for (int x = 0; x < xmax; x++)
        {
            for (int y = 0; y < ymax; y++)
            {
                final Cell cell = map.get(p.x + x - halfcols, p.y + y - halfrows);
                graphics.setCharacter(x, y, cell.c);
            }
        }

        graphics.setCharacter(halfcols, halfrows, PLAYER);

        try
        {
            screen.refresh();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }

        setEnabled(false);
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
        screen.clear();
        try
        {
            screen.stopScreen();
        } catch (final IOException e)
        {
            e.printStackTrace();
        }
    }
}
