/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.los;

import rlforj.IBoard;

/**
 * An interface for FOV algorithms.
 *
 * @author sdatta
 */
public interface IFovAlgorithm
{
    /**
     * All locations of Board b that are visible
     * from (x, y) will be visited, ie b.visit(x, y)
     * will be called on them.
     * <p>
     * Algorithms must call visit on the same location only once.
     * Algorithms should try to visit points closer to the
     * starting point before farther points.
     * Algorithms should try to visit a location before calling isObstacle
     * on it, allowing effects like an explosion destroying a wall and affecting
     * areas beyond it.
     *
     * @param b        The target board
     * @param x        Starting location:x
     * @param y        Starting location:y
     * @param distance How far can this Field of View go
     */
    void visitFoV(IBoard b, int x, int y, int distance);
}
