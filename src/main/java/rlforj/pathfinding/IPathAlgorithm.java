/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.pathfinding;

import rlforj.math.Point;

/**
 * Author: Fabio Ticconi
 * Date: 07/11/17
 */
public interface IPathAlgorithm
{
    Point[] findPath(final int startX, final int starty, final int endX, final int endY, final int radius);
}
