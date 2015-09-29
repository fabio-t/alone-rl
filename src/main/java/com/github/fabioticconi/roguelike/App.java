package com.github.fabioticconi.roguelike;

import java.io.IOException;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
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
    public static void main(final String[] args) throws IOException
    {
        final Terminal terminal = new DefaultTerminalFactory().createTerminal();
        final Screen screen = new TerminalScreen(terminal);

        final TextGraphics tGraphics = screen.newTextGraphics();

        screen.startScreen();
        screen.clear();

        tGraphics.drawRectangle(new TerminalPosition(3, 3), new TerminalSize(10, 10), '*');
        screen.refresh();

        KeyStroke key;
        while ((key = screen.readInput()).getKeyType() != KeyType.Escape)
        {
            ;
        }
        screen.stopScreen();
    }
}
