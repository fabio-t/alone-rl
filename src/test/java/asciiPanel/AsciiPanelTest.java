package asciiPanel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless tests for the vendored AsciiPanel: no windows are opened, and
 * rendering goes to an in-memory image.
 */
public class AsciiPanelTest
{
    @BeforeAll
    public static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    private char charAt(final AsciiPanel panel, final int x, final int y)
    {
        final char[] out = new char[1];
        panel.withEachTile(x, y, 1, 1, (tx, ty, data) -> out[0] = data.character);
        return out[0];
    }

    private Color foregroundAt(final AsciiPanel panel, final int x, final int y)
    {
        final Color[] out = new Color[1];
        panel.withEachTile(x, y, 1, 1, (tx, ty, data) -> out[0] = data.foregroundColor);
        return out[0];
    }

    private Color backgroundAt(final AsciiPanel panel, final int x, final int y)
    {
        final Color[] out = new Color[1];
        panel.withEachTile(x, y, 1, 1, (tx, ty, data) -> out[0] = data.backgroundColor);
        return out[0];
    }

    @Test
    public void testDefaultSize()
    {
        final AsciiPanel panel = new AsciiPanel();

        assertEquals(80, panel.getWidthInCharacters());
        assertEquals(24, panel.getHeightInCharacters());
        assertEquals(AsciiFont.CP437_9x16, panel.getAsciiFont());
        assertEquals(9, panel.getCharWidth());
        assertEquals(16, panel.getCharHeight());
    }

    @Test
    public void testInvalidSizeRejected()
    {
        assertThrows(IllegalArgumentException.class, () -> new AsciiPanel(0, 24));
        assertThrows(IllegalArgumentException.class, () -> new AsciiPanel(80, 0));
        assertThrows(IllegalArgumentException.class, () -> new AsciiPanel(-1, -1));
    }

    @ParameterizedTest
    @EnumSource(AsciiFont.class)
    public void testEveryFontSheetLoads(final AsciiFont font)
    {
        final AsciiPanel panel = new AsciiPanel(10, 10, font);

        assertEquals(font.getWidth(), panel.getCharWidth());
        assertEquals(font.getHeight(), panel.getCharHeight());
        assertEquals(font.getWidth() * 10, panel.getPreferredSize().width);
        assertEquals(font.getHeight() * 10, panel.getPreferredSize().height);
    }

    @Test
    public void testWriteCharacter()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        panel.write('@', 3, 4, Color.RED, Color.BLUE);

        assertEquals('@', charAt(panel, 3, 4));
        assertEquals(Color.RED, foregroundAt(panel, 3, 4));
        assertEquals(Color.BLUE, backgroundAt(panel, 3, 4));

