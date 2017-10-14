/*
 * Copyright (C) 2017 Fabio Ticconi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.fabioticconi.alone.components;

import com.artemis.Component;
import com.github.fabioticconi.alone.behaviours.Behaviour;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Fabio Ticconi
 */
public class AI extends Component
{
    public float cooldown;

    public List<Behaviour> behaviours;

    public Behaviour activeBehaviour;

    public AI()
    {
        behaviours = new LinkedList<Behaviour>();
    }

    public AI(final float cooldown)
    {
        this.cooldown = cooldown;

        behaviours = new LinkedList<Behaviour>();
    }
}
