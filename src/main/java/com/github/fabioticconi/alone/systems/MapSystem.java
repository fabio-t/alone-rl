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
package com.github.fabioticconi.alone.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fabioticconi.alone.components.Obstacle;
import com.github.fabioticconi.alone.constants.Options;
import com.github.fabioticconi.alone.constants.Side;
import com.github.fabioticconi.alone.constants.TerrainType;
import com.github.fabioticconi.alone.utils.Coords;
import com.github.fabioticconi.alone.utils.LongBag;
import com.github.fabioticconi.alone.utils.SingleGrid;
import com.github.fabioticconi.alone.utils.Util;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.IBoard;
import rlforj.los.BresLos;
import rlforj.los.IFovAlgorithm;
import rlforj.los.ILosAlgorithm;
import rlforj.los.ShadowCasting;
import rlforj.math.Point;
import rlforj.pathfinding.AStar;
import rlforj.pathfinding.IPathAlgorithm;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

/**
 * @author Fabio Ticconi
 */
public class MapSystem extends PassiveSystem implements IBoard
{
    static final Logger log = LoggerFactory.getLogger(MapSystem.class);

    Cell terrain[][];

    /* FOV/LOS stuff */ LongBag                   lastVisited;
                        IPathAlgorithm            path;
                        IFovAlgorithm             fov;
                        ILosAlgorithm             los;
                        SingleGrid                obstacles;
                        SingleGrid                items;
                        ComponentMapper<Obstacle> mObstacle;

    @Wire
    ObjectMapper mapper;

    Map<String, Cell>    templates;
    TreeMap<Float, Cell> cellAtHeight;

    public void loadTemplates() throws IOException
    {
        final InputStream fileStream = new FileInputStream("data/map/terrain.yml");

        templates = mapper.readValue(fileStream, new TypeReference<HashMap<String, Cell>>()
        {
        });

        cellAtHeight = new TreeMap<>();

        for (final Map.Entry<String, Cell> entry : templates.entrySet())
        {
            final Cell temp = entry.getValue();
            temp.tag = entry.getKey();
            cellAtHeight.put(temp.theight, temp);
        }
    }

    public MapSystem() throws IOException
    {

    }

