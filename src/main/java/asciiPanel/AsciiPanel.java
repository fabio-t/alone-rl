package asciiPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * This simulates a code page 437 ASCII terminal display.
 *
 * @author Trystan Spangler
 */
public class AsciiPanel extends JPanel {
    private static final long serialVersionUID = -4167851861147593092L;

    /** The number of glyphs in a font sheet (16x16 tiles). */
    private static final int GLYPH_COUNT = 256;

    /** Color pairs are cached as LookupOps; the cache is emptied beyond this. */
    private static final int MAX_COLOR_CACHE = 4096;

    /** The color black (pure black). */
    public static final Color black = new Color(0, 0, 0);

    /** The color red. */
    public static final Color red = new Color(128, 0, 0);

    /** The color green. */
    public static final Color green = new Color(0, 128, 0);

    /** The color yellow. */
    public static final Color yellow = new Color(128, 128, 0);

    /** The color blue. */
    public static final Color blue = new Color(0, 0, 128);

    /** The color magenta. */
    public static final Color magenta = new Color(128, 0, 128);

    /** The color cyan. */
    public static final Color cyan = new Color(0, 128, 128);

    /** The color white (light gray). */
    public static final Color white = new Color(192, 192, 192);

    /** A brighter black (dark gray). */
    public static final Color brightBlack = new Color(128, 128, 128);

    /** A brighter red. */
    public static final Color brightRed = new Color(255, 0, 0);

    /** A brighter green. */
    public static final Color brightGreen = new Color(0, 255, 0);

    /** A brighter yellow. */
    public static final Color brightYellow = new Color(255, 255, 0);

    /** A brighter blue. */
    public static final Color brightBlue = new Color(0, 0, 255);

    /** A brighter magenta. */
    public static final Color brightMagenta = new Color(255, 0, 255);

    /** A brighter cyan. */
    public static final Color brightCyan = new Color(0, 255, 255);

    /** A brighter white (pure white). */
    public static final Color brightWhite = new Color(255, 255, 255);

    private final int widthInCharacters;
    private final int heightInCharacters;
    private final char[][] chars;
    private final Color[][] backgroundColors;
    private final Color[][] foregroundColors;
    private final char[][] oldChars;
    private final Color[][] oldBackgroundColors;
    private final Color[][] oldForegroundColors;

    private final transient Map<Long, LookupOp> colorOps = new HashMap<>();

    private int charWidth;
    private int charHeight;
    private Color defaultBackgroundColor;
    private Color defaultForegroundColor;
    private int cursorX;
    private int cursorY;
    private transient BufferedImage offscreenBuffer;
    private transient Graphics offscreenGraphics;
    private transient BufferedImage[] glyphs;
    private AsciiFont asciiFont;

    /**
     * Class constructor.
     * Default size is 80x24.
     */
    public AsciiPanel() {
        this(80, 24);
    }

    /**
     * Class constructor specifying the width and height in characters.
     */
    public AsciiPanel(int width, int height) {
        this(width, height, null);
    }

    /**
     * Class constructor specifying the width and height in characters and the AsciiFont.
     *
     * @param font if null, the standard font CP437_9x16 is used
     */
    public AsciiPanel(int width, int height, AsciiFont font) {
        super();

        if (width < 1)
            throw new IllegalArgumentException("width " + width + " must be greater than 0.");

        if (height < 1)
            throw new IllegalArgumentException("height " + height + " must be greater than 0.");

        widthInCharacters = width;
        heightInCharacters = height;

        defaultBackgroundColor = black;
        defaultForegroundColor = white;

        chars = new char[widthInCharacters][heightInCharacters];
        backgroundColors = new Color[widthInCharacters][heightInCharacters];
        foregroundColors = new Color[widthInCharacters][heightInCharacters];

        oldChars = new char[widthInCharacters][heightInCharacters];
        oldBackgroundColors = new Color[widthInCharacters][heightInCharacters];
        oldForegroundColors = new Color[widthInCharacters][heightInCharacters];

        setAsciiFont(font == null ? AsciiFont.CP437_9x16 : font);
    }

    /**
     * Gets the height, in pixels, of a character.
     */
    public int getCharHeight() {
        return charHeight;
    }

    /**
     * Gets the width, in pixels, of a character.
     */
    public int getCharWidth() {
        return charWidth;
    }

    /**
     * Gets the height in characters.
     * A standard terminal is 24 characters high.
     */
    public int getHeightInCharacters() {
        return heightInCharacters;
    }

    /**
     * Gets the width in characters.
     * A standard terminal is 80 characters wide.
     */
    public int getWidthInCharacters() {
        return widthInCharacters;
    }

