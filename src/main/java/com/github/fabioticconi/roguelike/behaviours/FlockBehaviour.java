package com.github.fabioticconi.roguelike.behaviours;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.github.fabioticconi.roguelike.components.Position;
import com.github.fabioticconi.roguelike.components.Sight;
import com.github.fabioticconi.roguelike.components.Speed;

public class FlockBehaviour extends AbstractBehaviour
{
    ComponentMapper<Sight>    mSight;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Speed>    mSpeed;

    // TODO needs a "Group" component, or similar, to identify the leader/other
    // group members

    @Override
    protected void initialize()
    {
        aspect = Aspect.all(Position.class, Speed.class, Sight.class).build(world);
    }

    @Override
    public float evaluate(final int entityId)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float update()
    {
        // TODO Auto-generated method stub
        return 0;
    }
}
