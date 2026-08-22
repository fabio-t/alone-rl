package com.github.fabioticconi.tergen;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;

/**
 * Entry point for generating terrain from Java.
 *
 * <p>This binds the native library through the Foreign Function &amp;
 * Memory API (Java 22+), so there is no JNI shim to build and no
 * Java-specific code inside the Rust library — the same C ABI serves C,
 * C++, C# and anything else with an FFI.
 *
 * <h2>Loading the library</h2>
 *
 * The shared library is located, in order:
 *
 * <ol>
 *   <li>the path in the {@code tergen.library} system property, if set;</li>
 *   <li>{@code libterrain_generator_ffi.so} (or the platform equivalent)
 *       on the default library search path.</li>
 * </ol>
 *
 * Because the FFM API is a restricted operation, run with
 * {@code --enable-native-access=ALL-UNNAMED} (or the equivalent for your
 * module) to avoid a warning.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * TerrainConfig config = TerrainConfig.defaults().size(512, 512).seed(42);
 * try (Terrain terrain = TerrainGenerator.generate(config)) {
 *     float[] elevation = terrain.floats(Layer.HEIGHT);
 *     terrain.savePng(Layer.SHADED, Path.of("map.png"));
 * }
 * }</pre>
 */
public final class TerrainGenerator {

    /** ABI version this binding was written against. */
    private static final int EXPECTED_ABI_VERSION = 1;

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LOOKUP = loadLibrary();

