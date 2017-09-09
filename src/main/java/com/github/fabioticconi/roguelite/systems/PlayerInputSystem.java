package com.github.fabioticconi.roguelite.systems;

import com.artemis.ComponentMapper;
import com.artemis.managers.PlayerManager;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.roguelite.Roguelite;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.constants.Side;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import java.awt.event.KeyEvent;

public class PlayerInputSystem extends PassiveSystem
{
    ComponentMapper<Speed> mSpeed;

    MovementSystem movement;

    PlayerManager pManager;

    public float handleKeys(final BitVector keys)
    {
        // FIXME: hackish, very crappy but it should work
        final int pID = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final float speed = mSpeed.get(pID).value;

        if (keys.get(KeyEvent.VK_UP))
        {
            if (keys.get(KeyEvent.VK_LEFT))
            {
                // northwest
                return movement.moveTo(pID, speed, Side.NW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // northeast
                return movement.moveTo(pID, speed, Side.NE);
            }
            else
            {
                // north
                return movement.moveTo(pID, speed, Side.N);
            }
        }
        else if (keys.get(KeyEvent.VK_DOWN))
        {
            if (keys.get(KeyEvent.VK_LEFT))
            {
                // southwest
                return movement.moveTo(pID, speed, Side.SW);
            }
            else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // southeast
                return movement.moveTo(pID, speed, Side.SE);
            }
            else
            {
                // south
                return movement.moveTo(pID, speed, Side.S);
            }
        }
        else if (keys.get(KeyEvent.VK_RIGHT))
        {
            // northeast
            return movement.moveTo(pID, speed, Side.E);
        }
        else if (keys.get(KeyEvent.VK_LEFT))
        {
            // northwest
            return movement.moveTo(pID, speed, Side.W);
        }
        else if (keys.get(KeyEvent.VK_ESCAPE))
        {
            Roguelite.keepRunning = false;
        }

        return 0f;
    }
}
