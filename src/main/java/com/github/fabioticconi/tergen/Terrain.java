package com.github.fabioticconi.tergen;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.Path;

/**
 * A generated terrain, owning native memory.
 *
 * <p>Instances are {@link AutoCloseable}: use try-with-resources, or call
 * {@link #close()} when done. Reading a layer after closing throws
 * {@link IllegalStateException} rather than reading freed memory.
 *
 * <p>Layer data lives in the native library. {@link #floats} and
 * {@link #mask} copy it into Java arrays, which is usually what you want;
 * {@link #segment} hands back a read-only view for the zero-copy case (for
 * example uploading a 1024×1024 heightmap — 4 MB — straight to a GPU
 * buffer without a Java-side copy).
 */
public final class Terrain implements AutoCloseable {

    private MemorySegment handle;
    private final int width;
    private final int height;
    private final float seaLevel;

    Terrain(MemorySegment handle) {
        this.handle = handle;
        this.width = (int) (long) TerrainGenerator.call(TerrainGenerator.TG_TERRAIN_WIDTH, handle);
        this.height = (int) (long) TerrainGenerator.call(TerrainGenerator.TG_TERRAIN_HEIGHT, handle);
        this.seaLevel = (float) TerrainGenerator.call(TerrainGenerator.TG_TERRAIN_SEA_LEVEL, handle);
    }

    /** Map width in cells. */
    public int width() {
        return width;
    }

    /** Map height in cells. */
    public int height() {
        return height;
    }

    /** The sea level this terrain was generated with. */
    public float seaLevel() {
        return seaLevel;
    }

    /** Number of cells, {@code width * height}. */
    public int cellCount() {
        return width * height;
    }

    private MemorySegment handle() {
        if (handle == null) {
            throw new IllegalStateException("terrain has already been closed");
        }
        return handle;
    }

    /**
     * A read-only view of a layer's native memory, valid until this
     * terrain is closed.
     *
     * <p>Prefer {@link #floats} or {@link #mask} unless you specifically
     * want to avoid the copy.
     */
    public MemorySegment segment(Layer layer) {
        MemorySegment self = handle();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment lenOut = arena.allocate(ValueLayout.JAVA_LONG);
            MemorySegment ptr;
            long elementSize;

            switch (layer.elementType()) {
                case FLOAT -> {
                    ptr = (MemorySegment) TerrainGenerator.call(
                            TerrainGenerator.TG_TERRAIN_LAYER_F32, self, layer.id(), lenOut);
                    elementSize = Float.BYTES;
                }
                case MASK -> {
                    ptr = (MemorySegment) TerrainGenerator.call(
                            TerrainGenerator.TG_TERRAIN_LAYER_U8, self, layer.id(), lenOut);
                    elementSize = Byte.BYTES;
                }
                default -> throw new IllegalArgumentException(
                        layer + " is an image-only layer; use savePng instead");
            }

            if (ptr.equals(MemorySegment.NULL)) {
                throw new TerrainException(3, TerrainGenerator.lastError());
            }

            long cells = lenOut.get(ValueLayout.JAVA_LONG, 0);
            return ptr.reinterpret(cells * elementSize).asReadOnly();
        }
    }

    /**
     * Copies a float layer ({@link Layer#HEIGHT}, {@link Layer#FLOW},
     * {@link Layer#FLOW_ACCUMULATION}) into a new array, row-major.
     */
    public float[] floats(Layer layer) {
        if (layer.elementType() != Layer.ElementType.FLOAT) {
            throw new IllegalArgumentException(layer + " is not a float layer");
        }
        return segment(layer).toArray(ValueLayout.JAVA_FLOAT);
    }

    /**
     * Copies a mask layer ({@link Layer#RIVERS}, {@link Layer#LAKES},
     * {@link Layer#OCEAN}) into a new array of 0/1 bytes, row-major.
     */
    public byte[] mask(Layer layer) {
        if (layer.elementType() != Layer.ElementType.MASK) {
            throw new IllegalArgumentException(layer + " is not a mask layer");
        }
        return segment(layer).toArray(ValueLayout.JAVA_BYTE);
    }

    /** Elevation at {@code (x, y)}, in {@code [0, 1]}. */
    public float heightAt(int x, int y) {
        return segment(Layer.HEIGHT).get(ValueLayout.JAVA_FLOAT, (long) index(x, y) * Float.BYTES);
    }

    /** Raw upstream catchment at {@code (x, y)}, in cells. */
    public float flowAccumulationAt(int x, int y) {
        return segment(Layer.FLOW_ACCUMULATION)
                .get(ValueLayout.JAVA_FLOAT, (long) index(x, y) * Float.BYTES);
    }

    /** Whether a river runs through {@code (x, y)}. */
    public boolean isRiver(int x, int y) {
        return segment(Layer.RIVERS).get(ValueLayout.JAVA_BYTE, index(x, y)) == 1;
    }

    /** Whether {@code (x, y)} lies in a lake. */
    public boolean isLake(int x, int y) {
        return segment(Layer.LAKES).get(ValueLayout.JAVA_BYTE, index(x, y)) == 1;
    }

    /**
     * Whether {@code (x, y)} is ocean: at or below sea level and connected
     * to the map border. Enclosed low ground is not ocean.
     */
    public boolean isOcean(int x, int y) {
        return segment(Layer.OCEAN).get(ValueLayout.JAVA_BYTE, index(x, y)) == 1;
    }

    /** Whether {@code (x, y)} is covered by ocean, lake or river. */
    public boolean isWater(int x, int y) {
        return isOcean(x, y) || isLake(x, y) || isRiver(x, y);
    }

    private int index(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IndexOutOfBoundsException("(" + x + ", " + y + ") outside " + width + "x" + height);
        }
        return y * width + x;
    }

    /**
     * Writes one layer to {@code path} as a PNG. {@link Layer#SHADED} is
     * the relief render; data layers are written losslessly (elevation and
     * flow as 16-bit greyscale, masks as 8-bit black/white).
     *
     * @throws TerrainException if the file cannot be written
     */
    public void savePng(Layer layer, Path path) {
        MemorySegment self = handle();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nativePath = arena.allocateFrom(path.toString());
            TerrainGenerator.check(
                    (int) TerrainGenerator.call(TerrainGenerator.TG_TERRAIN_SAVE_PNG, self, layer.id(), nativePath));
        }
    }

    /** Frees the native terrain. Idempotent. */
    @Override
    public void close() {
        if (handle != null) {
            TerrainGenerator.call(TerrainGenerator.TG_TERRAIN_FREE, handle);
            handle = null;
        }
    }
}
