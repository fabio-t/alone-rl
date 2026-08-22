package com.github.fabioticconi.tergen;

/**
 * Thrown when the native generator rejects a configuration or fails to
 * write a file. The message is the one produced by the generator, e.g.
 * {@code "sea level must be in range [0,1)"}.
 */
public class TerrainException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int status;

    TerrainException(int status, String message) {
        super(message);
        this.status = status;
    }

    /** The native status code that caused this exception. */
    public int status() {
        return status;
    }
}
