/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.los;

import rlforj.IBoard;
import rlforj.math.Point;
import rlforj.util.BresenhamLine;

import java.util.List;
import java.util.Vector;

/**
 * Bresenham LOS class.
 * Checks if a bresenham line can be drawn from
 * source to destination. If symmetric, also checks
 * the alternate Bresenham line from destination to
 * source.
 *
 * @author sdatta
 */
public class BresLos implements ILosAlgorithm
{

    private boolean symmetricEnabled = false;

    private Vector<Point> path;

    public BresLos(final boolean symmetric)
    {
        symmetricEnabled = symmetric;
    }

    public boolean exists(final IBoard b, final int startX, final int startY, final int endX, final int endY,
                          final boolean savePath)
    {
        final int dx  = startX - endX;
        final int dy  = startY - endY;
        final int adx = dx > 0 ? dx : -dx;
        final int ady = dy > 0 ? dy : -dy;
        final int len = (adx > ady ? adx : ady) + 1;//Max number of points on the path.

        if (savePath)
            path = new Vector<>(len);

        // array to store path.
        final int[] px = new int[len];
        final int[] py = new int[len];

        //Start to finish path
        BresenhamLine.plot(startX, startY, endX, endY, px, py);

        boolean los = false;
        for (int i = 0; i < len; i++)
        {
            if (savePath)
            {
                path.add(new Point(px[i], py[i]));
            }
            if (px[i] == endX && py[i] == endY)
            {
                los = true;
                break;
            }
            if (b.blocksLight(px[i], py[i]))
                break;
        }
        // Direct path couldn't find LOS so try alternate path
        if (!los && symmetricEnabled)
        {
            final int[] px1;
            final int[] py1;
            // allocate space for alternate path
            px1 = new int[len];
            py1 = new int[len];
            // finish to start path.
            BresenhamLine.plot(endX, endY, startX, startY, px1, py1);

            final Vector<Point> oldpath = path;
            path = new Vector<>(len);
            for (int i = len - 1; i > -1; i--)
            {
                if (savePath)
                {
                    path.add(new Point(px1[i], py1[i]));
                }
                if (px1[i] == endX && py1[i] == endY)
                {
                    los = true;
                    break;
                }
                if (b.blocksLight(px1[i], py1[i]))
                    break;
            }

            if (savePath)
                path = oldpath.size() > path.size() ? oldpath : path;
        }

        return los;
    }

    public List<Point> getPath()
    {
        return path;
    }
}