    // ---- TgConfig layout -------------------------------------------------
    // Mirrors the #[repr(C)] struct. Padding is explicit; the total size is
    // checked against the native tg_config_size() below, so a mismatch
    // fails loudly at class-init instead of silently misreading fields.
    private static final MemoryLayout CONFIG_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("width"),
            ValueLayout.JAVA_LONG.withName("height"),
            ValueLayout.JAVA_INT.withName("seed"),
            ValueLayout.JAVA_FLOAT.withName("sea_level"),
            ValueLayout.JAVA_LONG.withName("octaves"),
            ValueLayout.JAVA_DOUBLE.withName("frequency"),
            ValueLayout.JAVA_DOUBLE.withName("lacunarity"),
            ValueLayout.JAVA_DOUBLE.withName("persistence"),
            ValueLayout.JAVA_FLOAT.withName("island_size"),
            ValueLayout.JAVA_FLOAT.withName("coast_noise"),
            ValueLayout.JAVA_LONG.withName("archipelago_islands"),
            ValueLayout.JAVA_FLOAT.withName("islands_size"),
            ValueLayout.JAVA_FLOAT.withName("islands_variation"),
            ValueLayout.JAVA_FLOAT.withName("islands_spread"),
            ValueLayout.JAVA_FLOAT.withName("islands_spacing"),
            ValueLayout.JAVA_LONG.withName("droplets"),
            ValueLayout.JAVA_FLOAT.withName("breach_max_lake_fraction"),
            ValueLayout.JAVA_FLOAT.withName("river_density"),
            ValueLayout.JAVA_FLOAT.withName("river_min_upstream_cells"),
            MemoryLayout.paddingLayout(4));

    private static VarHandle field(String name) {
        return CONFIG_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement(name));
    }

    private static final VarHandle WIDTH = field("width");
    private static final VarHandle HEIGHT = field("height");
    private static final VarHandle SEED = field("seed");
    private static final VarHandle SEA_LEVEL = field("sea_level");
    private static final VarHandle OCTAVES = field("octaves");
    private static final VarHandle FREQUENCY = field("frequency");
    private static final VarHandle LACUNARITY = field("lacunarity");
    private static final VarHandle PERSISTENCE = field("persistence");
    private static final VarHandle ISLAND_SIZE = field("island_size");
    private static final VarHandle COAST_NOISE = field("coast_noise");
    private static final VarHandle ARCHIPELAGO_ISLANDS = field("archipelago_islands");
    private static final VarHandle ISLANDS_SIZE = field("islands_size");
    private static final VarHandle ISLANDS_VARIATION = field("islands_variation");
    private static final VarHandle ISLANDS_SPREAD = field("islands_spread");
    private static final VarHandle ISLANDS_SPACING = field("islands_spacing");
    private static final VarHandle DROPLETS = field("droplets");
    private static final VarHandle BREACH = field("breach_max_lake_fraction");
    private static final VarHandle RIVER_DENSITY = field("river_density");
    private static final VarHandle RIVER_MIN_UPSTREAM = field("river_min_upstream_cells");

    // ---- native functions ------------------------------------------------
    private static final MethodHandle TG_ABI_VERSION =
            downcall("tg_abi_version", FunctionDescriptor.of(ValueLayout.JAVA_INT));
    private static final MethodHandle TG_CONFIG_SIZE =
            downcall("tg_config_size", FunctionDescriptor.of(ValueLayout.JAVA_LONG));
    private static final MethodHandle TG_CONFIG_DEFAULT =
            downcall("tg_config_default", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle TG_GENERATE = downcall(
            "tg_generate",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle TG_LAST_ERROR = downcall(
            "tg_last_error",
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));

    static final MethodHandle TG_TERRAIN_FREE =
            downcall("tg_terrain_free", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    static final MethodHandle TG_TERRAIN_WIDTH =
            downcall("tg_terrain_width", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    static final MethodHandle TG_TERRAIN_HEIGHT =
            downcall("tg_terrain_height", FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    static final MethodHandle TG_TERRAIN_SEA_LEVEL =
            downcall("tg_terrain_sea_level", FunctionDescriptor.of(ValueLayout.JAVA_FLOAT, ValueLayout.ADDRESS));
    static final MethodHandle TG_TERRAIN_LAYER_F32 = downcall(
            "tg_terrain_layer_f32",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    static final MethodHandle TG_TERRAIN_LAYER_U8 = downcall(
            "tg_terrain_layer_u8",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    static final MethodHandle TG_TERRAIN_SAVE_PNG = downcall(
            "tg_terrain_save_png",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

    static {
        int abi = (int) invoke(TG_ABI_VERSION);
        if (abi != EXPECTED_ABI_VERSION) {
            throw new IllegalStateException("terrain-generator ABI mismatch: this binding expects version "
                    + EXPECTED_ABI_VERSION + ", the native library reports " + abi
                    + ". Rebuild the native library and the Java sources from the same revision.");
        }
        long nativeSize = (long) invoke(TG_CONFIG_SIZE);
        if (nativeSize != CONFIG_LAYOUT.byteSize()) {
            throw new IllegalStateException("terrain-generator config layout mismatch: native struct is "
                    + nativeSize + " bytes, this binding models " + CONFIG_LAYOUT.byteSize()
                    + ". Rebuild the native library and the Java sources from the same revision.");
        }
    }

    private TerrainGenerator() {}

    private static SymbolLookup loadLibrary() {
        String explicit = System.getProperty("tergen.library");
        Arena arena = Arena.global();
        if (explicit != null && !explicit.isBlank()) {
            return SymbolLookup.libraryLookup(Path.of(explicit), arena);
        }
        return SymbolLookup.libraryLookup(System.mapLibraryName("terrain_generator_ffi"), arena);
    }

    private static MethodHandle downcall(String symbol, FunctionDescriptor descriptor) {
        MemorySegment address = LOOKUP.find(symbol)
                .orElseThrow(() -> new IllegalStateException(
                        "symbol '" + symbol + "' not found in the terrain-generator native library"));
        return LINKER.downcallHandle(address, descriptor);
    }

    private static Object invoke(MethodHandle handle, Object... args) {
        try {
            return handle.invokeWithArguments(args);
        } catch (Throwable t) {
            throw new IllegalStateException("native call failed", t);
        }
    }

    /** Reads the native defaults into {@code config}. */
    static void fillDefaults(TerrainConfig config) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment struct = arena.allocate(CONFIG_LAYOUT);
            check((int) invoke(TG_CONFIG_DEFAULT, struct));

            config.width = (long) WIDTH.get(struct, 0L);
            config.height = (long) HEIGHT.get(struct, 0L);
            config.seed = (int) SEED.get(struct, 0L);
            config.seaLevel = (float) SEA_LEVEL.get(struct, 0L);
            config.octaves = (long) OCTAVES.get(struct, 0L);
            config.frequency = (double) FREQUENCY.get(struct, 0L);
            config.lacunarity = (double) LACUNARITY.get(struct, 0L);
            config.persistence = (double) PERSISTENCE.get(struct, 0L);
            config.islandSize = (float) ISLAND_SIZE.get(struct, 0L);
            config.coastNoise = (float) COAST_NOISE.get(struct, 0L);
            config.archipelagoIslands = (long) ARCHIPELAGO_ISLANDS.get(struct, 0L);
            config.islandsSize = (float) ISLANDS_SIZE.get(struct, 0L);
            config.islandsVariation = (float) ISLANDS_VARIATION.get(struct, 0L);
            config.islandsSpread = (float) ISLANDS_SPREAD.get(struct, 0L);
            config.islandsSpacing = (float) ISLANDS_SPACING.get(struct, 0L);
            config.droplets = (long) DROPLETS.get(struct, 0L);
            config.breachMaxLakeFraction = (float) BREACH.get(struct, 0L);
            config.riverDensity = (float) RIVER_DENSITY.get(struct, 0L);
            config.riverMinUpstreamCells = (float) RIVER_MIN_UPSTREAM.get(struct, 0L);
        }
    }

    /**
     * Generates a terrain.
     *
     * <p>The returned {@link Terrain} owns native memory; close it (it is
     * {@link AutoCloseable}) or use try-with-resources.
     *
     * @throws TerrainException if a parameter is out of range
     */
    public static Terrain generate(TerrainConfig config) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment struct = arena.allocate(CONFIG_LAYOUT);

            WIDTH.set(struct, 0L, config.width);
            HEIGHT.set(struct, 0L, config.height);
            SEED.set(struct, 0L, config.seed);
            SEA_LEVEL.set(struct, 0L, config.seaLevel);
            OCTAVES.set(struct, 0L, config.octaves);
            FREQUENCY.set(struct, 0L, config.frequency);
            LACUNARITY.set(struct, 0L, config.lacunarity);
            PERSISTENCE.set(struct, 0L, config.persistence);
            ISLAND_SIZE.set(struct, 0L, config.islandSize);
            COAST_NOISE.set(struct, 0L, config.coastNoise);
            ARCHIPELAGO_ISLANDS.set(struct, 0L, config.archipelagoIslands);
            ISLANDS_SIZE.set(struct, 0L, config.islandsSize);
            ISLANDS_VARIATION.set(struct, 0L, config.islandsVariation);
            ISLANDS_SPREAD.set(struct, 0L, config.islandsSpread);
            ISLANDS_SPACING.set(struct, 0L, config.islandsSpacing);
            DROPLETS.set(struct, 0L, config.droplets);
            BREACH.set(struct, 0L, config.breachMaxLakeFraction);
            RIVER_DENSITY.set(struct, 0L, config.riverDensity);
            RIVER_MIN_UPSTREAM.set(struct, 0L, config.riverMinUpstreamCells);

            MemorySegment out = arena.allocate(ValueLayout.ADDRESS);
            check((int) invoke(TG_GENERATE, struct, out));

            MemorySegment handle = out.get(ValueLayout.ADDRESS, 0);
            return new Terrain(handle);
        }
    }

    /** Throws with the native error message unless {@code status} is OK. */
    static void check(int status) {
        if (status == 0) {
            return;
        }
        throw new TerrainException(status, lastError());
    }

    /** The last error message reported on this thread. */
    static String lastError() {
        try (Arena arena = Arena.ofConfined()) {
            long needed = (long) invoke(TG_LAST_ERROR, MemorySegment.NULL, 0L);
            if (needed <= 1) {
                return "unknown error";
            }
            MemorySegment buf = arena.allocate(needed);
            invoke(TG_LAST_ERROR, buf, needed);
            return buf.getString(0);
        }
    }

    static Object call(MethodHandle handle, Object... args) {
        return invoke(handle, args);
    }
}
