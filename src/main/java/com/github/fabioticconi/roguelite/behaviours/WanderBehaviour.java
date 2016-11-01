package com.github.fabioticconi.roguelite.behaviours;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.roguelite.components.Position;
import com.github.fabioticconi.roguelite.components.Sight;
import com.github.fabioticconi.roguelite.components.Speed;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.map.Map;
import com.github.fabioticconi.roguelite.systems.MovementSystem;

public class WanderBehaviour extends AbstractBehaviour
{
    ComponentMapper<Position> mPosition;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Sight>    mSight;

    MovementSystem sMovement;

    @Wire Map map;

    @Override protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class).build(world);
    }

    @Override public float evaluate(final int entityId)
    {
        this.entityId = entityId;

        if (!interested(entityId))
            return 0f;

        return 0.2f;
    }

    @Override public float update()
    {
        final Position pos   = mPosition.get(entityId);
        final Sight    sight = mSight.getSafe(entityId, null);
        final float    speed = mSpeed.get(entityId).value;

        Side direction;

        if (sight == null || sight.value == 0)
        {
            direction = Side.getRandom();
        } else
        {
            direction = map.getFreeExitRandomised(pos.x, pos.y);
        }

        if (direction == Side.HERE)
            return 0f;

        return sMovement.moveTo(entityId, speed, direction);
    }
}
