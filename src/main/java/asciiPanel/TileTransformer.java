package asciiPanel;

@FunctionalInterface
public interface TileTransformer {
    void transformTile(int x, int y, AsciiCharacterData data);
}
