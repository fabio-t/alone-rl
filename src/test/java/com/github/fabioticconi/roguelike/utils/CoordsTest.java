package com.github.fabioticconi.roguelike.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CoordsTest
{

    @Test
    public void testDistances() throws Exception
    {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testVisitVonNeumannNeighbours() throws Exception
    {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testVisitMooreNeighbours() throws Exception
    {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testPackUnpackCoords() throws Exception
    {
        final int[] x = { 0, 100, 1000, Integer.MAX_VALUE - 1, Integer.MAX_VALUE };
        final int[] y = { 0, 100, 1000, Integer.MAX_VALUE - 1, Integer.MAX_VALUE };

        for (int i = 0; i < x.length; i++)
        {
            final long key = Coords.packCoords(x[i], y[i]);

            final int[] coords = Coords.unpackCoords(key);

            assertEquals(coords[0], x[i]);
            assertEquals(coords[1], y[i]);
        }
    }
}