    /**
     * Gets the distance from the left new text will be written to.
     */
    public int getCursorX() {
        return cursorX;
    }

    /**
     * Sets the distance from the left new text will be written to.
     * This should be equal to or greater than 0 and less than the width in characters.
     */
    public void setCursorX(int cursorX) {
        if (cursorX < 0 || cursorX >= widthInCharacters)
            throw new IllegalArgumentException("cursorX " + cursorX + " must be within range [0," + widthInCharacters + ").");

        this.cursorX = cursorX;
    }

    /**
     * Gets the distance from the top new text will be written to.
     */
    public int getCursorY() {
        return cursorY;
    }

    /**
     * Sets the distance from the top new text will be written to.
     * This should be equal to or greater than 0 and less than the height in characters.
     */
    public void setCursorY(int cursorY) {
        if (cursorY < 0 || cursorY >= heightInCharacters)
            throw new IllegalArgumentException("cursorY " + cursorY + " must be within range [0," + heightInCharacters + ").");

        this.cursorY = cursorY;
    }

    /**
     * Sets the x and y position of where new text will be written to. The origin (0,0) is the upper left corner.
     */
    public void setCursorPosition(int x, int y) {
        setCursorX(x);
        setCursorY(y);
    }

    /**
     * Gets the default background color that is used when writing new text.
     */
    public Color getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    /**
     * Sets the default background color that is used when writing new text.
     */
    public void setDefaultBackgroundColor(Color defaultBackgroundColor) {
        this.defaultBackgroundColor =
            Objects.requireNonNull(defaultBackgroundColor, "defaultBackgroundColor must not be null.");
    }

    /**
     * Gets the default foreground color that is used when writing new text.
     */
    public Color getDefaultForegroundColor() {
        return defaultForegroundColor;
    }

    /**
     * Sets the default foreground color that is used when writing new text.
     */
    public void setDefaultForegroundColor(Color defaultForegroundColor) {
        this.defaultForegroundColor =
            Objects.requireNonNull(defaultForegroundColor, "defaultForegroundColor must not be null.");
    }

    /**
     * Gets the currently selected font.
     */
    public AsciiFont getAsciiFont() {
        return asciiFont;
    }

