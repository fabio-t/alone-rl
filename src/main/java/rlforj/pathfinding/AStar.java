/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.pathfinding;

import rlforj.IBoard;
import rlforj.math.Point;
import rlforj.util.HeapNode;
import rlforj.util.SimpleHeap;

import java.util.ArrayList;

public class AStar implements IPathAlgorithm
{
    private final IBoard  map;
    private final int     boardWidth;
    private final int     boardHeight;
    private final boolean allowDiagonal;

    public AStar(final IBoard map, final int boardWidth, final int boardHeight)
    {
        this(map, boardWidth, boardHeight, true);
    }

    public AStar(final IBoard map, final int boardWidth, final int boardHeight, final boolean allowDiagonal)
    {
        this.map = map;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.allowDiagonal = allowDiagonal;
    }

    public Point[] findPath(final int startX, final int startY, final int endX, final int endY)
    {
        return findPath(startX, startY, endX, endY, -1);
    }

    public Point[] findPath(final int startX, final int startY, final int endX, final int endY, final int radius)
    {
        if (!this.map.contains(startX, startY) || !this.map.contains(endX, endY))
        {
            return null;
        }

        final int width;
        final int height;
        final int minX, minY, maxX, maxY;

        if (radius == 0)
        {
            return new Point[] { new Point(startX, startY) };
        }
        else if (radius < 0)
        {
            width = boardWidth;
            height = boardHeight;

            minX = 0;
            minY = 0;
            maxX = boardWidth - 1;
            maxY = boardHeight - 1;
        }
        else
        {
            minX = Math.max(0, startX - radius);
            minY = Math.max(0, startY - radius);
            maxX = Math.min(boardWidth - 1, startX + radius);
            maxY = Math.min(boardHeight - 1, startY + radius);

            width = maxX - minX + 1;
            height = maxY - minY + 1;
        }

        final PathNode[][]         nodeHash  = new PathNode[width][height];
        final SimpleHeap<HeapNode> open      = new SimpleHeap<>(1000);
        final PathNode             startNode = new PathNode(startX, startY, 0.0);
        startNode.h = this.computeHeuristics(startNode, endX, endY, startX, startY);
        startNode.calcCost();
        open.add(startNode);
        nodeHash[startX - minX][startY - minY] = startNode;
        while (open.size() > 0)
        {
            final PathNode step = (PathNode) open.poll();
            if (step.x == endX && step.y == endY)
            {
                return this.createPath(step);
            }

            for (int dx = -1; dx <= 1; dx++)
            {
                for (int dy = -1; dy <= 1; dy++)
                {
                    // exclude the current point, as well as diagonals if not allowed
                    if (!((dx == 0 && dy == 0) || (dx != 0 && dy != 0 && !this.allowDiagonal)))
                    {
                        final int cx = step.x + dx;
                        final int cy = step.y + dy;
                        if (cx >= minX && cy >= minY && cx <= maxX && cy <= maxY && this.map.contains(cx, cy))
                        {
                            // the only allowed obstacle is the end point
                            if ((cx != endX || cy != endY) && this.map.isObstacle(cx, cy))
                                continue;

                            final PathNode n1;
                            final double   this_cost = dx != 0 && dy != 0 ? 1.1 : 1.0;
                            if (nodeHash[cx - minX][cy - minY] == null)
                            {
                                n1 = new PathNode(cx, cy, step.g + this_cost);
                                n1.prev = step;
                                n1.h = this.computeHeuristics(n1, endX, endY, startX, startY);
                                n1.calcCost();
                                open.add(n1);
                                nodeHash[cx - minX][cy - minY] = n1;
                            }
                            else
                            {
                                n1 = nodeHash[cx - minX][cy - minY];
                                if (n1.g > step.g + this_cost)
                                {
                                    n1.g = step.g + this_cost;
                                    n1.calcCost();
                                    n1.prev = step;
                                    if (open.contains(n1))
                                    {
                                        open.adjust(n1);
                                    }
                                    else
                                    {
                                        open.add(n1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private double computeHeuristics(final PathNode node, final int x1, final int y1, final int startx,
                                     final int starty)
    {
        final int dx        = Math.abs(node.x - x1);
        final int dy        = Math.abs(node.y - y1);
        final int diagsteps = Math.min(dx, dy);
        return (double) diagsteps * 1.0 + (double) ((Math.max(dx, dy) - diagsteps)) +
               (double) Math.abs((node.x - x1) * (starty - y1) - (node.y - y1) * (startx - x1)) * 0.01;
    }

    private Point[] createPath(PathNode end)
    {
        if (end == null)
            return null;

        final ArrayList<Point> v = new ArrayList<>();
        while (end != null)
        {
            v.add(new Point(end.x, end.y));
            end = end.prev;
        }
        final int     sz  = v.size();
        final Point[] ret = new Point[sz];
        int           i   = 0;
        while (i < sz)
        {
            ret[i] = v.get(sz - i - 1);
            ++i;
        }
        return ret;
    }

    public static class PathNode implements HeapNode
    {
        public double g;
        public double h;
        int      x;
        int      y;
        double   cost;
        PathNode prev;
        int      heapIndex;

        public PathNode(final int x, final int y, final double g)
        {
            this.x = x;
            this.y = y;
            this.g = g;
        }

        public PathNode(final int x, final int y)
        {
            this.x = x;
            this.y = y;
        }

        public void calcCost()
        {
            this.cost = this.h + this.g;
        }

        public int compareTo(final Object o)
        {
            return (int) Math.signum(this.cost - ((PathNode) o).cost);
        }

        public int getHeapIndex()
        {
            return this.heapIndex;
        }

        public void setHeapIndex(final int heapIndex)
        {
            this.heapIndex = heapIndex;
        }
    }
}
