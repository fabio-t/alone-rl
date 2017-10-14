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
package com.github.fabioticconi.alone.map;

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.alone.components.Obstacle;
import com.github.fabioticconi.alone.constants.Cell;
import com.github.fabioticconi.alone.constants.Options;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.utils.Coords;
import com.github.fabioticconi.alone.utils.Util;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.los.ILosBoard;
import rlforj.los.PrecisePermissive;
import rlforj.math.Point2I;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabio Ticconi
 */
public class MapSystem extends PassiveSystem implements ILosBoard
{
    static final Logger log = LoggerFactory.getLogger(MapSystem.class);

    final Cell terrain[][];

    /* FOV/LOS stuff */
    final LongSet lastVisited;
    ComponentMapper<Obstacle> mObstacle;

    @Wire
    SingleGrid obstacles;

    PrecisePermissive view;

    public MapSystem() throws IOException
    {
        lastVisited = new LongOpenHashSet();
        view = new PrecisePermissive();

        final InputStream mapStream       = new FileInputStream("data/map/map.png");
        final InputStream elevationStream = new FileInputStream("data/map/elevation.data");

        final BufferedImage img       = ImageIO.read(mapStream);
        final byte[]        elevation = IOUtils.toByteArray(elevationStream);

        Options.MAP_SIZE_X = img.getWidth();
        Options.MAP_SIZE_Y = img.getHeight();

        terrain = new Cell[Options.MAP_SIZE_X][Options.MAP_SIZE_Y];

        float value;

        for (int x = 0; x < Options.MAP_SIZE_X; x++)
        {
            for (int y = 0; y < Options.MAP_SIZE_Y; y++)
            {
                final byte h     = elevation[x * Options.MAP_SIZE_X + y];
                final int  uns_h = Byte.toUnsignedInt(h);
                value = (float) (uns_h) / 255f;

                if (value < 0.01)
                {
                    terrain[x][y] = Cell.DEEP_WATER;
                }
                else if (value < 0.05f)
                {
                    terrain[x][y] = Cell.WATER;
                }
                else if (value < 0.08f)
                {
                    terrain[x][y] = Cell.SAND;
                }
                else if (value < 0.1f)
                {
                    terrain[x][y] = Cell.GROUND;
                }
                else if (value < 0.4f)
                {
                    terrain[x][y] = Cell.GRASS;
                }
                else if (value < 0.7f)
                {
                    terrain[x][y] = Cell.HILL_GRASS;
                }
                else if (value < 0.8f)
                {
                    terrain[x][y] = Cell.HILL;
                }
                else if (value < 0.9f)
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
     * Take a position and return all free exits in the surrounding "circle".
     *
     * @param x
     * @param y
     * @return A set of free exits, or HERE if none is available
     */
    public Set<Side> getFreeExits(final int x, final int y)
    {
        int xn, yn;

        final Set<Side> exits = new ObjectArraySet<>(8);

        for (final Side side : Side.values())
        {
            if (side.equals(Side.HERE))
                continue;

            xn = x + side.x;
            yn = y + side.y;

            if (contains(xn, yn) && obstacles.get(xn, yn) < 0)
                exits.add(side);
        }

        return exits;
    }

    /**
     * Take a position and returns the first free exit (if any).
     *
     * @param x
     * @param y
     * @return A free exit, or HERE if none is available
     */
    public Side getFirstFreeExit(final int x, final int y)
    {
        int xn, yn;

        for (final Side side : Side.values())
        {
            if (side.equals(Side.HERE))
                continue;

            xn = x + side.x;
            yn = y + side.y;

            if (contains(xn, yn) && obstacles.get(xn, yn) < 0)
                return side;
        }

        return Side.HERE;
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

    /**
     * It is NOT equivalent to !isObstacle(x,y). This function only returns true if the cell is within bounds
     * and does not contain any creature, regardless of visibility status.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isFree(final int x, final int y)
    {
        return contains(x, y) && obstacles.get(x, y) < 0;
    }

    public boolean isFree(final int x, final int y, final Side direction)
    {
        return isFree(x + direction.x, y + direction.y);
    }

    @Override
    public boolean contains(final int x, final int y)
    {
        return Util.inRange(x, 0, Options.MAP_SIZE_X) && Util.inRange(y, 0, Options.MAP_SIZE_Y);
    }

    @Override
    public boolean isObstacle(final int x, final int y)
    {
        // outside boundaries is "obstacle"
        if (!contains(x, y))
            return true;

        final int entityId = obstacles.get(x, y);

        // must check for -1
        return entityId >= 0 && mObstacle.has(entityId);
    }

    public boolean isObstacle(final int x, final int y, final Side direction)
    {
        return isObstacle(x + direction.x, y + direction.y);
    }

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
