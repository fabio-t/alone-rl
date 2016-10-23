package com.github.fabioticconi.roguelite;

import java.io.IOException;
import java.util.Random;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.github.fabioticconi.roguelite.behaviours.ChaseBehaviour;
import com.github.fabioticconi.roguelite.behaviours.FleeBehaviour;
import com.github.fabioticconi.roguelite.behaviours.FlockBehaviour;
import com.github.fabioticconi.roguelite.behaviours.GrazeBehaviour;
import com.github.fabioticconi.roguelite.behaviours.WanderBehaviour;
import com.github.fabioticconi.roguelite.map.EntityGrid;
import com.github.fabioticconi.roguelite.map.Map;
import com.github.fabioticconi.roguelite.systems.AISystem;
import com.github.fabioticconi.roguelite.systems.BootstrapSystem;
import com.github.fabioticconi.roguelite.systems.GroupSystem;
import com.github.fabioticconi.roguelite.systems.HungerSystem;
import com.github.fabioticconi.roguelite.systems.MovementSystem;
import com.github.fabioticconi.roguelite.systems.PlayerInputSystem;
import com.github.fabioticconi.roguelite.systems.RenderSystem;

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
        config.setSystem(GroupSystem.class);
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

        // FIXME: https://github.com/TomGrill/logic-render-game-loop
        // needs to modify that, so that I can divide systems in three groups:
        // input collection/processing, logic, output sending
        // this is because the first and the last will be only processed once,
        // while the logic ones can be re-processed until the lag is gone

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
