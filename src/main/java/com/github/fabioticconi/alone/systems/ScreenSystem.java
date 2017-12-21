/*
 * Copyright (C) 2015-2017 Fabio Ticconi
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

package com.github.fabioticconi.alone.systems;

import asciiPanel.AsciiPanel;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.screens.AbstractScreen;
import com.github.fabioticconi.alone.screens.Screen;
import com.github.fabioticconi.alone.screens.StartScreen;
import net.mostlyoriginal.api.system.core.PassiveSystem;

/**
 * Author: Fabio Ticconi
 * Date: 01/11/17
 */
public final class ScreenSystem extends PassiveSystem implements Screen
{
    private Screen selected;

    @Override
    protected void initialize()
    {
        super.initialize();
        this.selected = world.getSystem(StartScreen.class);
    }

    @Override
    public float handleKeys(final BitVector keys)
    {
        return selected.handleKeys(keys);
    }

    @Override
    public void display(final AsciiPanel terminal)
    {
        selected.display(terminal);
    }

    public void select(final Class<? extends AbstractScreen> next)
    {
        this.selected = world.getSystem(next);
    }
}
