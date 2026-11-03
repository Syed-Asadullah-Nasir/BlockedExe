package logic;

import model.BlockPiece;
import model.CellPosition;
import util.Constants;

import java.util.List;
import java.util.Random;

public class PieceGenerator {

    private final Random random;
    private final List<List<CellPosition>> shapes;

    public PieceGenerator() {
        random = new Random();
        shapes = ShapeLibrary.getAllShapes();
    }

    // Returns a new tray of random pieces. Slots are never shared between trays.
    public BlockPiece[] generateTray() {
        BlockPiece[] tray = new BlockPiece[Constants.TRAY_SIZE];
        for (int slot = 0; slot < tray.length; slot++) {
            tray[slot] = generatePiece();
        }
        return tray;
    }

    // Picks a random shape and a random colour. The BlockPiece constructor copies
    // the cell list, so every piece is its own object.
    public BlockPiece generatePiece() {
        List<CellPosition> shape = shapes.get(random.nextInt(shapes.size()));
        int colorIndex = 1 + random.nextInt(Constants.COLOR_COUNT);
        return new BlockPiece(shape, colorIndex);
    }
}
