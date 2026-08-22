# AsciiPanel (vendored)

This package (and the `*.png` font sheets in `src/main/resources/`) comes
from [trystan/AsciiPanel](https://github.com/trystan/AsciiPanel) at commit
`372dfbae98`, under the **MIT License** (see `LICENSE.md` here).

It is deliberately copied into the tree rather than referenced as a
dependency: upstream is small, long dormant and not on Maven Central, and
having the sources here means they can be modified freely if the game
needs it. Local changes, if any, should be noted below.

## Local changes

Modernized and fixed relative to upstream `372dfbae98`; behavior-compatible
public API. Tested by `src/test/java/asciiPanel/AsciiPanelTest.java`
(headless, including pixel-level rendering checks).

- The dirty-tile repaint check compared `Color`s with `==`, so callers
  allocating fresh `Color` instances each frame (as this game does)
  redrew every tile on every repaint; it now uses `equals`.
- Painting allocated a fresh 256-entry lookup table, `LookupOp` and
  filtered image per changed tile per frame; ops are now cached per
  fore/background color pair.
- Legacy AWT `paint`/`update` overrides replaced with Swing's
  `paintComponent`.
- A missing or unreadable font sheet now fails fast with a clear error
  instead of printing to stderr and NPE-ing later.
- `write(char)` had an off-by-one glyph bound (`>` instead of `>=`).
- `setAsciiFont` reset the offscreen buffer but not the dirty-tile state,
  so a font change could leave stale cells undrawn.
- Color constants and fields made `final` where possible; the duplicated
  argument validation across the many `write`/`clear` overloads is
  centralised; `TileTransformer` is a `@FunctionalInterface`.
