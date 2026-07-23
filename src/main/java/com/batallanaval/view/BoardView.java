package com.batallanaval.view;

import com.batallanaval.model.Board;
import com.batallanaval.model.BoardListener;
import com.batallanaval.model.Cell;
import com.batallanaval.model.Ship;
import com.batallanaval.util.Coordinate;
import com.batallanaval.util.CellState;
import com.batallanaval.util.Orientation;
import com.batallanaval.util.ShipType;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Visual representation of a {@link Board} using JavaFX 2D shapes
 * (Rectangle, Circle, Line, Polygon). It registers as a {@link BoardListener}
 * to automatically repaint the cell that changed, without the model
 * knowing anything about JavaFX (Observer).
 *
 * If this is the own (placement) board, it always shows the ships.
 * If it is the opponent's board, ships stay hidden until they are
 * hit/sunk, unless verification mode is turned on (HU-3). Own ships are
 * drawn in brown/wood tones and the opponent's in blue tones, to match the
 * start screen.
 */
public class BoardView implements BoardListener {

    private static final int CELL_SIZE = 32;
    private static final Color VALID_PREVIEW_COLOR = Color.web("#63992280");
    private static final Color INVALID_PREVIEW_COLOR = Color.web("#E24B4A80");

    private static final Color VALID_HULL = Color.web("#63992299");
    private static final Color VALID_HULL_DARK = Color.web("#3B6D1199");
    private static final Color VALID_ACCENT = Color.web("#EAF3DEE0");

    private static final Color INVALID_HULL = Color.web("#E24B4A99");
    private static final Color INVALID_HULL_DARK = Color.web("#A32D2D99");
    private static final Color INVALID_ACCENT = Color.web("#FCEBEBE0");

    private static final Color OWN_HULL = Color.web("#6B4423");
    private static final Color OWN_HULL_DARK = Color.web("#3A2412");
    private static final Color OWN_ACCENT = Color.web("#F5E6C8");

    private static final Color ENEMY_HULL = Color.web("#185FA5");
    private static final Color ENEMY_HULL_DARK = Color.web("#0C447C");
    private static final Color ENEMY_ACCENT = Color.web("#E6F1FB");

    private final Board board;
    private final boolean isOwnBoard;
    private final GridPane grid = new GridPane();
    private final StackPane[][] visualCells = new StackPane[Board.SIZE][Board.SIZE];
    private final Rectangle[][] previewOverlays = new Rectangle[Board.SIZE][Board.SIZE];
    private final List<Node> activePreviewShapes = new ArrayList<>();

    private boolean verificationMode = false;
    private Consumer<Coordinate> onCellClick;
    private Consumer<Coordinate> onCellHoverEnter;
    private Consumer<Coordinate> onCellHoverExit;

    public BoardView(Board board, boolean isOwnBoard) {
        this.board = board;
        this.isOwnBoard = isOwnBoard;
        grid.setHgap(2);
        grid.setVgap(2);
        build();
    }

