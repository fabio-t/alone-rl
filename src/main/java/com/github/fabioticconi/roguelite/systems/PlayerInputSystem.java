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

    public void handleKeys(BitVector keys)
    {
        // FIXME: hackish, very crappy but it should work
        int pID = pManager.getEntitiesOfPlayer("player").get(0).getId();

        final float speed = mSpeed.get(pID).value;

        if (keys.get(KeyEvent.VK_UP))
        {
            if (keys.get(KeyEvent.VK_LEFT))
            {
                // northwest
                movement.moveTo(pID, speed, Side.NW);
            } else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // northeast
                movement.moveTo(pID, speed, Side.NE);
            } else
            {
                // north
                movement.moveTo(pID, speed, Side.N);
            }
        } else if (keys.get(KeyEvent.VK_DOWN))
        {
            if (keys.get(KeyEvent.VK_LEFT))
            {
                // southwest
                movement.moveTo(pID, speed, Side.SW);
            } else if (keys.get(KeyEvent.VK_RIGHT))
            {
                // southeast
                movement.moveTo(pID, speed, Side.SE);
            } else
            {
                // south
                movement.moveTo(pID, speed, Side.S);
            }
        } else if (keys.get(KeyEvent.VK_RIGHT))
        {
            // northeast
            movement.moveTo(pID, speed, Side.E);
        } else if (keys.get(KeyEvent.VK_LEFT))
        {
            // northwest
            movement.moveTo(pID, speed, Side.W);
        } else if (keys.get(KeyEvent.VK_ESCAPE))
        {
            Roguelite.keepRunning = false;
        }
    }
}
