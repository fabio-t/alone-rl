package com.github.fabioticconi.tergen;

/**
 * One output layer of a generated {@link Terrain}.
 *
 * <p>Layers are raw data, not interpretations: this library reports what
 * the terrain <em>is</em> (elevation, catchment, where standing water sits)
 * and leaves what it <em>means</em> — habitability, drinkability,
 * navigability — to the caller.
 */
public enum Layer {
    /** Elevation in {@code [0, 1]}, one float per cell. */
    HEIGHT(0, ElementType.FLOAT),
    /** Log-normalised flow accumulation in {@code [0, 1]}: a moisture proxy. */
    FLOW(1, ElementType.FLOAT),
    /** Raw upstream catchment in cells; size channels from this. */
    FLOW_ACCUMULATION(2, ElementType.FLOAT),
    /** River mask: 1 where a river runs. */
    RIVERS(3, ElementType.MASK),
    /** Lake mask: 1 where an inland depression holds standing water. */
    LAKES(4, ElementType.MASK),
    /**
     * Ocean mask: 1 where the cell is at or below sea level <em>and</em>
     * connected to the map border. A closed basin below sea level is a
     * lake, not ocean.
     */
    OCEAN(5, ElementType.MASK),
    /** The shaded relief render. Only valid for {@link Terrain#savePng}. */
    SHADED(6, ElementType.IMAGE_ONLY);

    /** How a layer's cells are stored. */
    public enum ElementType {
        /** 32-bit float per cell. */
        FLOAT,
        /** One byte per cell, 0 or 1. */
        MASK,
        /** Not readable as data; can only be written to an image. */
        IMAGE_ONLY
    }

    private final int id;
    private final ElementType elementType;

    Layer(int id, ElementType elementType) {
        this.id = id;
        this.elementType = elementType;
    }

    /** The layer id used by the native ABI. */
    public int id() {
        return id;
    }

    /** How this layer's cells are stored. */
    public ElementType elementType() {
        return elementType;
    }
}