    private void build() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Coordinate coordinate = new Coordinate(row, column);
                StackPane cell = createCell(coordinate);
                visualCells[row][column] = cell;
                paintCell(coordinate);
                grid.add(cell, column, row);
            }
        }
    }

    private StackPane createCell(Coordinate coordinate) {
        StackPane stack = new StackPane();
        Rectangle base = new Rectangle(CELL_SIZE, CELL_SIZE);
        base.setArcWidth(4);
        base.setArcHeight(4);
        base.setStroke(Color.web("#5f5e5a"));
        base.setStrokeWidth(0.5);
        stack.getChildren().add(base);

        Rectangle overlay = new Rectangle(CELL_SIZE, CELL_SIZE);
        overlay.setArcWidth(4);
        overlay.setArcHeight(4);
        overlay.setFill(Color.TRANSPARENT);
        overlay.setMouseTransparent(true);
        previewOverlays[coordinate.getRow()][coordinate.getColumn()] = overlay;
        stack.getChildren().add(overlay);

        CellInteractionHandler handler = new CellInteractionHandler(coordinate);
        stack.setOnMouseClicked(handler);
        stack.setOnMouseEntered(handler);
        stack.setOnMouseExited(handler);
        return stack;
    }

    /**
     * Inner class (not static: it needs the onCellClick/onCellHoverEnter/
     * onCellHoverExit fields of its BoardView) that groups handling a
     * cell's three mouse events (click, enter, exit) into a single
     * {@link EventHandler}, instead of repeating three nearly identical
     * lambdas for each of the board's 100 cells.
     */
    private class CellInteractionHandler implements EventHandler<MouseEvent> {

        private final Coordinate coordinate;

        private CellInteractionHandler(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                if (onCellClick != null) {
                    onCellClick.accept(coordinate);
                }
            } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                if (onCellHoverEnter != null) {
                    onCellHoverEnter.accept(coordinate);
                }
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                if (onCellHoverExit != null) {
                    onCellHoverExit.accept(coordinate);
                }
            }
        }
    }

    public void setOnCellClick(Consumer<Coordinate> handler) {
        this.onCellClick = handler;
    }

    /** Fires when the mouse enters a cell (to preview ship placement, HU-1). */
    public void setOnCellHoverEnter(Consumer<Coordinate> handler) {
        this.onCellHoverEnter = handler;
    }

    /** Fires when the mouse leaves a cell (to clear the preview). */
    public void setOnCellHoverExit(Consumer<Coordinate> handler) {
        this.onCellHoverExit = handler;
    }

    /**
     * Shows a translucent "ghost" of the ship about to be placed (with its
     * real shape, not a plain square) on the given cells: green if the
     * position is valid, red if it falls outside the board or overlaps
     * another ship. It does not modify the actual state of those cells,
     * it is only a preview.
     */
    public void showPlacementPreview(List<Coordinate> coordinates, ShipType type, Orientation orientation,
                                      boolean valid) {
        clearPreview();
        Color hull = valid ? VALID_HULL : INVALID_HULL;
        Color hullDark = valid ? VALID_HULL_DARK : INVALID_HULL_DARK;
        Color accent = valid ? VALID_ACCENT : INVALID_ACCENT;
        Color tint = valid ? VALID_PREVIEW_COLOR : INVALID_PREVIEW_COLOR;

        int size = coordinates.size();
        for (int index = 0; index < size; index++) {
            Coordinate coordinate = coordinates.get(index);
            if (!coordinate.isWithinBoard(Board.SIZE)) {
                continue;
            }
            previewOverlays[coordinate.getRow()][coordinate.getColumn()].setFill(tint);

            Group shape = type == ShipType.FRIGATE
                    ? createFrigateShape(hull, hullDark, accent)
                    : createSegmentShape(type, index, size, hull, hullDark, accent);
            applyOrientation(shape, orientation);
            shape.setManaged(false);
            shape.setMouseTransparent(true);
            shape.setLayoutX(CELL_SIZE / 2.0);
            shape.setLayoutY(CELL_SIZE / 2.0);

            visualCells[coordinate.getRow()][coordinate.getColumn()].getChildren().add(shape);
            activePreviewShapes.add(shape);
        }
    }

    /** Removes any active placement preview. */
    public void clearPreview() {
        for (Node shape : activePreviewShapes) {
            StackPane parent = (StackPane) shape.getParent();
            if (parent != null) {
                parent.getChildren().remove(shape);
            }
        }
        activePreviewShapes.clear();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                previewOverlays[row][column].setFill(Color.TRANSPARENT);
            }
        }
    }

    public void setVerificationMode(boolean verificationMode) {
        this.verificationMode = verificationMode;
        redrawAll();
    }

    public GridPane getNode() {
        return grid;
    }

    public void redrawAll() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                paintCell(new Coordinate(row, column));
            }
        }
    }

    @Override
    public void onCellChanged(Coordinate coordinate, CellState newState) {
        paintCell(coordinate);
    }

    private void paintCell(Coordinate coordinate) {
        Cell cell = board.getCell(coordinate);
        StackPane stack = visualCells[coordinate.getRow()][coordinate.getColumn()];

        // deja el rectangulo base (indice 0) y el overlay de previsualizacion
        // (indice 1) intactos, y quita solo las marcas de estado anteriores.
        while (stack.getChildren().size() > 2) {
            stack.getChildren().remove(2);
        }

        Rectangle base = (Rectangle) stack.getChildren().get(0);
        boolean showShip = isOwnBoard || verificationMode;

        switch (cell.getState()) {
            case EMPTY:
                base.setFill(Color.web("#B5D4F4"));
                break;
            case SHIP:
                base.setFill(showShip ? Color.WHITE : Color.web("#B5D4F4"));
                if (showShip) {
                    Node detail = createShipDetail(cell);
                    // no administrado: el casco se extiende un poco mas alla
                    // de los 32px de la casilla para "morder" el espacio de
                    // 2px hacia la casilla vecina y verse como una sola pieza
                    // continua; si quedara administrado, StackPane agrandaria
                    // la casilla entera para ajustarse a esa figura mas ancha.
                    detail.setManaged(false);
                    detail.setLayoutX(CELL_SIZE / 2.0);
                    detail.setLayoutY(CELL_SIZE / 2.0);
                    stack.getChildren().add(detail);
                }
                break;
            case WATER:
                base.setFill(Color.web("#378ADD"));
                stack.getChildren().add(createWaterMark());
                break;
            case HIT:
                base.setFill(Color.web("#F2A623"));
                stack.getChildren().add(createHitMark());
                break;
            case SUNK:
                base.setFill(Color.web("#791F1F"));
                stack.getChildren().add(createSunkMark());
                break;
            default:
                break;
        }
    }

    /**
     * Draws the silhouette of the ship this cell belongs to, with 2D
     * shapes (no images): each ship type has a distinct shape (frigate,
     * destroyer, submarine, aircraft carrier), colored in brown tones if
     * it is the own fleet or blue tones if it is the enemy's. The
     * silhouette is always drawn "pointing right" and then rotated
     * according to the ship's actual orientation (HU-1: up/down/left/right).
     */
    private Node createShipDetail(Cell cell) {
        Ship ship = cell.getShip();
        Color hull = isOwnBoard ? OWN_HULL : ENEMY_HULL;
        Color hullDark = isOwnBoard ? OWN_HULL_DARK : ENEMY_HULL_DARK;
        Color accent = isOwnBoard ? OWN_ACCENT : ENEMY_ACCENT;

        List<Coordinate> positions = ship.getPositions();
        int index = positions.indexOf(cell.getCoordinate());
        int size = positions.size();

        Group shape = ship.getType() == ShipType.FRIGATE
                ? createFrigateShape(hull, hullDark, accent)
                : createSegmentShape(ship.getType(), index, size, hull, hullDark, accent);

        applyOrientation(shape, ship.getOrientation());
        return shape;
    }

    /**
     * Orients the silhouette (drawn pointing left, with the mast above the
     * hull) according to the ship's actual direction.
     *
     * DOWN/UP are resolved with a 90/270 degree rotation: the ship ends up
     * "lying on its side," with the mast pointing sideways instead of up,
     * but it is still a clean rotation.
     *
     * LEFT would need a 180 degree rotation, but that flips the ship
     * upside down in addition to mirroring it side to side (the mast
     * would end up pointing down, below the hull: "upside down"). Instead
     * of rotating, it is mirrored horizontally (the X axis is flipped):
     * the bow points the other way without touching the vertical axis, so
     * the mast always stays above the hull.
     */
    private void applyOrientation(Group shape, Orientation orientation) {
        switch (orientation) {
            case RIGHT:
                break;
            case LEFT:
                shape.getTransforms().add(new Scale(-1, 1, 0, 0));
                break;
            case DOWN:
                shape.getTransforms().add(new Rotate(90, 0, 0));
                break;
            case UP:
                shape.getTransforms().add(new Rotate(270, 0, 0));
                break;
            default:
                break;
        }
    }

    /** The frigate occupies a single cell: a boat with a mast and sail. */
    private Group createFrigateShape(Color hull, Color hullDark, Color accent) {
        Polygon hullShape = new Polygon(-9.0, 5.0, 9.0, 5.0, 6.0, 13.0, -6.0, 13.0);
        hullShape.setFill(hull);

        Rectangle waterline = new Rectangle(-7, 10, 14, 3);
        waterline.setFill(hullDark);

        Line mast = new Line(0, 5, 0, -11);
        mast.setStroke(accent);
        mast.setStrokeWidth(2);

        Polygon sail = new Polygon(0.0, -9.0, 0.0, 4.0, 9.0, 0.0);
        sail.setFill(hull);

        Polygon flag = new Polygon(0.0, -11.0, 0.0, -6.0, 6.0, -9.0);
        flag.setFill(accent);

        Circle porthole = new Circle(-3, 8, 1.6);
        porthole.setFill(accent);

        return new Group(hullShape, waterline, mast, sail, flag, porthole);
    }

    /**
     * How far the hull extends past its own cell's edge (16px) on the
     * side where it connects to the next cell of the same ship. The gap
     * between cells is 2px; by extending 2px further (reaching right up
     * to the neighboring cell's edge) both cells' hulls touch without
     * leaving a blank gap between them.
     */
    private static final double HULL_BRIDGE = 18.0;
    private static final double HULL_EDGE = 14.0;

    /**
     * The other types occupy several cells: the first one (bow) is a
     * pointed hull, the last one (stern) has the ship type's own
     * structure, and the ones in the middle carry a minor detail. The
     * side that connects to the next cell of the ship extends out (see
     * {@link #HULL_BRIDGE}) so the hull looks like a single piece instead
     * of cut off by the gap between cells.
     */
    private Group createSegmentShape(ShipType type, int index, int size, Color hull, Color hullDark, Color accent) {
        Group group = new Group();
        boolean isBow = index == 0;
        boolean isTail = index == size - 1;

        if (isBow) {
            double rightEdge = isTail ? HULL_EDGE : HULL_BRIDGE;
            Polygon bow = new Polygon(-HULL_EDGE, 0.0, 6.0, -9.0, rightEdge, -9.0, rightEdge, 9.0, 6.0, 9.0);
            bow.setFill(hull);
            Rectangle bowWaterline = new Rectangle(-6, 6, rightEdge + 6, 3);
            bowWaterline.setFill(hullDark);
            group.getChildren().addAll(bow, bowWaterline);
            return group;
        }

        double leftEdge = -HULL_BRIDGE;
        double rightEdge = isTail ? HULL_EDGE : HULL_BRIDGE;
        Rectangle segment = new Rectangle(leftEdge, -9, rightEdge - leftEdge, 18);
        segment.setArcWidth(isTail ? 6 : 0);
        segment.setArcHeight(isTail ? 6 : 0);
        segment.setFill(hull);
        Rectangle waterline = new Rectangle(leftEdge, 6, rightEdge - leftEdge, 3);
        waterline.setFill(hullDark);
        group.getChildren().addAll(segment, waterline);

        if (isTail) {
            group.getChildren().add(createSternStructure(type, accent));
        } else {
            group.getChildren().add(createMiddleDetail(type, accent));
        }
        return group;
    }

    /**
     * The cell is 32px on each side (16px from the center to each edge),
     * so every shape must stay within that range or it will spill out of
     * its cell and ruin the drawing. The hull already occupies up to
     * y=-9; these structures only use the free space between y=-9 and the
     * y=-16 edge.
     */
    private Node createSternStructure(ShipType type, Color accent) {
        switch (type) {
            case DESTROYER: {
                Rectangle bridge = new Rectangle(-6, -15, 12, 6);
                bridge.setFill(accent);
                Polygon flag = new Polygon(4.0, -15.0, 4.0, -11.0, 8.0, -13.0);
                flag.setFill(accent);
                return new Group(bridge, flag);
            }
            case SUBMARINE: {
                Rectangle tower = new Rectangle(-7, -16, 14, 7);
                tower.setFill(accent);
                Circle periscopeHead = new Circle(4, -15, 1);
                periscopeHead.setFill(accent);
                return new Group(tower, periscopeHead);
            }
            case AIRCRAFT_CARRIER: {
                Rectangle island = new Rectangle(-6, -15, 12, 6);
                island.setFill(accent);
                Circle antenna1 = new Circle(-3, -15, 1);
                Circle antenna2 = new Circle(3, -15, 1);
                antenna1.setFill(accent);
                antenna2.setFill(accent);
                return new Group(island, antenna1, antenna2);
            }
            default:
                return new Group();
        }
    }

    private Node createMiddleDetail(ShipType type, Color accent) {
        if (type == ShipType.AIRCRAFT_CARRIER) {
            Polygon plane = new Polygon(-5.0, 3.0, 5.0, 3.0, 0.0, -5.0);
            plane.setFill(accent);
            return plane;
        }
        Circle porthole = new Circle(0, 0, 2.2);
        porthole.setFill(accent);
        return porthole;
    }

    private Group createWaterMark() {
        Line l1 = new Line(-6, -6, 6, 6);
        Line l2 = new Line(-6, 6, 6, -6);
        l1.setStroke(Color.WHITE);
        l2.setStroke(Color.WHITE);
        l1.setStrokeWidth(2);
        l2.setStrokeWidth(2);
        return new Group(l1, l2);
    }

    private Circle createHitMark() {
        Circle circle = new Circle(6);
        circle.setFill(Color.web("#4A1B0C"));
        return circle;
    }

    private Group createSunkMark() {
        Line l1 = new Line(-8, -8, 8, 8);
        Line l2 = new Line(-8, 8, 8, -8);
        l1.setStroke(Color.WHITE);
        l2.setStroke(Color.WHITE);
        l1.setStrokeWidth(3);
        l2.setStrokeWidth(3);
        return new Group(l1, l2);
    }
}