    /**
     * Sets the used font. It is advisable to make sure the parent component is properly sized after setting the font
     * as the panel dimensions will most likely change.
     */
    public void setAsciiFont(AsciiFont font) {
        if (this.asciiFont == font)
            return;

        this.asciiFont = font;

        this.charHeight = font.getHeight();
        this.charWidth = font.getWidth();

        Dimension panelSize = new Dimension(charWidth * widthInCharacters, charHeight * heightInCharacters);
        setPreferredSize(panelSize);

        offscreenBuffer = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
        offscreenGraphics = offscreenBuffer.getGraphics();

        glyphs = loadGlyphs(font);

        // the offscreen buffer is fresh, so every written tile must be redrawn
        for (int x = 0; x < widthInCharacters; x++) {
            for (int y = 0; y < heightInCharacters; y++) {
                oldChars[x][y] = 0;
                oldBackgroundColors[x][y] = null;
                oldForegroundColors[x][y] = null;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        for (int x = 0; x < widthInCharacters; x++) {
            for (int y = 0; y < heightInCharacters; y++) {
                if (oldChars[x][y] == chars[x][y]
                    && Objects.equals(oldBackgroundColors[x][y], backgroundColors[x][y])
                    && Objects.equals(oldForegroundColors[x][y], foregroundColors[x][y]))
                    continue;

                Color bg = backgroundColors[x][y];
                Color fg = foregroundColors[x][y];

                if (bg == null || fg == null)
                    continue;

                LookupOp op = colorOp(bg, fg);
                BufferedImage img = op.filter(glyphs[chars[x][y]], null);
                offscreenGraphics.drawImage(img, x * charWidth, y * charHeight, null);

                oldChars[x][y] = chars[x][y];
                oldBackgroundColors[x][y] = bg;
                oldForegroundColors[x][y] = fg;
            }
        }

        g.drawImage(offscreenBuffer, 0, 0, this);
    }

    private static BufferedImage[] loadGlyphs(AsciiFont font) {
        final URL resource = AsciiPanel.class.getClassLoader().getResource(font.getFontFilename());

        if (resource == null)
            throw new IllegalStateException("font sheet not found on the classpath: " + font.getFontFilename());

        final BufferedImage glyphSprite;
        try {
            glyphSprite = ImageIO.read(resource);
        } catch (IOException e) {
            throw new UncheckedIOException("could not read font sheet " + font.getFontFilename(), e);
        }

        final int width = font.getWidth();
        final int height = font.getHeight();

        final BufferedImage[] glyphs = new BufferedImage[GLYPH_COUNT];
        for (int i = 0; i < GLYPH_COUNT; i++) {
            int sx = (i % 16) * width;
            int sy = (i / 16) * height;

            glyphs[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = glyphs[i].getGraphics();
            g.drawImage(glyphSprite, 0, 0, width, height, sx, sy, sx + width, sy + height, null);
            g.dispose();
        }

        return glyphs;
    }

    /**
     * A {@code LookupOp} mapping a glyph's pixels to the given background and
     * foreground colors. Ops are cached per color pair: painting allocates
     * nothing for color pairs it has already seen.
     */
    private LookupOp colorOp(Color bgColor, Color fgColor) {
        if (colorOps.size() > MAX_COLOR_CACHE)
            colorOps.clear();

        final long key = ((long) fgColor.getRGB() << 32) | (bgColor.getRGB() & 0xFFFFFFFFL);

        return colorOps.computeIfAbsent(key, k -> {
            short[] a = new short[256];
            short[] r = new short[256];
            short[] g = new short[256];
            short[] b = new short[256];

            // index 0 holds the background color, every other index the foreground
            a[0] = (byte) bgColor.getAlpha();
            r[0] = (byte) bgColor.getRed();
            g[0] = (byte) bgColor.getGreen();
            b[0] = (byte) bgColor.getBlue();

            for (int i = 1; i < 256; i++) {
                a[i] = (byte) fgColor.getAlpha();
                r[i] = (byte) fgColor.getRed();
                g[i] = (byte) fgColor.getGreen();
                b[i] = (byte) fgColor.getBlue();
            }

            short[][] table = { r, g, b, a };
            return new LookupOp(new ShortLookupTable(0, table), null);
        });
    }

    private void checkCharacter(char character) {
        if (character >= GLYPH_COUNT)
            throw new IllegalArgumentException("character " + character + " must be within range [0," + GLYPH_COUNT + ").");
    }

    private void checkPosition(int x, int y) {
        if (x < 0 || x >= widthInCharacters)
            throw new IllegalArgumentException("x " + x + " must be within range [0," + widthInCharacters + ").");

        if (y < 0 || y >= heightInCharacters)
            throw new IllegalArgumentException("y " + y + " must be within range [0," + heightInCharacters + ").");
    }

    /**
     * Clear the entire screen to whatever the default background color is.
     *
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear() {
        return clear(' ', 0, 0, widthInCharacters, heightInCharacters, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Clear the entire screen with the specified character and whatever the default foreground and background colors are.
     *
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear(char character) {
        return clear(character, 0, 0, widthInCharacters, heightInCharacters, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Clear the entire screen with the specified character and the specified foreground and background colors.
     *
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear(char character, Color foreground, Color background) {
        return clear(character, 0, 0, widthInCharacters, heightInCharacters, foreground, background);
    }

    /**
     * Clear a section of the screen with the specified character and whatever the default foreground and background colors are.
     * The cursor position will not be modified.
     *
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear(char character, int x, int y, int width, int height) {
        return clear(character, x, y, width, height, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Clear a section of the screen with the specified character and the specified foreground and background colors.
     * The cursor position will not be modified.
     *
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear(char character, int x, int y, int width, int height, Color foreground, Color background) {
        checkCharacter(character);
        checkPosition(x, y);

        if (width < 1)
            throw new IllegalArgumentException("width " + width + " must be greater than 0.");

        if (height < 1)
            throw new IllegalArgumentException("height " + height + " must be greater than 0.");

        if (x + width > widthInCharacters)
            throw new IllegalArgumentException("x + width " + (x + width) + " must be less than " + (widthInCharacters + 1) + ".");

        if (y + height > heightInCharacters)
            throw new IllegalArgumentException("y + height " + (y + height) + " must be less than " + (heightInCharacters + 1) + ".");

        int originalCursorX = cursorX;
        int originalCursorY = cursorY;
        for (int xo = x; xo < x + width; xo++) {
            for (int yo = y; yo < y + height; yo++) {
                write(character, xo, yo, foreground, background);
            }
        }
        cursorX = originalCursorX;
        cursorY = originalCursorY;

        return this;
    }

    /**
     * Write a character to the cursor's position.
     * This updates the cursor's position.
     *
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character) {
        return write(character, cursorX, cursorY, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Write a character to the cursor's position with the specified foreground color.
     * This updates the cursor's position but not the default foreground color.
     *
     * @param foreground the foreground color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, Color foreground) {
        return write(character, cursorX, cursorY, foreground, defaultBackgroundColor);
    }

    /**
     * Write a character to the cursor's position with the specified foreground and background colors.
     * This updates the cursor's position but not the default colors.
     *
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, Color foreground, Color background) {
        return write(character, cursorX, cursorY, foreground, background);
    }

    /**
     * Write a character to the specified position.
     * This updates the cursor's position.
     *
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, int x, int y) {
        return write(character, x, y, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Write a character to the specified position with the specified foreground color.
     * This updates the cursor's position but not the default foreground color.
     *
     * @param foreground the foreground color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, int x, int y, Color foreground) {
        return write(character, x, y, foreground, defaultBackgroundColor);
    }

    /**
     * Write a character to the specified position with the specified foreground and background colors.
     * This updates the cursor's position but not the default colors.
     *
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, int x, int y, Color foreground, Color background) {
        checkCharacter(character);
        checkPosition(x, y);

        if (foreground == null) foreground = defaultForegroundColor;
        if (background == null) background = defaultBackgroundColor;

        chars[x][y] = character;
        foregroundColors[x][y] = foreground;
        backgroundColors[x][y] = background;
        cursorX = x + 1;
        cursorY = y;
        return this;
    }

    /**
     * Write a string to the cursor's position.
     * This updates the cursor's position.
     *
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string) {
        return write(string, cursorX, cursorY, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Write a string to the cursor's position with the specified foreground color.
     * This updates the cursor's position but not the default foreground color.
     *
     * @param foreground the foreground color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, Color foreground) {
        return write(string, cursorX, cursorY, foreground, defaultBackgroundColor);
    }

    /**
     * Write a string to the cursor's position with the specified foreground and background colors.
     * This updates the cursor's position but not the default colors.
     *
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, Color foreground, Color background) {
        return write(string, cursorX, cursorY, foreground, background);
    }

    /**
     * Write a string to the specified position.
     * This updates the cursor's position.
     *
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, int x, int y) {
        return write(string, x, y, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Write a string to the specified position with the specified foreground color.
     * This updates the cursor's position but not the default foreground color.
     *
     * @param foreground the foreground color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, int x, int y, Color foreground) {
        return write(string, x, y, foreground, defaultBackgroundColor);
    }

    /**
     * Write a string to the specified position with the specified foreground and background colors.
     * This updates the cursor's position but not the default colors.
     *
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, int x, int y, Color foreground, Color background) {
        Objects.requireNonNull(string, "string must not be null.");

        if (x + string.length() > widthInCharacters)
            throw new IllegalArgumentException("x + string.length() " + (x + string.length()) + " must be less than " + widthInCharacters + ".");

        checkPosition(x, y);

        if (foreground == null)
            foreground = defaultForegroundColor;

        if (background == null)
            background = defaultBackgroundColor;

        for (int i = 0; i < string.length(); i++) {
            write(string.charAt(i), x + i, y, foreground, background);
        }
        return this;
    }

    /**
     * Write a string to the center of the panel at the specified y position.
     * This updates the cursor's position.
     *
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel writeCenter(String string, int y) {
        return writeCenter(string, y, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Write a string to the center of the panel at the specified y position with the specified foreground color.
     * This updates the cursor's position but not the default foreground color.
     *
     * @param foreground the foreground color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel writeCenter(String string, int y, Color foreground) {
        return writeCenter(string, y, foreground, defaultBackgroundColor);
    }

    /**
     * Write a string to the center of the panel at the specified y position with the specified foreground and background colors.
     * This updates the cursor's position but not the default colors.
     *
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel writeCenter(String string, int y, Color foreground, Color background) {
        Objects.requireNonNull(string, "string must not be null.");

        if (string.length() > widthInCharacters)
            throw new IllegalArgumentException("string.length() " + string.length() + " must be less than " + widthInCharacters + ".");

        int x = (widthInCharacters - string.length()) / 2;

        return write(string, x, y, foreground, background);
    }

    public void withEachTile(TileTransformer transformer) {
        withEachTile(0, 0, widthInCharacters, heightInCharacters, transformer);
    }

    public void withEachTile(int left, int top, int width, int height, TileTransformer transformer) {
        AsciiCharacterData data = new AsciiCharacterData();

        for (int x0 = 0; x0 < width; x0++)
            for (int y0 = 0; y0 < height; y0++) {
                int x = left + x0;
                int y = top + y0;

                if (x < 0 || y < 0 || x >= widthInCharacters || y >= heightInCharacters)
                    continue;

                data.character = chars[x][y];
                data.foregroundColor = foregroundColors[x][y];
                data.backgroundColor = backgroundColors[x][y];

                transformer.transformTile(x, y, data);

                chars[x][y] = data.character;
                foregroundColors[x][y] = data.foregroundColor;
                backgroundColors[x][y] = data.backgroundColor;
            }
    }
}
