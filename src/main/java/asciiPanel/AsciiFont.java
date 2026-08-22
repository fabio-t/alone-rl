package asciiPanel;

/**
 * This class provides all available Fonts for the AsciiPanel.
 * Some graphics are from the Dwarf Fortress Tileset Wiki Page.
 *
 * @author zn80
 */
public enum AsciiFont {

    CP437_8x8("cp437_8x8.png", 8, 8),
    CP437_10x10("cp437_10x10.png", 10, 10),
    CP437_12x12("cp437_12x12.png", 12, 12),
    CP437_16x16("cp437_16x16.png", 16, 16),
    CP437_9x16("cp437_9x16.png", 9, 16),
    DRAKE_10x10("drake_10x10.png", 10, 10),
    TAFFER_10x10("taffer_10x10.png", 10, 10),
    QBICFEET_10x10("qbicfeet_10x10.png", 10, 10),
    TALRYTH_15_15("talryth_square_15x15.png", 15, 15);

    private final String fontFilename;
    private final int width;
    private final int height;

    AsciiFont(String filename, int width, int height) {
        this.fontFilename = filename;
        this.width = width;
        this.height = height;
    }

    public String getFontFilename() {
        return fontFilename;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
