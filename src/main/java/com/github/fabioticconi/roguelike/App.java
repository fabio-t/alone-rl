package com.github.fabioticconi.roguelike;

import java.io.IOException;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.github.fabioticconi.roguelike.systems.BootstrapSystem;
import com.github.fabioticconi.roguelike.systems.MovementSystem;
import com.github.fabioticconi.roguelike.systems.PlayerInputSystem;
import com.github.fabioticconi.roguelike.systems.RenderSystem;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

/**
 * Hello world!
 *
 */
public class App
{
    public static final TextCharacter PLAYER = new TextCharacter('@').withForegroundColor(TextColor.ANSI.GREEN)
                                                                     .withModifier(SGR.BOLD);

    public static boolean keepRunning = true;

    public static void main(final String[] args) throws IOException
    {
        // 60 FPS
        final float delta = 1.0f / 60.0f;

        final WorldConfiguration config;
        config = new WorldConfiguration();
        config.setSystem(BootstrapSystem.class);
        config.setSystem(PlayerInputSystem.class);
        config.setSystem(MovementSystem.class);
        config.setSystem(RenderSystem.class);
        final World world = new World(config);

        final int playerID = world.create();
        world.edit(playerID);

        // FIXME: rendering.. to be moved somewhere else

        final DefaultTerminalFactory factory = new DefaultTerminalFactory();
        factory.setInitialTerminalSize(new TerminalSize(150, 50));

        final Terminal terminal = factory.createTerminal();
        final Screen screen = new TerminalScreen(terminal);

        final TerminalSize tsize = screen.getTerminalSize();

        final TextGraphics tGraphics = screen.newTextGraphics();

        screen.startScreen();
        screen.clear();

        tGraphics.fill('.');
        TerminalPosition pos = new TerminalPosition(tsize.getColumns() / 2, tsize.getRows() / 2);
        tGraphics.setCharacter(pos, PLAYER);

        screen.refresh();

        KeyType key;
        while ((key = screen.readInput().getKeyType()) != KeyType.Escape)
        {
            tGraphics.setCharacter(pos, '.');

            switch (key)
            {
                case ArrowDown:
                    if (pos.getRow() < tsize.getRows() - 1)
                    {
                        pos = pos.withRelativeRow(1);
                    }

                    break;
                case ArrowUp:
                    if (pos.getRow() > 0)
                    {
                        pos = pos.withRelativeRow(-1);
                    }

                    break;
                case ArrowLeft:
                    if (pos.getColumn() > 0)
                    {
                        pos = pos.withRelativeColumn(-1);
                    }

                    break;
                case ArrowRight:
                    if (pos.getColumn() < tsize.getColumns() - 1)
                    {
                        pos = pos.withRelativeColumn(1);
                    }

                    break;
                default:
                    break;
            }

            tGraphics.setCharacter(pos, PLAYER);

            screen.refresh();

            world.setDelta(delta);
            world.process();
        }

        screen.stopScreen();
    }

    private void handleInput(final KeyType key)
    {

    }
}
