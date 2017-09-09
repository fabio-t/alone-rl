package com.github.fabioticconi.roguelite.utils;

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
}
