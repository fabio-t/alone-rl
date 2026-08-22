/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.los;

import rlforj.IBoard;
import rlforj.math.Point;

import java.util.*;

/**
 * Code adapted from NG roguelike engine http://roguelike-eng.sourceforge.net/
 * <p>
 * Recursive line-of-sight class implementing a spiraling shadow-casting
 * algorithm. This algorithm chosen because it can establish line-of-sight by
 * visiting each grid at most once, and is (for me) much simpler to implement
 * than octant oriented or non-recursive approaches. -TSS
 *
 * @author TSS
 */
public class ShadowCasting implements IConeFovAlgorithm, ILosAlgorithm
{
    public static final int MAX_CACHED_RADIUS = 40;

    static HashMap<Integer, ArrayList<ArcPoint>> circles = new HashMap<>();

    static
    {
        final Point origin = new Point(0, 0);

        final int radius = MAX_CACHED_RADIUS;

        for (int i = -radius; i <= radius; i++)
        {
            for (int j = -radius; j <= radius; j++)
            {
                // final int distance = (int) floor(origin.distance(i, j));
                final int distance = origin.distance2(i, j);

                // If filled, add anything where floor(distance) <= radius
                // If not filled, require that floor(distance) == radius
                if (distance <= radius)
                {
                    final ArrayList<ArcPoint> circ = circles.computeIfAbsent(distance, k -> new ArrayList<>());
                    circ.add(new ArcPoint(i, j));
                }
            }
        }

        for (final ArrayList<ArcPoint> list : circles.values())
        {
            Collections.sort(list);
            // System.out.println("r: "+r+" "+list);
        }
    }

    /**
     * When LOS not found, use Bresenham to find failed path
     */
    BresLos fallBackLos = new BresLos(true);
    private Vector<Point> path;

    static void go(final IBoard board, final Point ctr, final int r, final int maxDistance, double th1,
                   final double th2)
    {
        if (r > maxDistance)
            throw new IllegalArgumentException();
        if (r <= 0)
            throw new IllegalArgumentException();
        final ArrayList<ArcPoint> circle      = circles.get(r);
        final int                 circSize    = circle.size();
        boolean                   wasObstacle = false;
        boolean                   foundClear  = false;
        for (int i = 0; i < circSize; i++)
        {
            final ArcPoint arcPoint = circle.get(i);
            final int      px       = ctr.x + arcPoint.x;
            final int      py       = ctr.y + arcPoint.y;
            //			Point point = new Point(px, py);

            // if outside the board, ignore it and move to the next one
            if (!board.contains(px, py))
            {
                wasObstacle = true;
                continue;
            }

            if (arcPoint.lagging < th1 && arcPoint.theta != th1 && arcPoint.theta != th2)
            {
                // System.out.println("< than " + arcPoint);
                continue;
            }
            if (arcPoint.leading > th2 && arcPoint.theta != th1 && arcPoint.theta != th2)
            {
                // System.out.println("> than " + arcPoint);
                continue;
            }

            // Accept this point
            // pointSet.add(point);
            board.visit(px, py);

            // Check to see if we have an obstacle here
            final boolean isObstacle = board.blocksLight(px, py);

            // If obstacle is encountered, we start a new run from our start
            // theta
            // to the rightTheta of the current point at radius+1
            // We then proceed to the next non-obstacle, whose leftTheta
            // becomes
            // our new start theta
            // If the last point is an obstacle, we do not start a new Run
            // at the
            // end.
            if (isObstacle)
            {
                // keep going
                if (wasObstacle)
                {
                    continue;
                }

                // start a new run from our start to this point's right side
                else if (foundClear)
                {

                    if (r < maxDistance)
                        go(board, ctr, r + 1, maxDistance, th1, arcPoint.leading);
                }
                else
                {
                    if (arcPoint.theta == 0.0)
                    {
                        th1 = 0.0;
                    }
                    else
                    {
                        th1 = arcPoint.leading;
                    }
                    // System.out.println("Adjusting start for obstacle
                    // "+th1+" at " + arcPoint);
                }
            }
            else
            {
                foundClear = true;
                // we're clear of obstacle; any runs propogated from this
                // run starts at this
                // point's leftTheta
                if (wasObstacle)
                {
                    final ArcPoint last = circle.get(i - 1);
                    // if (last.theta == 0.0) {
                    // th1 = 0.0;
                    // }
                    // else {
                    th1 = last.lagging;
                    // }

                    // System.out.println("Adjusting start for clear of
                    // obstacle "+th1+" at " + arcPoint);

                    wasObstacle = false;
                }
                else
                {
                    wasObstacle = false;
                    continue;
                }
            }
            wasObstacle = isObstacle;
        }

        if (!wasObstacle && r < maxDistance)
        {
            go(board, ctr, r + 1, maxDistance, th1, th2);
        }
    }

    /**
     * Compute and return the list of RLPoints in line-of-sight to the given
     * region. In general, this method should be very fast.
     */
    public void visitFoV(final IBoard b, final int x, final int y, final int distance)
    {
        if (b == null)
            throw new IllegalArgumentException();
        if (distance < 1)
            throw new IllegalArgumentException();

        // HashSet<RLPoint> points = new HashSet<RLPoint>(31);
        // RLRectangle r = locator.bounds();
        // Board b = locator.board();

        // Note: it would be slightly more efficient to just check around
        // the perimeter, but only for observers of size 3+, so for now I'm
        // too lazy
        // for (int i = 0; i < r.width; i++) {
        // for (int j = 0; j < r.height; j++) {
        // RLPoint p = RLPoint.point(r.x + i, r.y + j);
        // points.add(p);
        final Point p = new Point(x, y);
        b.visit(x, y);
        go(b, p, 1, distance, 0.0, 359.9);
        // }
        // }

        // return points;
    }

