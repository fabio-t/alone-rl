package com.github.fabioticconi.roguelike.utils;

import com.github.fabioticconi.roguelike.constants.Options;

public class Coords
{
    public static float distanceEuclidean(final int x1, final int y1, final int x2, final int y2)
    {
        final float xdiff = x1 - x2;
        final float ydiff = y1 - y2;

        return (float) Math.sqrt(xdiff * xdiff + ydiff * ydiff);
    }

    public static int distanceBlock(final int x1, final int y1, final int x2, final int y2)
    {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public static int distanceChebyshev(final int x1, final int y1, final int x2, final int y2)
    {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    public static void visitVonNeumannNeighbours(final int x, final int y, final int r)
    {
        if (r == 0)
        {
            System.out.println("HERE");
            return;
        }

        final int xmin = Math.max(x - r, 0);
        final int xmax = Math.min(x + r, Options.MAP_SIZE_X - 1);

        int i = -r - Math.min(x - r, 0);
        for (int x_t = xmin; x_t <= xmax; x_t++, i++)
        {
            final int z = r - Math.abs(i);

            final int y1 = y + z;

            if (y1 < Options.MAP_SIZE_Y)
            {
                System.out.println("i=" + i + ", x=" + x_t + ", y=" + y1 + ", " + distanceBlock(x, y, x_t, y1));
            }

            final int y2 = y - z;

            if (y1 == y2 || y2 < 0)
            {
                continue;
            }

            System.out.println("i=" + i + ", x=" + x_t + ", y=" + y2 + ", " + distanceBlock(x, y, x_t, y2));
        }
    }

    public static void visitMooreNeighbours(final int x, final int y, final int r)
    {

    }

    public static long packCoords(final int x, final int y)
    {
        return x | ((long) y << 32);
    }
}
