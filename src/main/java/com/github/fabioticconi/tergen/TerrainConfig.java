package com.github.fabioticconi.tergen;

/**
 * Generation parameters, mirroring the native {@code TgConfig} struct.
 *
 * <p>Start from {@link #defaults()} and override what you need; the
 * setters chain:
 *
 * <pre>{@code
 * TerrainConfig config = TerrainConfig.defaults()
 *         .size(512, 512)
 *         .seed(42)
 *         .archipelago(5);
 * }</pre>
 *
 * <p>Several fields double as switches so the native struct stays a plain
 * value type: see the individual setters. Invalid values are not rejected
 * here — the native generator validates them and
 * {@link TerrainGenerator#generate} raises {@link TerrainException} with
 * the offending parameter named.
 */
public final class TerrainConfig {
    // package-private so TerrainGenerator can marshal them without getters
    long width = 512;
    long height = 512;
    int seed = 0;
    float seaLevel = 0.08f;

    long octaves = 8;
    double frequency = 2.5;
    double lacunarity = 2.0;
    double persistence = 0.5;

    float islandSize = 1.0f;
    float coastNoise = 1.0f;

    long archipelagoIslands = 0;
    float islandsSize = 0.35f;
    float islandsVariation = 0.35f;
    float islandsSpread = 0.85f;
    float islandsSpacing = 0.22f;

    long droplets = -1;
    float breachMaxLakeFraction = 0.0f;

    float riverDensity = 0.01f;
    float riverMinUpstreamCells = 0.0f;

    private TerrainConfig() {}

    /**
     * A configuration holding the generator's defaults: a single 512×512
     * island with erosion and rivers enabled.
     *
     * <p>The values come from the native library, so they cannot drift out
     * of step with it.
     */
    public static TerrainConfig defaults() {
        TerrainConfig config = new TerrainConfig();
        TerrainGenerator.fillDefaults(config);
        return config;
    }

    /** Map dimensions in cells. */
    public TerrainConfig size(long width, long height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /** Generation seed: the same seed and parameters give the same map. */
    public TerrainConfig seed(int seed) {
        this.seed = seed;
        return this;
    }

    /** Sea level in {@code [0, 1)}. */
    public TerrainConfig seaLevel(float seaLevel) {
        this.seaLevel = seaLevel;
        return this;
    }

    /** Fractal noise settings for the base heightmap. */
    public TerrainConfig noise(long octaves, double frequency, double lacunarity, double persistence) {
        this.octaves = octaves;
        this.frequency = frequency;
        this.lacunarity = lacunarity;
        this.persistence = persistence;
        return this;
    }

    /**
     * Single-island shaping: island diameter as a fraction of the shorter
     * map side. Also clears archipelago mode.
     */
    public TerrainConfig island(float size) {
        this.islandSize = size;
        this.archipelagoIslands = 0;
        return this;
    }

    /** Coastline perturbation strength; {@code 0} gives a round island. */
    public TerrainConfig coastNoise(float coastNoise) {
        this.coastNoise = coastNoise;
        return this;
    }

    /** No land mask at all: terrain reaches the map borders. */
    public TerrainConfig noIsland() {
        this.islandSize = 0.0f;
        this.archipelagoIslands = 0;
        return this;
    }

    /**
     * Archipelago mode: place {@code islands} islands instead of one.
     * The count is approximate — neighbours can merge.
     */
    public TerrainConfig archipelago(long islands) {
        this.archipelagoIslands = islands;
        return this;
    }

    /** Archipelago island size, variation, spread and minimum spacing. */
    public TerrainConfig archipelagoShape(float size, float variation, float spread, float spacing) {
        this.islandsSize = size;
        this.islandsVariation = variation;
        this.islandsSpread = spread;
        this.islandsSpacing = spacing;
        return this;
    }

    /** Number of erosion droplets. Negative means one per cell. */
    public TerrainConfig droplets(long droplets) {
        this.droplets = droplets;
        return this;
    }

    /** Disables hydraulic erosion. */
    public TerrainConfig noErosion() {
        this.droplets = 0;
        return this;
    }

    /**
     * Carves outlets from closed basins bigger than this fraction of the
     * map, so they drain instead of becoming inland seas. Off by default.
     */
    public TerrainConfig breachBasins(float maxLakeFraction) {
        this.breachMaxLakeFraction = maxLakeFraction;
        return this;
    }

    /** Approximate fraction of land covered by rivers. */
    public TerrainConfig riverDensity(float density) {
        this.riverDensity = density;
        this.riverMinUpstreamCells = 0.0f;
        return this;
    }

    /**
     * Absolute river threshold: a channel starts once this many cells of
     * catchment drain through. Overrides {@link #riverDensity}. Prefer this
     * when cells have a fixed real-world size, since an area share does not
     * transfer between map sizes.
     */
    public TerrainConfig riverMinUpstreamCells(float cells) {
        this.riverMinUpstreamCells = cells;
        return this;
    }

    /**
     * Skips flow routing, rivers and lakes entirely. Intended for callers
     * running their own hydrology: the heightmap is unaffected either way,
     * and the ocean mask is still computed.
     */
    public TerrainConfig noRivers() {
        this.riverDensity = 0.0f;
        this.riverMinUpstreamCells = 0.0f;
        return this;
    }
}