        // the cursor moves one to the right
        assertEquals(4, panel.getCursorX());
        assertEquals(4, panel.getCursorY());
    }

    @Test
    public void testWriteUsesDefaultsForNullColors()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        panel.write('x', 0, 0, null, null);

        assertEquals(panel.getDefaultForegroundColor(), foregroundAt(panel, 0, 0));
        assertEquals(panel.getDefaultBackgroundColor(), backgroundAt(panel, 0, 0));
    }

    @Test
    public void testWriteString()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        panel.write("abc", 2, 5);

        assertEquals('a', charAt(panel, 2, 5));
        assertEquals('b', charAt(panel, 3, 5));
        assertEquals('c', charAt(panel, 4, 5));
        assertEquals(5, panel.getCursorX());
    }

    @Test
    public void testWriteCenter()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        panel.writeCenter("ab", 1);

        assertEquals('a', charAt(panel, 4, 1));
        assertEquals('b', charAt(panel, 5, 1));

        // a full-width string starts at the left edge
        panel.writeCenter("0123456789", 2);
        assertEquals('0', charAt(panel, 0, 2));
        assertEquals('9', charAt(panel, 9, 2));
    }

    @Test
    public void testWriteValidation()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        assertThrows(IllegalArgumentException.class, () -> panel.write('x', -1, 0));
        assertThrows(IllegalArgumentException.class, () -> panel.write('x', 10, 0));
        assertThrows(IllegalArgumentException.class, () -> panel.write('x', 0, -1));
        assertThrows(IllegalArgumentException.class, () -> panel.write('x', 0, 10));
        // beyond the 256 glyphs of CP437
        assertThrows(IllegalArgumentException.class, () -> panel.write('Ā', 0, 0));
        // string spilling over the right edge
        assertThrows(IllegalArgumentException.class, () -> panel.write("toolong", 5, 0));
        assertThrows(NullPointerException.class, () -> panel.write((String) null, 0, 0));
    }

    @Test
    public void testCursorValidation()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        panel.setCursorPosition(9, 9);
        assertEquals(9, panel.getCursorX());
        assertEquals(9, panel.getCursorY());

        assertThrows(IllegalArgumentException.class, () -> panel.setCursorX(-1));
        assertThrows(IllegalArgumentException.class, () -> panel.setCursorX(10));
        assertThrows(IllegalArgumentException.class, () -> panel.setCursorY(-1));
        assertThrows(IllegalArgumentException.class, () -> panel.setCursorY(10));
    }

    @Test
    public void testClearResetsTilesButNotCursor()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        panel.write('#', 2, 2, Color.RED, Color.BLUE);
        panel.setCursorPosition(7, 8);

        panel.clear();

        assertEquals(' ', charAt(panel, 2, 2));
        assertEquals(panel.getDefaultBackgroundColor(), backgroundAt(panel, 2, 2));
        assertEquals(7, panel.getCursorX());
        assertEquals(8, panel.getCursorY());
    }

    @Test
    public void testClearSection()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        panel.clear('#', 0, 0, 10, 10);
        panel.clear('.', 2, 2, 3, 3);

        assertEquals('#', charAt(panel, 1, 1));
        assertEquals('.', charAt(panel, 2, 2));
        assertEquals('.', charAt(panel, 4, 4));
        assertEquals('#', charAt(panel, 5, 5));

        assertThrows(IllegalArgumentException.class, () -> panel.clear('x', 5, 5, 6, 1));
        assertThrows(IllegalArgumentException.class, () -> panel.clear('x', 5, 5, 1, 6));
        assertThrows(IllegalArgumentException.class, () -> panel.clear('x', 0, 0, 0, 1));
    }

    @Test
    public void testDefaultColorSettersRejectNull()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        assertThrows(NullPointerException.class, () -> panel.setDefaultForegroundColor(null));
        assertThrows(NullPointerException.class, () -> panel.setDefaultBackgroundColor(null));
    }

    @Test
    public void testWithEachTileTransforms()
    {
        final AsciiPanel panel = new AsciiPanel(10, 10);

        panel.clear('a');
        panel.withEachTile((x, y, data) -> data.character = 'b');

        assertEquals('b', charAt(panel, 0, 0));
        assertEquals('b', charAt(panel, 9, 9));
    }

    /**
     * Renders to an in-memory image and checks actual pixels: the glyph must
     * paint foreground-colored pixels over the background, in the right cell.
     */
    @Test
    public void testPaintRendersGlyphPixels()
    {
        final AsciiPanel panel = new AsciiPanel(4, 4, AsciiFont.TALRYTH_15_15);
        final int cw = panel.getCharWidth();
        final int ch = panel.getCharHeight();

        panel.setSize(panel.getPreferredSize());
        panel.clear(' ', null, Color.BLACK);
        panel.write('@', 1, 1, Color.WHITE, Color.BLACK);

        final BufferedImage image = new BufferedImage(4 * cw, 4 * ch, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = image.createGraphics();
        panel.paint(g);
        g.dispose();

        int white = 0, black = 0;
        for (int px = cw; px < 2 * cw; px++)
        {
            for (int py = ch; py < 2 * ch; py++)
            {
                final int rgb = image.getRGB(px, py) & 0xFFFFFF;
                if (rgb == 0xFFFFFF)
                    white++;
                else if (rgb == 0)
                    black++;
            }
        }

        assertTrue(white > 0, "the glyph should have foreground pixels");
        assertTrue(black > 0, "the glyph should have background pixels");
        assertEquals(cw * ch, white + black, "every pixel is either pure fore- or background");

        // an untouched cell is entirely background
        for (int px = 0; px < cw; px++)
            for (int py = 0; py < ch; py++)
                assertEquals(0, image.getRGB(px, py) & 0xFFFFFF);
    }

    /**
     * Regression test for the repaint optimisation: writing equal-but-not-same
     * Color instances must still be detected as "unchanged" (the panel used to
     * compare colors with ==, defeating the dirty-tile check whenever callers
     * allocate fresh Color objects each frame).
     */
    @Test
    public void testRepaintWithEqualColorInstances()
    {
        final AsciiPanel panel = new AsciiPanel(4, 4);
        final int w = 4 * panel.getCharWidth(), h = 4 * panel.getCharHeight();
        panel.setSize(panel.getPreferredSize());

        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = image.createGraphics();

        panel.write('@', 1, 1, new Color(10, 20, 30), new Color(40, 50, 60));
        panel.paint(g);

        // same content via fresh (equal) instances: must render identically
        panel.write('@', 1, 1, new Color(10, 20, 30), new Color(40, 50, 60));
        panel.paint(g);
        g.dispose();

        final int fg = new Color(10, 20, 30).getRGB() & 0xFFFFFF;
        int found = 0;
        for (int px = 0; px < w; px++)
            for (int py = 0; py < h; py++)
                if ((image.getRGB(px, py) & 0xFFFFFF) == fg)
                    found++;

        assertTrue(found > 0, "glyph must remain rendered after a repaint with equal colors");
    }
}
