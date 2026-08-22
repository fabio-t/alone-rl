/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.util;

public class MathUtils
{
    public static final int isqrt(final int x)
    {
        int op, res, one;

        op = x;
        res = 0;

	    /* "one" starts at the highest power of four <= than the argument. */
        one = 1 << 30;  /* second-to-top bit set */
        while (one > op)
            one >>= 2;

        while (one != 0)
        {
            if (op >= res + one)
            {
                op = op - (res + one);
                res = res + 2 * one;
            }
            res >>= 1;
            one >>= 2;
        }
        return (res);
    }
}
