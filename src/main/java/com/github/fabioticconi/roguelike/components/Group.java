package com.github.fabioticconi.roguelike.components;

import java.util.Set;

import com.artemis.Component;

import it.unimi.dsi.fastutil.ints.IntArraySet;

public class Group extends Component
{
    public final Set<Integer> members;

    public Group()
    {
        members = new IntArraySet();
    }
}
