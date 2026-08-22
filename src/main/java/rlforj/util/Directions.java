/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.util;

/**
 * A class for various directions, their offsets.
 *
 * @author sdatta
 */
public enum Directions
{
    NORTH,
    WEST,
    SOUTH,
    EAST,
    NE,
    NW,
    SE,
    SW;
    public static final int[] dx = { 0, -1, 0, 1, 1, -1, 1, -1 }, dy = { 1, 0, -1, 0, 1, 1, -1, -1 };

    /**
     * The N4 neighbourhood, in clockwise order
     * NORTH, WEST, SOUTH, EAST
     */
    public static final Directions[] N4 = { NORTH, WEST, SOUTH, EAST };

    /**
     * The N8 neighbourhood, in clockwise order
     * NORTH, NW, WEST, SW, SOUTH, SE, EAST, NE
     */
    public static final Directions[] N8 = { NORTH, NW, WEST, SW, SOUTH, SE, EAST, NE };

    /**
     * The x offset
     *
     * @return x offset of this direction
     */
    public int dx()
    {
        return dx[this.ordinal()];
    }

    /**
     * The y offset
     *
     * @return y offset of this direction
     */
    public int dy()
    {
        return dy[this.ordinal()];
    }
}
