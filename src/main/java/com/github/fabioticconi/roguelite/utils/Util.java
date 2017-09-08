package com.github.fabioticconi.roguelite.utils;

/**
 * Author: Fabio Ticconi
 * Date: 08/09/17
 */
public class Util
{
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
