/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.math;

/**
 * A Euclidean 2D line class represented by integers.
 *
 * @author Jonathan Duerig
 */
public class Line2I
{
    public Point near;

    public Point far;

    public Line2I(final Point newNear, final Point newFar)
    {
        near = newNear;
        far = newFar;
    }

    public Line2I(final int x1, final int y1, final int x2, final int y2)
    {
        near = new Point(x1, y1);
        far = new Point(x2, y2);
    }

    public boolean isBelow(final Point point)
    {
        return relativeSlope(point) > 0;
    }

    public boolean isBelowOrContains(final Point point)
    {
        return relativeSlope(point) >= 0;
    }

    public boolean isAbove(final Point point)
    {
        return relativeSlope(point) < 0;
    }

    public boolean isAboveOrContains(final Point point)
    {
        return relativeSlope(point) <= 0;
    }

    public boolean doesContain(final Point point)
    {
        return relativeSlope(point) == 0;
    }

    // negative if the line is above the point.
    // positive if the line is below the point.
    // 0 if the line is on the point.
    public int relativeSlope(final Point point)
    {
        return (far.y - near.y) * (far.x - point.x) - (far.y - point.y) * (far.x - near.x);
    }

    @Override
    public String toString()
    {
        return "( " + near + " -> " + far + " )";
    }

}
