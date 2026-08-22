/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */
package rlforj.util;

/**
 * Bresenham's famous line drawing algorithm. Works for 2D.
 */
public final class BresenhamLine
{
    /**
     * General case algorithm
     */
    private static final BresenhamLine bresenham = new BresenhamLine();

    /**
     * Used for calculation
     */
    private int dx, dy, error, x_inc, y_inc, xx, yy, length, count;

    /**
     * Construct a Bresenham algorithm.
     */
    public BresenhamLine()
    {
    }

    /**
     * Plot a line between (x1,y1) and (x2,y2). The results are placed in x[] and y[], which must be large enough.
     *
     * @param x1 x start position
     * @param y1 y start position
     * @param x2 x end position
     * @param y2 y end position
     * @param x  array where output x values are put
     * @param y  array where output y values are put
     * @return the length of the line or the length of x[]/y[], whichever is smaller
     */
    public static final int plot(final int x1, final int y1, final int x2, final int y2, final int x[], final int y[])
    {

        final int length = Math.min(x.length, Math.min(y.length, bresenham.plot(x1, y1, x2, y2)));
        for (int i = 0; i < length; i++)
        {
            x[i] = bresenham.getX();
            y[i] = bresenham.getY();
            bresenham.next();
        }

        return length;
    }

    /**
     * Plot a line between (x1,y1) and (x2,y2). To step through the line use next().
     */
    private int plot(final int x1, final int y1, final int x2, final int y2)
    {
        // compute horizontal and vertical deltas
        dx = x2 - x1;
        dy = y2 - y1;

        // test which direction the line is going in i.e. slope angle
        if (dx >= 0)
        {
            x_inc = 1;
        }
        else
        {
            x_inc = -1;
            dx = -dx; // need absolute value
        }

        // test y component of slope

        if (dy >= 0)
        {
            y_inc = 1;
        }
        else
        {
            y_inc = -1;
            dy = -dy; // need absolute value
        }

        xx = x1;
        yy = y1;

        if (dx > dy)
            error = dx >> 1;
        else
            error = dy >> 1;

        count = 0;
        length = Math.max(dx, dy) + 1;
        return length;
    }

    /**
     * Get the next point in the line. You must not call next() if the
     * previous invocation of next() returned false.
     * <p>
     * Retrieve the X and Y coordinates of the line with getX() and getY().
     */
    private void next()
    {
        // now based on which delta is greater we can draw the line
        if (dx > dy)
        {
            // adjust the error term
            error += dy;

            // test if error has overflowed
            if (error >= dx)
            {
                error -= dx;

                // move to next line
                yy += y_inc;
            }

            // move to the next pixel
            xx += x_inc;
        }
        else
        {
            // adjust the error term
            error += dx;

            // test if error overflowed
            if (error >= dy)
            {
                error -= dy;

                // move to next line
                xx += x_inc;
            }

            // move to the next pixel
            yy += y_inc;
        }

        count++;
    }

    /**
     * @return the current X coordinate
     */
    private int getX()
    {
        return xx;
    }

    /**
     * @return the current Y coordinate
     */
    private int getY()
    {
        return yy;
    }
}
