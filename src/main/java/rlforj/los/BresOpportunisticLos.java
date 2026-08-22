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
 * Bresenham LOS.
 * Tries to reach destination along first path. If
 * obstacled, shifts to alternate path. If that is blocked,
 * shift to first path again. Fails only if both are blocked
 * at a point.
 *
 * @author sdatta
 */
public class BresOpportunisticLos implements ILosAlgorithm
{

    private Vector<Point> path;

    public boolean exists(final IBoard b, final int startX, final int startY, final int endX, final int endY,
                          final boolean savePath)
    {
        final int dx  = startX - endX;
        final int dy  = startY - endY;
        final int adx = dx > 0 ? dx : -dx;
        final int ady = dy > 0 ? dy : -dy;
        final int len = (adx > ady ? adx : ady) + 1;

        if (savePath)
            path = new Vector<>(len);

        final int[] px = new int[len];
        final int[] py = new int[len];
        int[]       px1, py1;
        px1 = new int[len];
        py1 = new int[len];

        //Compute both paths
        BresenhamLine.plot(startX, startY, endX, endY, px, py);
        BresenhamLine.plot(endX, endY, startX, startY, px1, py1);

        boolean los           = false;
        boolean alternatePath = false;
        for (int i = 0; i < len; i++)
        {
            // Have we reached the end ? In that case quit
            if (px[i] == endX && py[i] == endY)
            {
                if (savePath)
                {
                    path.add(new Point(px[i], py[i]));
                }
                los = true;
                break;
            }
            // if we are on alternate path, is the path clear ?
            if (alternatePath && !b.blocksLight(px1[len - i - 1], py1[len - i - 1]))
            {
                if (savePath)
                    path.add(new Point(px1[len - i - 1], py1[len - i - 1]));
                continue;
            }
            else
                alternatePath = false;//come back to ordinary path

            //if on ordinary path, or alternate path was not clear
            if (!b.blocksLight(px[i], py[i]))
            {
                if (savePath)
                {
                    path.add(new Point(px[i], py[i]));
                }
                continue;
            }
            //if ordinary path wasnt clear
            if (!b.blocksLight(px1[len - i - 1], py1[len - i - 1]))
            {
                if (savePath)
                    path.add(new Point(px1[len - i - 1], py1[len - i - 1]));
                alternatePath = true;//go on alternate path
                continue;
            }
            if (savePath)
                path.add(new Point(px1[len - i - 1], py1[len - i - 1]));
            break;
        }

        return los;
    }

    public List<Point> getPath()
    {
        return path;
    }

}
