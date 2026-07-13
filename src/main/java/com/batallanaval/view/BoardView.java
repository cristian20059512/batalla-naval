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
 * Representacion visual de un {@link Board} usando figuras 2D de JavaFX
 * (Rectangle, Circle, Line, Polygon). Se registra como {@link BoardListener}
 * para repintar automaticamente la celda que cambio, sin que el modelo
 * conozca nada de JavaFX (Observer).
 *
 * Si es el tablero propio (de posicion) siempre muestra los barcos.
 * Si es el tablero del oponente, los barcos permanecen ocultos hasta que
 * son tocados/hundidos, salvo que se active el modo de verificacion (HU-3).
 * Los barcos propios se dibujan en tonos cafe/madera y los del oponente en
 * tonos azules, para hacer juego con la pantalla inicial.
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
     * Clase interna (no estatica: necesita los campos onCellClick/
     * onCellHoverEnter/onCellHoverExit de su BoardView) que agrupa el manejo
     * de los tres eventos de mouse de una celda (click, entra, sale) en un
     * unico {@link EventHandler}, en vez de repetir tres lambdas casi
     * identicas por cada una de las 100 celdas del tablero.
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

    /** Se dispara cuando el mouse entra en una celda (para previsualizar la colocacion de un barco, HU-1). */
    public void setOnCellHoverEnter(Consumer<Coordinate> handler) {
        this.onCellHoverEnter = handler;
    }

    /** Se dispara cuando el mouse sale de una celda (para limpiar la previsualizacion). */
    public void setOnCellHoverExit(Consumer<Coordinate> handler) {
        this.onCellHoverExit = handler;
    }

    /**
     * Muestra un "fantasma" translucido del barco que se va a colocar (con
     * su forma real, no un cuadro liso) en las casillas indicadas: verde si
     * la posicion es valida, rojo si queda fuera del tablero o se superpone
     * con otro barco. No modifica el estado real de esas celdas, es solo
     * una previsualizacion.
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

    /** Quita cualquier previsualizacion de colocacion activa. */
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
     * Dibuja la silueta del barco al que pertenece esta casilla, con
     * figuras 2D (sin imagenes): cada tipo de barco tiene una forma
     * distinta (fragata, destructor, submarino, portaaviones), coloreada
     * en tonos cafe si es la flota propia o azules si es la enemiga. La
     * silueta se dibuja siempre "apuntando a la derecha" y despues se rota
     * segun la orientacion real del barco (HU-1: arriba/abajo/izq/der).
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
     * Orienta la silueta (dibujada apuntando a la izquierda, con el mastil
     * arriba del casco) segun la direccion real del barco.
     *
     * DOWN/UP se resuelven con una rotacion de 90/270 grados: el barco
     * queda "acostado de lado", con el mastil apuntando hacia un costado
     * en vez de hacia arriba, pero sigue siendo una rotacion limpia.
     *
     * LEFT necesitaria una rotacion de 180 grados, pero eso voltea el
     * barco de arriba a abajo ademas de invertirlo de lado a lado (el
     * mastil terminaria apuntando hacia abajo, debajo del casco: "patas
     * arriba"). En vez de rotar, se refleja horizontalmente (se invierte
     * el eje X): la proa apunta al otro lado sin tocar el eje vertical,
     * asi el mastil se queda siempre arriba del casco.
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

    /** La fragata ocupa una sola casilla: un bote con mastil y vela. */
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
     * Cuanto se extiende el casco mas alla del borde de su propia casilla
     * (16px) hacia el lado donde conecta con la siguiente casilla del mismo
     * barco. El espacio entre casillas es de 2px; extendiendo 2px de mas
     * (hasta llegar justo al borde de la casilla vecina) el casco de ambas
     * casillas se toca sin dejar espacio en blanco entre ellas.
     */
    private static final double HULL_BRIDGE = 18.0;
    private static final double HULL_EDGE = 14.0;

    /**
     * Los demas tipos ocupan varias casillas: la primera (proa) es un
     * casco en punta, la ultima (popa) tiene la estructura propia del
     * tipo de barco, y las de en medio llevan un detalle menor. El lado
     * que conecta con la siguiente casilla del barco se extiende
     * (ver {@link #HULL_BRIDGE}) para que el casco se vea de una sola
     * pieza en vez de cortado por el espacio entre casillas.
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
     * La casilla tiene 32px de lado (16px de cada lado del centro), asi
     * que toda figura debe quedar dentro de ese rango o se sale de su
     * casilla y arruina el dibujo. El casco ya ocupa hasta y=-9; estas
     * estructuras usan solo el espacio libre entre y=-9 y el borde y=-16.
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
