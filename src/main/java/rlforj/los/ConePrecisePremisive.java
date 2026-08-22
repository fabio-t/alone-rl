/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.los;

import rlforj.IBoard;
import rlforj.math.Point;

import java.util.LinkedList;

/**
 * Precise Permissive class for computing cone
 * field of view.
 *
 * @author sdatta
 */
public class ConePrecisePremisive extends PrecisePermissive implements IConeFovAlgorithm
{

    public void visitConeFieldOfView(final IBoard b, final int x, final int y, final int distance, int startAngle,
                                     int endAngle)
    {
        if (startAngle % 90 == 0 && startAngle % 360 != 0)
            startAngle--;//we dont like to start at 90, 180, 270
        // because it is screwed up by the "dont visit an axis twice" logic

        // normalize angled
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

        final permissiveMaskT mask = new permissiveMaskT();
        mask.east = mask.north = mask.south = mask.west = distance;
        mask.mask = null;
        mask.fovType = FovType.CIRCLE;
        mask.distPlusOneSq = (distance + 1) * (distance + 1);
        mask.board = b;
        permissiveConeFov(x, y, mask, startAngle, endAngle);
    }

    /**
     * Process one quadrant.
     */
    private void calculateConeFovQuadrant(final coneFovState state, final int startAngle, final int finishAngle)
    {
        //		 System.out.println("calcfovq called " + state.quadrantIndex + " "
        //				+ startAngle + " " + finishAngle);
        final LinkedList<bumpT> steepBumps   = new LinkedList<>();
        final LinkedList<bumpT> shallowBumps = new LinkedList<>();
        // activeFields is sorted from shallow-to-steep.
        final LinkedList<fieldT> activeFields = new LinkedList<>();
        activeFields.addLast(new fieldT());

        // We decide the farthest cells that can be seen by the cone ( using
        // trigonometry.), then we set the active field to be in between them.
        if (startAngle == 0)
        {
            activeFields.getLast().shallow.near = new Point(0, 1);
            activeFields.getLast().shallow.far = new Point(state.extent.x, 0);
        }
        else
        {
            activeFields.getLast().shallow.near = new Point(0, 1);
            activeFields.getLast().shallow.far = new Point((int) Math.ceil(
                Math.cos(Math.toRadians(startAngle)) * state.extent.x),
                                                           (int) Math.floor(
                                                               Math.sin(Math.toRadians(startAngle)) * state.extent.y));
            //			System.out.println(activeFields.getLast().shallow.isAboveOrContains(new offsetT(0, 10)));
        }
        if (finishAngle == 90)
        {
            activeFields.getLast().steep.near = new Point(1, 0);
            activeFields.getLast().steep.far = new Point(0, state.extent.y);
        }
        else
        {
            activeFields.getLast().steep.near = new Point(1, 0);
            activeFields.getLast().steep.far = new Point((int) Math.floor(
                Math.cos(Math.toRadians(finishAngle)) * state.extent.x),
                                                         (int) Math.ceil(
                                                             Math.sin(Math.toRadians(finishAngle)) * state.extent.y));
        }
        final Point dest = new Point(0, 0);

        //		// Visit the source square exactly once (in quadrant 1).
        //		if (state.quadrant.x == 1 && state.quadrant.y == 1)
        //		{
        //			actIsBlockedCone(state, dest);
        //		}

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
                visitConeSquare(state, dest, currentField, steepBumps, shallowBumps, activeFields);
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

    private void permissiveConeFov(final int sourceX, final int sourceY, final permissiveMaskT mask,
                                   final int startAngle, final int finishAngle)
    {
        final coneFovState state = new coneFovState();
        state.source = new Point(sourceX, sourceY);
        state.mask = mask;
        state.board = mask.board;
        // state.isBlocked = isBlocked;
        // state.visit = visit;
        // state.context = context;

        //visit origin once
        state.board.visit(sourceX, sourceY);

        final Point quadrants[] = { new Point(1, 1), new Point(-1, 1), new Point(-1, -1), new Point(1, -1) };

        final Point extents[] = { new Point(mask.east, mask.north), new Point(mask.west, mask.north),
                                  new Point(mask.west, mask.south), new Point(mask.east, mask.south) };

        final int[] angles = new int[12];
        angles[0] = 0;
        angles[1] = 90;
        angles[2] = 180;
        angles[3] = 270;
        for (int i = 4; i < 12; i++)
            angles[i] = 720;//to keep them at the end
        int i;
        for (i = 0; i < 4; i++)
        {
            if (startAngle < angles[i])
            {
                for (int j = 3; j >= i; j--)
                    angles[j + 1] = angles[j];
                break;
            }
        }
        angles[i] = startAngle;
        for (i = 0; i < 5; i++)
        {
            if (finishAngle < angles[i])
            {
                for (int j = 4; j >= i; j--)
                    angles[j + 1] = angles[j];
                break;
            }
        }
        angles[i] = finishAngle;
        // Now angles[0..5] contains 0, 90, 180, 270, startAngle, finishAngle
        // in sorted order.
        int startIndex = 0;
        for (i = 0; i < 6; i++)
        {
            //			System.out.println("sorted "+angles[i]);
            angles[i + 6] = angles[i];
            if (angles[i] == startAngle)
                startIndex = i;
        }
        // Twice repeated, also foound out startAngle's index

        //effectively, what we do is:
        // traverse startAngle -> next axis(say 90), 90->180,
        // ...., some axis -> finishAngle.
        // Or startAngle -> endAngle if in same quadrant
        int stA = 0, endA = 0;
        for (i = startIndex; i < 12; i++)
        {
            if (angles[i] == finishAngle)
                break;
            final int quadrantIndex = angles[i] / 90;
            switch (quadrantIndex)
            {
                case 0:
                    stA = angles[i];
                    endA = angles[i + 1];
                    break;
                case 1:
                    stA = 180 - angles[i + 1];
                    endA = 180 - angles[i];
                    break;
                case 2:
                    stA = angles[i] - 180;
                    endA = angles[i + 1] - 180;
                    break;
                case 3:
                    stA = 360 - angles[i + 1];
                    endA = 360 - angles[i];
                    break;
            }
            state.quadrant = quadrants[quadrantIndex];
            state.extent = extents[quadrantIndex];
            state.quadrantIndex = quadrantIndex;

            calculateConeFovQuadrant(state, stA, endA);
            //			System.out.println(quadrantIndex+" "+stA+" "+endA);

            if (stA == 0)
                state.axisDone[quadrantIndex] = true;
            if (endA == 90)
                state.axisDone[(quadrantIndex + 1) % 4] = true;
            //			System.out.println(Arrays.toString(state.axisDone));
        }
    }

    /**
     * It is here so that actisBlockedCone is called
     * instead of actIsBlocked.
     * <p>
     * Note : I ( sdatta ) made the function name with Code added since
     * I wasnt sure inheritance was working properly when I was debugging this code.
     * Maybe this code can be simplified ?
     */
    private void visitConeSquare(final coneFovState state, final Point dest, final CLikeIterator<fieldT> currentField,
                                 final LinkedList<bumpT> steepBumps, final LinkedList<bumpT> shallowBumps,
                                 final LinkedList<fieldT> activeFields)
    {
        // System.out.println("visitsq called "+dest);
        // The top-left and bottom-right corners of the destination square.
        final Point topLeft     = new Point(dest.x, dest.y + 1);
        final Point bottomRight = new Point(dest.x + 1, dest.y);
        //		System.out.println(dest);
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
            //			System.out.println("currFld.steep.isBelowOrContains(bottomRight) and shallow "+
            //					currentField.getCurrent().shallow.isAboveOrContains(bottomRight)+" "+
            //					currentField.getCurrent().steep.isBelowOrContains(topLeft));

            if (currentField.getCurrent().shallow.isAboveOrContains(bottomRight) &&
                currentField.getCurrent().steep.isBelowOrContains(topLeft))
            {
                break;
            }

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
            //					+ currentField.getCurrent() +
            //					currentField.getCurrent().shallow.isAboveOrContains(topLeft)+" "+
            //					currentField.getCurrent().shallow.isAboveOrContains(bottomRight)+" "+
            //					currentField.getCurrent().steep.isAboveOrContains(topLeft)+" "+
            //					currentField.getCurrent().steep.isAboveOrContains(bottomRight));

            return;
        }
        // The square is between the lines in some way. This means that we
        // need to visit it and determine whether it is blocked.

        final boolean isBlocked = actIsBlockedCone(state, dest);
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
            final fieldT steeperField   = new fieldT(currentField.getCurrent());
            final fieldT shallowerField = currentField.getCurrent();
            currentField.insertBeforeCurrent(steeperField);
            // System.out.println("activeFields "+activeFields);
            addSteepBump(bottomRight, shallowerField, steepBumps, shallowBumps);
            currentField.gotoPrevious();
            if (!checkField(currentField)) // did not remove
                currentField.gotoNext();// point to the original element
            //			System.out.println("B4 addShallowBumps "
            //					+ currentField.getCurrent());
            addShallowBump(topLeft, steeperField, steepBumps, shallowBumps);
            checkField(currentField);
        }
    }

    /**
     * Visit the square, also decide if it is blocked
     */
    private boolean actIsBlockedCone(final coneFovState state, final Point pos)
    {
        final Point stateQuadrant = state.quadrant;
        final Point adjustedPos = new Point(pos.x * stateQuadrant.x + state.source.x,
                                            pos.y * stateQuadrant.y + state.source.y);

        //Keep track of which axes are done.
        if ((pos.x == 0 && stateQuadrant.y > 0 && !state.axisDone[1]) ||
            (pos.x == 0 && stateQuadrant.y < 0 && !state.axisDone[3]) ||
            (pos.y == 0 && stateQuadrant.x > 0 && !state.axisDone[0]) ||
            (pos.y == 0 && stateQuadrant.x < 0 && !state.axisDone[2]) || (pos.x != 0 && pos.y != 0))
            if (doesPermissiveVisit(state.mask, pos.x * stateQuadrant.x, pos.y * stateQuadrant.y) == 1)
            {
                state.board.visit(adjustedPos.x, adjustedPos.y);
            }
        return state.board.blocksLight(adjustedPos.x, adjustedPos.y);
    }

    public class coneFovState extends fovStateT
    {
        public boolean axisDone[] = { false, false, false, false };
    }
}
