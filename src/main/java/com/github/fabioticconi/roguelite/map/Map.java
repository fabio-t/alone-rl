/**
 * Copyright 2015 Fabio Ticconi
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.fabioticconi.roguelite.map;

import com.github.fabioticconi.roguelite.constants.Cell;
import com.github.fabioticconi.roguelite.constants.Options;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.utils.Coords;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import rlforj.los.ILosBoard;
import rlforj.los.PrecisePermissive;
import rlforj.math.Point2I;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Fabio Ticconi
 */
public class Map implements ILosBoard
{
    final Cell  map[][];

    /* FOV/LOS stuff */
    final LongSet lastVisited;
    PrecisePermissive view;

    public Map() throws IOException
    {
        lastVisited = new LongOpenHashSet();
        view = new PrecisePermissive();

        final BufferedImage img = ImageIO.read(new File("map.png"));

        final byte [] elevation = Files.readAllBytes(Paths.get("elevation.data"));

        Options.MAP_SIZE_X = img.getWidth();
        Options.MAP_SIZE_Y = img.getHeight();

        map = new Cell[Options.MAP_SIZE_X][Options.MAP_SIZE_Y];

        float value;

        for (int x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (int y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final byte h = elevation[x * Options.MAP_SIZE_X + y];
                value = (float)(h+128) / 255f;

                if ((x == 0 && y == 0) || (x == 780 && y == 900) || (x == 1137 && y == 633) || (x == 759 && y == 663) || (x == 435 && y == 838) || (x == 708 && y == 681))
                {
                    System.out.format("(%d,%d) -> %04x %d %f\n", x, y, h, h, value);
                }

                final float water = 0.3f;

                if (value < water)
                {
                    if (value < water * 0.7f)
                    {
                        map[x][y] = Cell.DEEP_WATER;
                    }
                    else
                    {
                        map[x][y] = Cell.WATER;
                    }
                }
                else
                {
                    // final float val = t;
                    // normalize val so 0 is at water level
                    final float val = (value - water) / (1.0f - water);

                    // set color based on above the see level
                    // beach, plain, forest, mountains etc

                    if (val < 0.1f)
                    {
                        map[x][y] = Cell.SAND;
                    }
                    else if (val < 0.3f)
                    {
                        map[x][y] = Cell.GRASS;
                    }
                    else if (val < 0.55f)
                    {
                        map[x][y] = Cell.HILL;
                    }
                    else if (val < 0.7f)
                    {
                        map[x][y] = Cell.MOUNTAIN;
                    }
                    else
                    {
                        map[x][y] = Cell.HIGH_MOUNTAIN;
                    }
                }
            }
        }
    }

    /**
     * Take a position and returns the first free exit (if any), starting from
     * North and going clockwise.
     *
     * @param x
     * @param y
     * @return An exit, or HERE if none is available
     */
    public Side getFirstFreeExit(final int x, final int y)
    {
        int side_x = Side.N.x + x;
        int side_y = Side.N.y + y;

        if (!isObstacle(side_x, side_y))
            return Side.N;

        side_x = Side.E.x + x;
        side_y = Side.E.y + y;

        if (!isObstacle(side_x, side_y))
            return Side.E;

        side_x = Side.S.x + x;
        side_y = Side.S.y + y;

        if (!isObstacle(side_x, side_y))
            return Side.S;

        side_x = Side.W.x + x;
        side_y = Side.W.y + y;

        if (!isObstacle(side_x, side_y))
            return Side.W;

        return Side.HERE;
    }

    /**
     * Takes a position and returns a free exit (if available), with some
     * randomisation (not guaranteed).
     *
     * @param x
     * @param y
     * @return An exit, or HERE if none is available
     */
    public Side getFreeExitRandomised(final int x, final int y)
    {
        final Side firstFree = getFirstFreeExit(x, y);

        if (firstFree == Side.HERE)
            return firstFree;

        final Side random = Side.getRandom();

        if (isObstacle(x, y, random))
            return firstFree;
        else
            return random;
    }

    /**
     * Searches concentrically for a cell of the specified type, and returns the
     * coordinate array if it finds one.
     *
     * @return null if none could be found, the coordinate array otherwise
     */
    public int[] getFirstOfType(final int x, final int y, final int r, final EnumSet<Cell> set)
    {
        if (set.contains(map[x][y]))
            return new int[] { x, y };

        lastVisited.clear();

        view.visitFieldOfView(this, x, y, r);

        int[] coords;
        Cell  cell;
        for (final long key : lastVisited)
        {
            coords = Coords.unpackCoords(key);
            cell = map[coords[0]][coords[1]];

            if (set.contains(cell))
                return coords;
        }

        return null;
    }

    public Cell get(final int x, final int y)
    {
        if (contains(x, y))
            return map[x][y];

        return Cell.EMPTY;
    }

    public void set(final int x, final int y, final Cell type)
    {
        if (contains(x, y))
        {
            map[x][y] = type;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see rlforj.los.ILosBoard#contains(int, int)
     */
    @Override
    public boolean contains(final int x, final int y)
    {
        return x >= 0 && x < Options.MAP_SIZE_X && y >= 0 && y < Options.MAP_SIZE_Y;
    }

    /*
     * (non-Javadoc)
     *
     * @see rlforj.los.ILosBoard#isObstacle(int, int)
     */
    @Override
    public boolean isObstacle(final int x, final int y)
    {
        return x >= Options.MAP_SIZE_X || x < 0 || y >= Options.MAP_SIZE_Y || y < 0 || map[x][y] == Cell.WALL ||
               map[x][y] == Cell.CLOSED_DOOR;
    }

    public boolean isObstacle(final int x, final int y, final Side direction)
    {
        return isObstacle(x + direction.x, y + direction.y);
    }

    /*
     * (non-Javadoc)
     *
     * @see rlforj.los.ILosBoard#visit(int, int)
     */
    @Override
    public void visit(final int x, final int y)
    {
        lastVisited.add(Coords.packCoords(x, y));
    }

    public LongSet getVisibleCells(final int x, final int y, final int r)
    {
        lastVisited.clear();

        view.visitFieldOfView(this, x, y, r);

        return LongSets.unmodifiable(lastVisited);
    }

    public List<Point2I> getLineOfSight(final int startX, final int startY, final int endX, final int endY)
    {
        final boolean exists = view.existsLineOfSight(this, startX, startY, endX, endY, true);

        if (exists)
            return view.getProjectPath();

        return null;
    }
}
