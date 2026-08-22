/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.los;

import rlforj.IBoard;
import rlforj.math.Point;

import java.util.List;

/**
 * An interface for for LOS and projection
 *
 * @author sdatta
 */
public interface ILosAlgorithm
{
    /**
     * Determines whether line of sight exists between point (startX, startY) and
     * (endX, endY). Optionally calculates the path of projection (retrievable via call to
     * {@link ILosAlgorithm#getPath}).
     *
     * @param b        The board to be visited.
     * @param startX   Starting position:x
     * @param startY   Starting position:y
     * @param endX     Target location:x
     * @param endY     Target location:y
     * @param savePath Whether to also calculate and store the path from the source to the target.
     * @return true if a line of sight could be established
     */
    boolean exists(IBoard b, int startX, int startY, int endX, int endY, boolean savePath);

    /**
     * Obtain the path of the projection calculated during the last call
     * to {@link ILosAlgorithm#exists}.
     *
     * @return null if no los was established so far, or a list of points if a los found
     */
    List<Point> getPath();
}
