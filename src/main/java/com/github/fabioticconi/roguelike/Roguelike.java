package com.github.fabioticconi.roguelike;

import java.io.IOException;
import java.util.Random;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.github.fabioticconi.roguelike.behaviours.ChaseBehaviour;
import com.github.fabioticconi.roguelike.behaviours.FleeBehaviour;
import com.github.fabioticconi.roguelike.behaviours.FlockBehaviour;
import com.github.fabioticconi.roguelike.behaviours.GrazeBehaviour;
import com.github.fabioticconi.roguelike.behaviours.WanderBehaviour;
import com.github.fabioticconi.roguelike.map.EntityGrid;
import com.github.fabioticconi.roguelike.map.Map;
import com.github.fabioticconi.roguelike.systems.AISystem;
import com.github.fabioticconi.roguelike.systems.BootstrapSystem;
import com.github.fabioticconi.roguelike.systems.HungerSystem;
import com.github.fabioticconi.roguelike.systems.MovementSystem;
import com.github.fabioticconi.roguelike.systems.PlayerInputSystem;
import com.github.fabioticconi.roguelike.systems.RenderSystem;

/**
 * Hello world!
 *
 */
public class Roguelike
{
    public static boolean keepRunning = true;

    public static void main(final String[] args) throws IOException
    {
        final WorldConfiguration config;
        config = new WorldConfiguration();
        // POJO
        config.register(new Map());
        config.register(new EntityGrid());
        config.register(new Random());
        // systems
        config.setSystem(BootstrapSystem.class);
        config.setSystem(PlayerInputSystem.class);
        config.setSystem(new HungerSystem(5f));
        config.setSystem(AISystem.class);
        config.setSystem(MovementSystem.class);
        config.setSystem(new RenderSystem(0.75f));
        // behaviours
        config.setSystem(FleeBehaviour.class);
        config.setSystem(GrazeBehaviour.class);
        config.setSystem(ChaseBehaviour.class);
        config.setSystem(FlockBehaviour.class);
        config.setSystem(WanderBehaviour.class);

        final World world = new World(config);

        final float FPS = 25.0f;
        final float frameDuration = 1000.0f / FPS;
        final float dt = frameDuration / 1000.0f;

        long previousTime = System.currentTimeMillis();
        long currentTime;

        float lag = 0.0f;
        float elapsed;

        while (keepRunning)
        {
            currentTime = System.currentTimeMillis();
            elapsed = currentTime - previousTime;
            previousTime = currentTime;

            if (elapsed > 250f)
            {
                System.out.println("lagging behind: " + elapsed);
                elapsed = 250f;
            }

            lag += elapsed;

            world.getSystem(RenderSystem.class).setEnabled(false);

            while (lag >= frameDuration)
            {
                world.setDelta(dt);
                world.process();

                lag -= frameDuration;
            }

            world.getSystem(RenderSystem.class).setEnabled(true);
            world.getSystem(RenderSystem.class).process();
        }

        System.exit(0);
    }
}
