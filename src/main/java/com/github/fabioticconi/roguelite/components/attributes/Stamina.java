package com.github.fabioticconi.roguelite.components.attributes;

import com.artemis.Component;

/**
 * Author: Fabio Ticconi
 * Date: 08/09/17
 */
public class Stamina extends Component
{
    public int value;

    public Stamina()
    {

    }

    public Stamina(final int stamina)
    {
        this.value = stamina;
    }
}
