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
import com.github.fabioticconi.alone.constants.WeaponType;

import java.util.EnumMap;

/**
 * Author: Fabio Ticconi
 * Date: 05/11/17
 */
public class Armour extends Component
{
    // TODO we can improve this by using a primitive float array and WeaponType ordinals as indeces.
    // which is what EnumMap does internally. We'd avoid autoboxing.
    public final EnumMap<WeaponType, Float> defences;

    public Armour()
    {
        this.defences = new EnumMap<>(WeaponType.class);
    }

    public void set(final float slash, final float point, final float blunt, final float natural)
    {
        this.defences.put(WeaponType.SLASH, slash);
        this.defences.put(WeaponType.POINT, point);
        this.defences.put(WeaponType.BLUNT, blunt);

        // a generic type for animal attacks
        this.defences.put(WeaponType.NATURAL, natural);
    }
}