    @Override
    protected void initialize()
    {
        lastVisited = new LongBag(256);
        fov = new ShadowCasting();
        los = new BresLos(true);

        try
        {
            final InputStream mapStream       = new FileInputStream("data/map/map.png");
            final InputStream elevationStream = new FileInputStream("data/map/elevation.data");

            final BufferedImage img       = ImageIO.read(mapStream);
            final byte[]        elevation = elevationStream.readAllBytes();

            Options.MAP_SIZE_X = img.getWidth();
            Options.MAP_SIZE_Y = img.getHeight();

            terrain = new Cell[Options.MAP_SIZE_X][Options.MAP_SIZE_Y];
            obstacles = new SingleGrid(Options.MAP_SIZE_X, Options.MAP_SIZE_Y);
            items = new SingleGrid(Options.MAP_SIZE_X, Options.MAP_SIZE_Y);

            path = new AStar(this, Options.MAP_SIZE_X, Options.MAP_SIZE_Y, true);

            loadTemplates();

            // TODO: we should make Cell a class, and obviously use a pool.
            // This way we can load the cells from a yaml file, with their thresholds, colours, characters etc,
            // and then we aren't stuck anymore with a few discrete terrain types but we can colour with a gradient,
            // for example.
            // movement costs should also be part of this cell.

            for (int x = 0; x < Options.MAP_SIZE_X; x++)
            {
                for (int y = 0; y < Options.MAP_SIZE_Y; y++)
                {
                    final byte h     = elevation[x * Options.MAP_SIZE_X + y];
                    final int  uns_h = Byte.toUnsignedInt(h);
                    final float value = (float) (uns_h) / 255f;

                    final float key = cellAtHeight.higherKey(value);
                    final Cell cell = cellAtHeight.get(key);

                    terrain[x][y] = cell;
                }
            }
        } catch (final IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        log.info("initialised");
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

        final Set<Side> exits = new LinkedHashSet<>(8);

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
     * Take a position and return all free exits in the surrounding "circle" that have a valid terrain
     * type.
     *
     * @param x
     * @param y
     * @return A set of free exits, or HERE if none is available
     */
    public Set<Side> getFreeExits(final int x, final int y, final EnumSet<TerrainType> set)
    {
        int xn, yn;

        final Set<Side> exits = new LinkedHashSet<>(8);

        for (final Side side : Side.values())
        {
            if (side.equals(Side.HERE))
                continue;

            xn = x + side.x;
            yn = y + side.y;

            if (contains(xn, yn) && obstacles.get(xn, yn) < 0 && set.contains(terrain[x][y].type))
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

    public Point getFirstTotallyFree(final int x, final int y, final int maxRadius)
    {
        if (!contains(x, y))
            return null;

        if (isTotallyFree(x, y))
            return new Point(x, y);
        else if (maxRadius == 0)
            return null;

        final int r = maxRadius < 0 ? Math.max(Options.MAP_SIZE_X, Options.MAP_SIZE_Y) : maxRadius;

        int cur_y = y - 1;
        int cur_x = x;
        for (int d = 1; d <= r; d++)
        {
            // FIXME what do we do if the north row is "out of bound" already?
            // we should skip the next for and position ourselves immediately to
            // the correct east-side column, at the same y position as we are

            final int max_x = x + d;
            final int max_y = y + d;
            final int min_x = x - d;
            final int min_y = y - d;

            // continue east, through the north row
            for (; cur_x < max_x; cur_x++)
            {
                // if we are out of bounds
                if (cur_x < 0 || cur_x >= Options.MAP_SIZE_X)
                {
                    continue;
                }

                if (isTotallyFree(cur_x, cur_y))
                {
                    return new Point(cur_x, cur_y);
                }
            }

            // continue south, through the east column
            for (; cur_y < max_y; cur_y++)
            {
                // if we are out of bounds
                if (cur_y < 0 || cur_y >= Options.MAP_SIZE_Y)
                {
                    continue;
                }

                if (isTotallyFree(cur_x, cur_y))
                {
                    return new Point(cur_x, cur_y);
                }
            }

            // continue west, through the south row
            for (; cur_x > min_x; cur_x--)
            {
                // if we are out of bounds
                if (cur_x < 0 || cur_x >= Options.MAP_SIZE_X)
                {
                    continue;
                }

                if (isTotallyFree(cur_x, cur_y))
                {
                    return new Point(cur_x, cur_y);
                }
            }

            // continue north, through the west column of this circle
            for (; cur_y >= min_y; cur_y--)
            {
                // if we are out of bounds
                if (cur_y < 0 || cur_y >= Options.MAP_SIZE_Y)
                {
                    continue;
                }

                if (isTotallyFree(cur_x, cur_y))
                {
                    return new Point(cur_x, cur_y);
                }
            }

            // at this point we are positioned WITHIN the north row of the next
            // cycle
        }

        // if we are here, we haven't found any free cells

        return null;
    }

    /**
     * Searches concentrically for a cell of the specified type, and returns the
     * coordinate array if it finds one.
     *
     * @return null if none could be found, the coordinate array otherwise
     */
    public int[] getFirstOfType(final int x, final int y, final int r, final EnumSet<TerrainType> set)
    {
        if (set.contains(terrain[x][y].type))
            return new int[] { x, y };

        lastVisited.clear();

        fov.visitFoV(this, x, y, r);

        int[] coords;
        Cell  cell;
        for (int i = 0, size = lastVisited.size(); i < size; i++)
        {
            final long key = lastVisited.get(i);

            coords = Coords.unpackCoords(key);
            cell = terrain[coords[0]][coords[1]];

            if (set.contains(cell.type))
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

    public SingleGrid getObstacles()
    {
        return obstacles;
    }

    public SingleGrid getItems()
    {
        return items;
    }

    /**
     * This function only returns true if the cell is within bounds
     * and does not contain any obstacle or item, regardless of visibility status.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isTotallyFree(final int x, final int y)
    {
        return isFree(x, y) && items.get(x, y) < 0;
    }

    /**
     * This function only returns true if the cell is within bounds
     * and does not contain any obstacle, regardless of visibility status.
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
        return Util.in(x, 0, Options.MAP_SIZE_X - 1) && Util.in(y, 0, Options.MAP_SIZE_Y - 1);
    }

    @Override
    public boolean blocksLight(final int x, final int y)
    {
        // outside boundaries is pitch black
        if (!contains(x, y))
            return true;

        final int entityId = obstacles.get(x, y);

        // currently no tile blocks light by itself, so if there's no creature
        // here we know that light passes.
        return entityId >= 0 && mObstacle.has(entityId);
    }

    @Override
    public boolean blocksStep(final int x, final int y)
    {
        return !isFree(x, y);
    }

    @Override
    public void visit(final int x, final int y)
    {
        lastVisited.add(Coords.packCoords(x, y));
    }

    public LongBag getVisibleCells(final int x, final int y, final int r)
    {
        lastVisited.clear();

        fov.visitFoV(this, x, y, r);

        return lastVisited;
    }

    public List<Point> getLineOfSight(final int startX, final int startY, final int endX, final int endY)
    {
        final boolean exists = los.exists(this, startX, startY, endX, endY, true);

        // FIXME: rlforj-alt should either always return a list, or always an array

        if (exists)
            return los.getPath();

        return null;
    }

    public Point[] getPath(final int startX, final int startY, final int endX, final int endY, final int radius)
    {
        return path.findPath(startX, startY, endX, endY, radius);
    }

    /**
     * @author Fabio Ticconi
     */
    public static class Cell implements Comparable<Float>
    {
        public final static Cell EMPTY = new Cell("empty", ' ', Color.BLACK, null, 0f);

        public String tag;

        public char  c;
        public Color col;
        public TerrainType type;

        public float theight;

        public Cell()
        {

        }

        public Cell(final String tag, final char c, final Color col, final TerrainType type, final float theight)
        {
            this.tag = tag;
            this.c = c;
            this.col = col;
            this.type = type;
            this.theight = theight;
        }

        @Override
        public int compareTo(final Float o)
        {
            return Float.compare(theight, o);
        }
    }
}
