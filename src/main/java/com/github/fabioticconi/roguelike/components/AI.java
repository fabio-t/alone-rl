/**
 * Copyright 2016 Fabio Ticconi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.fabioticconi.roguelike.components;

import java.util.LinkedList;
import java.util.List;

import com.artemis.Component;
import com.github.fabioticconi.roguelike.behaviours.Behaviour;

/**
 *
 * @author Fabio Ticconi
 */
public class AI extends Component
{
    public float           cooldown;

    public List<Behaviour> behaviours;

    public Behaviour       activeBehaviour;

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
