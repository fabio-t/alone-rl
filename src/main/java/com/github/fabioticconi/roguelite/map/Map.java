/*
 * Copyright (C) 2017 Fabio Ticconi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.fabioticconi.roguelite.map;

import com.github.fabioticconi.roguelite.constants.Cell;
import com.github.fabioticconi.roguelite.constants.Options;
import com.github.fabioticconi.roguelite.constants.Side;
import com.github.fabioticconi.roguelite.utils.Coords;
import com.github.fabioticconi.roguelite.utils.Util;
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
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Fabio Ticconi
 */
public class Map implements ILosBoard
{
    final Cell    terrain[][];
    final boolean obstacles[][];

    /* FOV/LOS stuff */
    final LongSet lastVisited;
    PrecisePermissive view;

    public Map() throws IOException
    {
        lastVisited = new LongOpenHashSet();
        view = new PrecisePermissive();

        final BufferedImage img = ImageIO.read(new File("data/map/map.png"));

        final byte[] elevation = Files.readAllBytes(Paths.get("data/map/elevation.data"));

        Options.MAP_SIZE_X = img.getWidth();
        Options.MAP_SIZE_Y = img.getHeight();

        terrain = new Cell[Options.MAP_SIZE_X][Options.MAP_SIZE_Y];
        obstacles = new boolean[Options.MAP_SIZE_X][Options.MAP_SIZE_Y];

        float value;

        for (int x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (int y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final byte h     = elevation[x * Options.MAP_SIZE_X + y];
                final int  uns_h = Byte.toUnsignedInt(h);
                value = (float) (uns_h) / 255f;

                if (value == 0f)
                {
                    terrain[x][y] = Cell.DEEP_WATER;
                }
                else if (value < 0.005f)
                {
                    terrain[x][y] = Cell.WATER;
                }
                else if (value < 0.03f)
                {
                    terrain[x][y] = Cell.SAND;
                }
                else if (value < 0.04f)
                {
                    terrain[x][y] = Cell.GROUND;
                }
                else if (value < 0.7f)
                {
                    terrain[x][y] = Cell.GRASS;
                }
                else if (value < 0.8f)
                {
                    terrain[x][y] = Cell.HILL_GRASS;
                }
                else if (value < 0.9f)
                {
                    terrain[x][y] = Cell.HILL;
                }
                else if (value < 0.99f)
                {
                    terrain[x][y] = Cell.MOUNTAIN;
                }
                else
                {
                    terrain[x][y] = Cell.HIGH_MOUNTAIN;
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
        if (set.contains(terrain[x][y]))
            return new int[] { x, y };

        lastVisited.clear();

        view.visitFieldOfView(this, x, y, r);

        int[] coords;
        Cell  cell;
        for (final long key : lastVisited)
        {
            coords = Coords.unpackCoords(key);
            cell = terrain[coords[0]][coords[1]];

            if (set.contains(cell))
                return coords;
        }

        return null;
    }

    public Cell get(final int x, final int y)
    {
        if (contains(x, y))
            return terrain[x][y];

        return Cell.EMPTY;
    }

    public void set(final int x, final int y, final Cell type)
    {
        if (contains(x, y))
        {
            terrain[x][y] = type;
        }
    }

    public void setObstacle(final int x, final int y)
    {
        obstacles[x][y] = true;
    }

    public void unsetObstacle(final int x, final int y)
    {
        obstacles[x][y] = false;
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
        return Util.outRange(x, 0, Options.MAP_SIZE_X) || Util.outRange(y, 0, Options.MAP_SIZE_Y) || obstacles[x][y];
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
