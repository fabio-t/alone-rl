package com.github.fabioticconi.roguelike;

import java.io.IOException;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.github.fabioticconi.roguelike.map.Map;
import com.github.fabioticconi.roguelike.systems.BootstrapSystem;
import com.github.fabioticconi.roguelike.systems.MovementSystem;
import com.github.fabioticconi.roguelike.systems.PlayerInputSystem;
import com.github.fabioticconi.roguelike.systems.RenderSystem;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;

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
        final float delta = 1.0f / 15.0f;

        final WorldConfiguration config;
        config = new WorldConfiguration();
        config.register(new Map());
        config.setSystem(BootstrapSystem.class);
        config.setSystem(PlayerInputSystem.class);
        config.setSystem(MovementSystem.class);
        config.setSystem(RenderSystem.class);
        final World world = new World(config);

        while (keepRunning)
        {
            world.setDelta(delta);
            world.process();
        }

        System.exit(0);
    }
}
