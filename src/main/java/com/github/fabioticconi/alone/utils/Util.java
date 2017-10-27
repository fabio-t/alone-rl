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

package com.github.fabioticconi.alone.utils;

import java.awt.*;

/**
 * Author: Fabio Ticconi
 * Date: 08/09/17
 */
public class Util
{
    // FIXME put this in a more appropriate place..
    public static Color BROWN = new Color(102, 51, 0);

    public static int ensureRange(final int value, final int min, final int max)
    {
        return Math.min(Math.max(value, min), max);
    }

    public static float ensureRange(final float value, final float min, final float max)
    {
        return Math.min(Math.max(value, min), max);
    }

    public static boolean inRange(final int value, final int min, final int max)
    {
        return (value >= min) && (value <= max);
    }

    public static boolean inRange(final float value, final float min, final float max)
    {
        return (value >= min) && (value <= max);
    }

    public static boolean outRange(final int value, final int min, final int max)
    {
        return (value > max) || (value < min);
    }

    public static boolean outRange(final float value, final float min, final float max)
    {
        return (value > max) || (value < min);
    }

    public static float bias(final float v, final float bias)
    {
        return (v / ((((1.0f / bias) - 2.0f) * (1.0f - v)) + 1.0f));
    }

    public static float gain(final float v, final float gain)
    {
        if (gain < 0.5f)
            return bias(v * 2.0f, gain) / 2.0f;
        else
            return bias(v * 2.0f - 1.0f,1.0f - gain) / 2.0f + 0.5f;
    }
}