    public boolean exists(final IBoard b, final int startX, final int startY, final int endX, final int endY,
                          final boolean savePath)
    {
        final int dx = endX - startX;
        final int dy = endY - startY;
        final int signX, signY;
        final int adx, ady;

        if (dx > 0)
        {
            adx = dx;
            signX = 1;
        }
        else
        {
            adx = -dx;
            signX = -1;
        }
        if (dy > 0)
        {
            ady = dy;
            signY = 1;
        }
        else
        {
            ady = -dy;
            signY = -1;
        }
        final RecordQuadrantVisitBoard fb = new RecordQuadrantVisitBoard(b, startX, startY, endX, endY, savePath);

        final Point p = new Point(startX, startY);

        if (startY == endY && endX > startX)
        {
            final int    distance = dx + 1;
            final double deg1     = Math.toDegrees(Math.atan2(.25, dx));//very thin angle
            go(fb, p, 1, distance, -deg1, 0);
            go(fb, p, 1, distance, 0, deg1);
        }
        else
        {
            final int distance = (int) Math.sqrt(adx * adx + ady * ady) + 1;
            double    deg1     = Math.toDegrees(Math.atan2(-dy, (adx - .5) * signX));
            if (deg1 < 0)
                deg1 += 360;
            double deg2 = Math.toDegrees(Math.atan2(-(ady - .5) * signY, dx));
            if (deg2 < 0)
                deg2 += 360;
            if (deg1 > deg2)
            {
                final double temp = deg1;
                deg1 = deg2;
                deg2 = temp;
            }

            //			System.out.println("Locations "+(adx-1)*signX+" "+dy);
            //			System.out.println("Locations "+dx+" "+(ady-1)*signY);
            //			System.out.println("Degrees "+deg1+" "+deg2);

            go(fb, p, 1, distance, deg1, deg2);
        }

        if (savePath)
        {
            if (fb.endVisited)
                path = GenericCalculateProjection.calculateProjecton(startX, startY, endX, endY, fb);
            else
            {
                fallBackLos.exists(b, startX, startY, endX, endY, true);
                path = (Vector<Point>) fallBackLos.getPath();
            }
            //			calculateProjecton(startX, startY, adx, ady, fb, state);
        }
        return fb.endVisited;
    }

    public List<Point> getPath()
    {
        return path;
    }

    public void visitConeFieldOfView(final IBoard b, final int x, final int y, final int distance, int startAngle,
                                     int endAngle)
    {
        // Making Positive Y downwards
        final int tmp = startAngle;
        startAngle = -endAngle;
        endAngle = -tmp;

        if (startAngle < 0)
        {
            startAngle %= 360;
            startAngle += 360;
        }
        if (endAngle < 0)
        {
            endAngle %= 360;
            endAngle += 360;
        }

        if (startAngle > 360)
            startAngle %= 360;
        if (endAngle > 360)
            endAngle %= 360;
        //		System.out.println(startAngle+" "+finishAngle);

        if (b == null)
            throw new IllegalArgumentException();
        if (distance < 1)
            throw new IllegalArgumentException();

        final Point p = new Point(x, y);
        b.visit(x, y);
        if (startAngle > endAngle)
        {
            go(b, p, 1, distance, startAngle, 359.999);
            go(b, p, 1, distance, 0.0, endAngle);
        }
        else
            go(b, p, 1, distance, startAngle, endAngle);
    }

    static class ArcPoint implements Comparable
    {
        int x, y;

        double theta;

        double leading;

        double lagging;

        ArcPoint(final int dx, final int dy)
        {
            this.x = dx;
            this.y = dy;
            theta = angle(y, x);
            // System.out.println(x + "," + y + ", theta=" + theta);
            // top left
            if (x < 0 && y < 0)
            {
                leading = angle(y - 0.5, x + 0.5);
                lagging = angle(y + 0.5, x - 0.5);
            }
            // bottom left
            else if (x < 0)
            {
                leading = angle(y - 0.5, x - 0.5);
                lagging = angle(y + 0.5, x + 0.5);
            }
            // bottom right
            else if (y > 0)
            {
                leading = angle(y + 0.5, x - 0.5);
                lagging = angle(y - 0.5, x + 0.5);
            }
            // top right
            else
            {
                leading = angle(y + 0.5, x + 0.5);
                lagging = angle(y - 0.5, x - 0.5);
            }

        }

        public String toString()
        {
            return "[" + x + "," + y + "=" + (int) (theta) + "/" + (int) (leading) + "/" + (int) (lagging);
        }

        double angle(final double y, final double x)
        {
            double a = Math.atan2(y, x);
            a = Math.toDegrees(a);
            a = 360.0 - a;
            a %= 360;
            if (a < 0)
                a += 360;
            return a;
        }

        public int compareTo(final Object o)
        {
            return theta > ((ArcPoint) o).theta ? 1 : -1;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final ArcPoint arcPoint = (ArcPoint) o;

            return theta == arcPoint.theta;
        }
    }

}
