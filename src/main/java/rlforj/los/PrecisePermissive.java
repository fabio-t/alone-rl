/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.los;

import rlforj.IBoard;
import rlforj.math.Line2I;
import rlforj.math.Point;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class PrecisePermissive implements IFovAlgorithm, ILosAlgorithm
{
    private final ILosAlgorithm fallBackLos = new BresLos(true);
    private Vector<Point> path;

    void calculateFovQuadrant(final fovStateT state)
    {
        // System.out.println("calcfovq called");
        final LinkedList<bumpT> steepBumps   = new LinkedList<>();
        final LinkedList<bumpT> shallowBumps = new LinkedList<>();
        // activeFields is sorted from shallow-to-steep.
        final LinkedList<fieldT> activeFields = new LinkedList<>();
        activeFields.addLast(new fieldT());
        activeFields.getLast().shallow.near = new Point(0, 1);
        activeFields.getLast().shallow.far = new Point(state.extent.x, 0);
        activeFields.getLast().steep.near = new Point(1, 0);
        activeFields.getLast().steep.far = new Point(0, state.extent.y);

        final Point dest = new Point(0, 0);

        // Visit the source square exactly once (in quadrant 1).
        if (state.quadrant.x == 1 && state.quadrant.y == 1)
        {
            actIsBlocked(state, dest);
        }

        CLikeIterator<fieldT> currentField = new CLikeIterator<>(activeFields.listIterator());
        int                   i;
        int                   j;
        final int             maxI         = state.extent.x + state.extent.y;
        // For each square outline
        for (i = 1; i <= maxI && !activeFields.isEmpty(); ++i)
        {
            final int startJ = max(0, i - state.extent.x);
            final int maxJ   = min(i, state.extent.y);
            // System.out.println("Startj "+startJ+" maxj "+maxJ);
            // Visit the nodes in the outline
            for (j = startJ; j <= maxJ && !currentField.isAtEnd(); ++j)
            {
                // System.out.println("i j "+i+" "+j);
                dest.x = i - j;
                dest.y = j;
                visitSquare(state, dest, currentField, steepBumps, shallowBumps, activeFields);
            }
            // System.out.println("Activefields size "+activeFields.size());
            currentField = new CLikeIterator<>(activeFields.listIterator());
        }
    }

    private final int max(final int i, final int j)
    {
        return i > j ? i : j;
    }

    private final int min(final int i, final int j)
    {
        return i < j ? i : j;
    }

    void visitSquare(final fovStateT state, final Point dest, final CLikeIterator<fieldT> currentField,
                     final LinkedList<bumpT> steepBumps, final LinkedList<bumpT> shallowBumps,
                     final LinkedList<fieldT> activeFields)
    {
        //		System.out.println("-> "+steepBumps+" - "+shallowBumps);
        // System.out.println("visitsq called "+dest);
        // The top-left and bottom-right corners of the destination square.
        final Point topLeft     = new Point(dest.x, dest.y + 1);
        final Point bottomRight = new Point(dest.x + 1, dest.y);

        // fieldT currFld=null;

        while (!currentField.isAtEnd() && currentField.getCurrent().steep.isBelowOrContains(bottomRight))
        {
            //			System.out.println("currFld.steep.isBelowOrContains(bottomRight) "
            //					+ currentField.getCurrent().steep
            //							.isBelowOrContains(bottomRight));
            // case ABOVE
            // The square is in case 'above'. This means that it is ignored
            // for the currentField. But the steeper fields might need it.
            // ++currentField;
            currentField.gotoNext();
        }
        if (currentField.isAtEnd())
        {
            //			System.out.println("currentField.isAtEnd()");
            // The square was in case 'above' for all fields. This means that
            // we no longer care about it or any squares in its diagonal rank.
            return;
        }

        // Now we check for other cases.
        if (currentField.getCurrent().shallow.isAboveOrContains(topLeft))
        {
            // case BELOW
            // The shallow line is above the extremity of the square, so that
            // square is ignored.
            //			System.out.println("currFld.shallow.isAboveOrContains(topLeft) "
            //					+ currentField.getCurrent().shallow);
            return;
        }
        // The square is between the lines in some way. This means that we
        // need to visit it and determine whether it is blocked.
        final boolean isBlocked = actIsBlocked(state, dest);
        if (!isBlocked)
        {
            // We don't care what case might be left, because this square does
            // not obstruct.
            return;
        }

        if (currentField.getCurrent().shallow.isAbove(bottomRight) && currentField.getCurrent().steep.isBelow(topLeft))
        {
            // case BLOCKING
            // Both lines intersect the square. This current field has ended.
            currentField.removeCurrent();
        }
        else if (currentField.getCurrent().shallow.isAbove(bottomRight))
        {
            // case SHALLOW BUMP
            // The square intersects only the shallow line.
            addShallowBump(topLeft, currentField.getCurrent(), steepBumps, shallowBumps);
            checkField(currentField);
        }
        else if (currentField.getCurrent().steep.isBelow(topLeft))
        {
            // case STEEP BUMP
            // The square intersects only the steep line.
            addSteepBump(bottomRight, currentField.getCurrent(), steepBumps, shallowBumps);
            checkField(currentField);
        }
        else
        {
            // case BETWEEN
            // The square intersects neither line. We need to split into two
            // fields.
            final fieldT steeperField   = currentField.getCurrent();
            final fieldT shallowerField = new fieldT(currentField.getCurrent());
            currentField.insertBeforeCurrent(shallowerField);
            addSteepBump(bottomRight, shallowerField, steepBumps, shallowBumps);
            currentField.gotoPrevious();
            if (!checkField(currentField)) // did not remove
                currentField.gotoNext();// point to the original element
            addShallowBump(topLeft, steeperField, steepBumps, shallowBumps);
            checkField(currentField);
        }
    }

    boolean checkField(final CLikeIterator<fieldT> currentField)
    {
        // If the two slopes are colinear, and if they pass through either
        // extremity, remove the field of view.
        final fieldT currFld = currentField.getCurrent();
        boolean      ret     = false;

        if (currFld.shallow.doesContain(currFld.steep.near) && currFld.shallow.doesContain(currFld.steep.far) &&
            (currFld.shallow.doesContain(new Point(0, 1)) || currFld.shallow.doesContain(new Point(1, 0))))
        {
            //			System.out.println("removing "+currentField.getCurrent());
            currentField.removeCurrent();
            ret = true;
        }
        // System.out.println("CheckField "+ret);
        return ret;
    }

    void addShallowBump(final Point point, final fieldT currFld, final LinkedList<bumpT> steepBumps,
                        final LinkedList<bumpT> shallowBumps)
    {
        //		System.out.println("Adding shallow "+point);
        // First, the far point of shallow is set to the new point.
        currFld.shallow.far = point;
        // Second, we need to add the new bump to the shallow bump list for
        // future steep bump handling.
        shallowBumps.addLast(new bumpT());
        shallowBumps.getLast().location = point;
        shallowBumps.getLast().parent = currFld.shallowBump;
        currFld.shallowBump = shallowBumps.getLast();
        // Now we have too look through the list of steep bumps and see if
        // any of them are below the line.
        // If there are, we need to replace near point too.
        bumpT currentBump = currFld.steepBump;
        while (currentBump != null)
        {
            if (currFld.shallow.isAbove(currentBump.location))
            {
                currFld.shallow.near = currentBump.location;
            }
            currentBump = currentBump.parent;
        }
    }

    void addSteepBump(final Point point, final fieldT currFld, final LinkedList<bumpT> steepBumps,
                      final LinkedList<bumpT> shallowBumps)
    {
        //		System.out.println("Adding steep "+point);
        currFld.steep.far = point;
        steepBumps.addLast(new bumpT());
        steepBumps.getLast().location = point;
        steepBumps.getLast().parent = currFld.steepBump;
        currFld.steepBump = steepBumps.getLast();
        // Now look through the list of shallow bumps and see if any of them
        // are below the line.
        bumpT currentBump = currFld.shallowBump;
        while (currentBump != null)
        {
            if (currFld.steep.isBelow(currentBump.location))
            {
                currFld.steep.near = currentBump.location;
            }
            currentBump = currentBump.parent;
        }
    }

    boolean actIsBlocked(final fovStateT state, final Point pos)
    {
        final Point adjustedPos = new Point(pos.x * state.quadrant.x + state.source.x,
                                            pos.y * state.quadrant.y + state.source.y);

        if (!state.board.contains(adjustedPos.x, adjustedPos.y))
            return false;//we are getting outside the board

        // System.out.println("actIsBlocked "+adjustedPos.x+" "+adjustedPos.y);

        // if ((state.quadrant.x * state.quadrant.y == 1
        // && pos.x == 0 && pos.y != 0)
        // || (state.quadrant.x * state.quadrant.y == -1
        // && pos.y == 0 && pos.x != 0)
        // || doesPermissiveVisit(state.mask, pos.x*state.quadrant.x,
        // pos.y*state.quadrant.y) == 0)
        // {
        // // return result;
        // }
        // else
        // {
        // board.visit(adjustedPos.x, adjustedPos.y);
        // // return result;
        // }
        /*
         * ^ | 2 | <-3-+-1-> | 4 | v
		 *
		 * To ensure all squares are visited before checked ( so that we can
		 * decide obstacling at visit time, eg walls destroyed by explosion) ,
		 * visit axes 1,2 only in Q1, 3 in Q2, 4 in Q3
		 */
        if (state.isLos // In LOS calculation all visits allowed
            || state.quadrantIndex == 0 // can visit anything from Q1
            || (state.quadrantIndex == 1 && pos.x != 0) // Q2 : no Y axis
            || (state.quadrantIndex == 2 && pos.y != 0) // Q3 : no X axis
            || (state.quadrantIndex == 3 && pos.x != 0 && pos.y != 0)) // Q4
            // no X
            // or Y
            // axis
            if (doesPermissiveVisit(state.mask, pos.x * state.quadrant.x, pos.y * state.quadrant.y) == 1)
            {
                state.board.visit(adjustedPos.x, adjustedPos.y);
            }
        return state.board.blocksLight(adjustedPos.x, adjustedPos.y);
    }

    void permissiveFov(final int sourceX, final int sourceY, final permissiveMaskT mask)
    {
        final fovStateT state = new fovStateT();
        state.source = new Point(sourceX, sourceY);
        state.mask = mask;
        state.board = mask.board;
        // state.isBlocked = isBlocked;
        // state.visit = visit;
        // state.context = context;

        final int   quadrantCount = 4;
        final Point quadrants[]   = { new Point(1, 1), new Point(-1, 1), new Point(-1, -1), new Point(1, -1) };

        final Point extents[] = { new Point(mask.east, mask.north), new Point(mask.west, mask.north),
                                  new Point(mask.west, mask.south), new Point(mask.east, mask.south) };
        int quadrantIndex = 0;
        for (; quadrantIndex < quadrantCount; ++quadrantIndex)
        {
            state.quadrant = quadrants[quadrantIndex];
            state.extent = extents[quadrantIndex];
            state.quadrantIndex = quadrantIndex;
            calculateFovQuadrant(state);
        }
    }

    int doesPermissiveVisit(final permissiveMaskT mask, final int x, final int y)
    {
        if (mask.fovType == FovType.SQUARE)
            return 1;
        else if (mask.fovType == FovType.CIRCLE)
        {
            if (x * x + y * y < mask.distPlusOneSq)
                return 1;
            else
                return 0;
        }
        return 1;
    }

    public void visitFoV(final IBoard b, final int x, final int y, final int distance)
    {
        final permissiveMaskT mask = new permissiveMaskT();
        mask.east = mask.north = mask.south = mask.west = distance;
        mask.mask = null;
        mask.fovType = FovType.CIRCLE;
        mask.distPlusOneSq = (distance + 1) * (distance + 1);
        mask.board = b;
        permissiveFov(x, y, mask);
    }

    /**
     * Algorithm inspired by
     * http://groups.google.com/group/rec.games.roguelike.development/browse_thread/thread/f3506215be9d9f9a/2e543127f705a278#2e543127f705a278
     *
     * @see rlforj.los.ILosAlgorithm#exists(IBoard, int, int, int, int, boolean)
     */
    public boolean exists(final IBoard b, final int startX, final int startY, final int endX, final int endY,
                          final boolean savePath)
    {
        final permissiveMaskT          mask = new permissiveMaskT();
        final int                      dx   = endX - startX;
        final int                      adx  = dx > 0 ? dx : -dx;
        final int                      dy   = endY - startY;
        final int                      ady  = dy > 0 ? dy : -dy;
        final RecordQuadrantVisitBoard fb   = new RecordQuadrantVisitBoard(b, startX, startY, endX, endY, savePath);
        mask.east = mask.west = adx;
        mask.north = mask.south = ady;
        mask.mask = null;
        mask.fovType = FovType.SQUARE;
        mask.distPlusOneSq = 0;
        mask.board = fb;

        final fovStateT state = new fovStateT();
        state.source = new Point(startX, startY);
        state.mask = mask;
        state.board = fb;
        state.isLos = true;
        state.quadrant = new Point(dx < 0 ? -1 : 1, dy < 0 ? -1 : 1);
        state.quadrantIndex = 0;

        final LinkedList<bumpT> steepBumps   = new LinkedList<>();
        final LinkedList<bumpT> shallowBumps = new LinkedList<>();
        // activeFields is sorted from shallow-to-steep.
        final LinkedList<fieldT> activeFields = new LinkedList<>();
        activeFields.addLast(new fieldT());
        activeFields.getLast().shallow.near = new Point(0, 1);
        activeFields.getLast().shallow.far = new Point(adx + 1, 0);
        activeFields.getLast().steep.near = new Point(1, 0);
        activeFields.getLast().steep.far = new Point(0, ady + 1);

        final Point dest = new Point(0, 0);

        final Line2I stopLine = new Line2I(new Point(0, 1),
                                           new Point(adx, ady + 1)), startLine = new Line2I(new Point(1, 0),
                                                                                            new Point(adx + 1, ady));

        // Visit the source square exactly once (in quadrant 1).
        actIsBlocked(state, dest);

        CLikeIterator<fieldT> currentField = new CLikeIterator<>(activeFields.listIterator());
        final int             maxI         = adx + ady;
        // For each square outline
        int         lastStartJ = -1;
        final Point topLeft    = new Point(0, 0), bottomRight = new Point(0, 0);
        for (int i = 1; i <= maxI && !activeFields.isEmpty(); ++i)
        {
            // System.out.println("i "+i);
            int startJ = max(0, i - adx);
            startJ = max(startJ, lastStartJ - 1);
            final int maxJ = min(i, ady);

            // System.out.println("Startj "+startJ+" maxj "+maxJ);
            // Visit the nodes in the outline
            int thisStartJ = -1;
            // System.out.println("startJ "+startJ+" maxJ "+maxJ);
            for (int j = startJ; j <= maxJ && !currentField.isAtEnd(); ++j)
            {
                // System.out.println("i j "+i+" "+j);
                dest.x = i - j;
                dest.y = j;
                topLeft.x = dest.x;
                topLeft.y = dest.y + 1;
                bottomRight.x = dest.x + 1;
                bottomRight.y = dest.y;
                // System.out.println(startLine+" "+topLeft+" "+stopLine+"
                // "+bottomRight);
                // System.out.println("isbelow "+startLine.isBelow(topLeft)+"
                // isabove "+stopLine.isAbove(bottomRight));
                if (startLine.isAboveOrContains(topLeft))
                {
                    // not in range, continue
                    // System.out.println("below start");
                    continue;
                }
                if (stopLine.isBelowOrContains(bottomRight))
                {
                    // done
                    // System.out.println("Above stop ");
                    break;
                }
                // in range
                if (thisStartJ == -1)
                    thisStartJ = j;
                visitSquare(state, dest, currentField, steepBumps, shallowBumps, activeFields);
            }
            lastStartJ = thisStartJ;
            // System.out.println("Activefields size "+activeFields.size());
            currentField = new CLikeIterator<>(activeFields.listIterator());
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

    /*
     * (non-Javadoc)
     *
     * @see sid.los.ILosAlgorithm1#getPath()
     */
    public List<Point> getPath()
    {
        return path;
    }

    class permissiveMaskT
    {
        public FovType fovType;
        public int     distPlusOneSq;
        /*
         * Do not interact with the members directly. Use the provided
         * functions.
         */ int north;
        int    south;
        int    east;
        int    west;
        // int width;
        // int height;
        int[]  mask;
        IBoard board;
    }

    class fovStateT
    {
        public int quadrantIndex;
        public boolean isLos = false;
        Point           source;
        permissiveMaskT mask;
        Object          context;
        Point           quadrant;
        Point           extent;
        IBoard          board;
    }

    class bumpT
    {
        Point location;
        bumpT parent = null;

        public bumpT()
        {
        }

        public String toString()
        {
            return location.toString() + " p( " + parent + " ) ";
        }
    }

    class fieldT
    {
        Line2I steep   = new Line2I(new Point(0, 0), new Point(0, 0));
        Line2I shallow = new Line2I(new Point(0, 0), new Point(0, 0));
        bumpT steepBump;
        bumpT shallowBump;

        public fieldT(final fieldT f)
        {
            steep = new Line2I(new Point(f.steep.near.x, f.steep.near.y), new Point(f.steep.far.x, f.steep.far.y));
            shallow = new Line2I(new Point(f.shallow.near.x, f.shallow.near.y),
                                 new Point(f.shallow.far.x, f.shallow.far.y));
            steepBump = f.steepBump;
            shallowBump = f.shallowBump;
        }

        public fieldT()
        {
            // TODO Auto-generated constructor stub
        }

        public String toString()
        {
            return "[ steep " + steep + ",  shallow " + shallow + "]";
        }
    }
}
