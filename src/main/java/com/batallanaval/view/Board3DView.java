package com.batallanaval.view;

import com.batallanaval.model.Board;
import com.batallanaval.model.BoardListener;
import com.batallanaval.model.Cell;
import com.batallanaval.model.Ship;
import com.batallanaval.util.CellState;
import com.batallanaval.util.Coordinate;
import com.batallanaval.util.Orientation;
import com.batallanaval.util.ShipType;

import javafx.event.EventHandler;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Representacion visual 3D de un {@link Board} (reemplaza a la version 2D,
 * {@link BoardView}, que se deja en el proyecto sin usar). Cada celda es
 * una placa (Box) sobre el plano XZ; los barcos son grupos de Box,
 * Cylinder y Sphere posicionados y rotados en el espacio; una
 * PerspectiveCamera y un par de luces (AmbientLight + PointLight) permiten
 * verlos, siguiendo el mismo enfoque del capitulo "Understanding 3D
 * Shapes" de Learn JavaFX 17. Todo vive dentro de un SubScene, que es un
 * Node como cualquier otro y por eso se puede insertar en el layout 2D del
 * resto de la aplicacion (FXML) sin cambiar nada mas alli.
 *
 * Se registra como {@link BoardListener} igual que la version 2D, para
 * repintar automaticamente la celda que cambio sin que el modelo conozca
 * nada de JavaFX (Observer).
 *
 * Si es el tablero propio (de posicion) siempre muestra los barcos. Si es
 * el tablero del oponente, los barcos permanecen ocultos hasta que son
 * tocados/hundidos, salvo que se active el modo de verificacion (HU-3).
 */
public class Board3DView implements BoardListener {

    private static final double CELL_SIZE = 55.0;
    private static final double TILE_HEIGHT = 6.0;
    private static final double TILE_TOP_Y = -(TILE_HEIGHT / 2.0);
    private static final double BOARD_SPAN = CELL_SIZE * Board.SIZE;

    private static final double SCENE_WIDTH = 600.0;
    private static final double SCENE_HEIGHT = 520.0;

    // Gris (no blanco/celeste casi blanco): con las piezas claras de los
    // barcos (puentes, banderas, cubierta de vuelo) encima, un tablero casi
    // blanco los dejaba invisibles por falta de contraste.
    private static final Color EMPTY_COLOR = Color.web("#8B95A1");
    private static final Color SHIP_VISIBLE_COLOR = Color.web("#C7C2B8");
    private static final Color WATER_COLOR = Color.web("#378ADD");
    private static final Color HIT_COLOR = Color.web("#F2A623");
    private static final Color SUNK_COLOR = Color.web("#791F1F");

    private static final Color VALID_TINT = Color.web("#639922");
    private static final Color INVALID_TINT = Color.web("#E24B4A");

    // Equipo propio: casco gris oscuro (pedido explicito: "lo que esta azul
    // dejalo gris oscuro"), con la bandera y solo la bandera en azul, y las
    // piezas blancas (puente, mastil, etc.) en blanco puro.
    private static final Color OWN_HULL = Color.web("#4B5158");
    private static final Color OWN_HULL_DARK = Color.web("#2A2E33");
    private static final Color OWN_ACCENT = Color.web("#FFFFFF");
    private static final Color OWN_FLAG = Color.web("#2E6FD9");

    // Equipo enemigo: mismo tratamiento que el propio (casco gris oscuro,
    // piezas blancas en blanco puro), pero con la bandera en rojo para
    // distinguir de un vistazo a que equipo pertenece cada barco.
    private static final Color ENEMY_HULL = Color.web("#4B5158");
    private static final Color ENEMY_HULL_DARK = Color.web("#2A2E33");
    private static final Color ENEMY_ACCENT = Color.web("#FFFFFF");
    private static final Color ENEMY_FLAG = Color.web("#D9382E");

    // Gris oscuro de pista de vuelo del portaaviones: neutro, no cambia por
    // equipo (como en un portaaviones real). Tiene que ser bien mas oscuro
    // que el agua clara y que los tonos de acento casi blancos, o se
    // confunden entre si y la pista/isla dejan de distinguirse.
    private static final Color FLIGHT_DECK_COLOR = Color.web("#5B6672");
    private static final Color FLIGHT_DECK_STRIPE = Color.web("#F2C230");
    private static final Color PLANE_COLOR = Color.web("#3D434B");
    private static final Color ISLAND_COLOR = Color.web("#2E343B");

    // Oceano low poly: mosaico de placas (mismo Box que las casillas, no una
    // malla nueva) en tonos azules/turquesa, cada una con un tono y una
    // altura ligeramente distintos al azar para que se vea facetado y
    // "picado" en vez de una superficie lisa de un solo color (pedido
    // explicito: "oceano facetado en tonos azules y turquesas").
    private static final Color[] OCEAN_PALETTE = {
            Color.web("#1E6FA8"), Color.web("#17A398"), Color.web("#2CA6A4"),
            Color.web("#3C8DAD"), Color.web("#0F5E86"), Color.web("#1B98A0"),
            Color.web("#2389B0"),
    };
    private static final double OCEAN_TILE_SIZE = 90.0;
    private static final double OCEAN_MARGIN = 260.0;
    private static final double OCEAN_BASE_Y = 12.0;
    private static final double OCEAN_JITTER_Y = 6.0;
    private static final double OCEAN_THICKNESS = 14.0;

    // Cielo low poly: nubes "bloquey" (racimos de cajas) repartidas en un
    // anillo alrededor del tablero, bien arriba, mas un degrade de fondo en
    // el propio SubScene (ver el constructor) en vez de dejarlo transparente.
    private static final Color CLOUD_COLOR = Color.web("#F5F7FA");
    private static final Color CLOUD_SHADOW = Color.web("#D8DEE6");
    private static final double SKY_HEIGHT = -110.0;
    private static final double SKY_RING_RADIUS = BOARD_SPAN * 0.95;

    private final Random ambientRandom = new Random(20260720L);

    /**
     * Cuanto se extiende el casco mas alla del borde de su propia casilla
     * hacia el lado donde conecta con la siguiente casilla del mismo
     * barco, para que el casco se vea de una sola pieza en vez de cortado
     * por el pequeno espacio entre placas (misma idea que en la version
     * 2D, adaptada de Polygon/Rectangle a Box). Calibrado originalmente
     * para CELL_SIZE=40 (22 y 16); se escala aqui por CELL_SIZE/40 para
     * que siga cerrando el espacio entre segmentos si vuelve a cambiar el
     * tamano de celda, en vez de quedar fijo para un tamano que ya no es
     * el actual (eso fue justo lo que dejo un hueco visible entre los dos
     * segmentos del destructor al agrandar el tablero a CELL_SIZE=55).
     */
    private static final double HULL_BRIDGE = 22.0 * (CELL_SIZE / 40.0);
    private static final double HULL_EDGE = 16.0 * (CELL_SIZE / 40.0);

    private final Board board;
    private final boolean isOwnBoard;

    private final Box[][] tiles = new Box[Board.SIZE][Board.SIZE];
    private final Node[][] decorations = new Node[Board.SIZE][Board.SIZE];
    private final List<Node> activePreviewShapes = new ArrayList<>();
    private final List<Coordinate> previewedTiles = new ArrayList<>();

    private final Group detailsLayer = new Group();
    private final SubScene subScene;
    private PerspectiveCamera camera;

    // Distancia de la camara al centro del tablero (radio de la orbita),
    // derivada de BOARD_SPAN (no un numero suelto): asi la camara escala
    // sola si CELL_SIZE cambia. El maximo (2.0, no 1.4125) deja margen de
    // sobra para la ESQUINA del tablero, no solo para el borde plano: una
    // esquina esta a mitad_del_lado*raiz(2) del centro, mas lejos que el
    // punto medio de un borde, asi que en angulos diagonales (45, 135,
    // etc.) necesita mas espacio en el encuadre que en los angulos "de
    // lado" (0, 90, 180) contra los que se habia probado antes. Verificado
    // con capturas fuera de pantalla en 0, 45 y 135 grados. Ahora es un
    // rango (no un numero fijo) porque la rueda del mouse acerca/aleja la
    // camara dentro de estos limites (ver enableDragToOrbit).
    private static final double ORBIT_RADIUS_MIN = BOARD_SPAN * 0.85;
    private static final double ORBIT_RADIUS_MAX = BOARD_SPAN * 2.0;
    /**
     * Limites de inclinacion (en grados). No se deja llegar a 0 (vista
     * totalmente horizontal, rasante) ni a 90 (vista totalmente cenital) y
     * mucho menos pasar de ahi, porque pasado el cenit la camara terminaria
     * mirando la cara de ABAJO del tablero (justo lo que el usuario pidio
     * evitar: "sin poder pasarme para la otra cara").
     */
    private static final double ELEVATION_MIN_DEG = 15.0;
    private static final double ELEVATION_MAX_DEG = 82.0;
    /** Cuantos grados gira/inclina la vista por cada pixel arrastrado. */
    private static final double DRAG_SENSITIVITY = 0.4;
    /** Cuanto se acerca/aleja la camara por cada "click" de la rueda del mouse. */
    private static final double ZOOM_SENSITIVITY = 0.6;

    private double azimuthDeg = 0.0;
    private double elevationDeg = 45.0;
    private double orbitRadius = ORBIT_RADIUS_MAX;
    private double lastDragSceneX;
    private double lastDragSceneY;

    private boolean verificationMode = false;
    private Consumer<Coordinate> onCellClick;
    private Consumer<Coordinate> onCellHoverEnter;
    private Consumer<Coordinate> onCellHoverExit;

    public Board3DView(Board board, boolean isOwnBoard) {
        this.board = board;
        this.isOwnBoard = isOwnBoard;

        Group tilesLayer = new Group();
        build(tilesLayer);

        Group world = new Group(createOcean(), createSky(), tilesLayer, detailsLayer, createLights());

        subScene = new SubScene(world, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED);
        // Un LinearGradient aqui se probo y JavaFX lo renderiza como blanco
        // solido (el color de fondo de un SubScene solo admite un color
        // plano, no un degrade real, a diferencia del fill de un Node 2D
        // comun) asi que el cielo queda en un azul palido solido.
        subScene.setFill(Color.web("#BEE3F5"));
        subScene.setCamera(createCamera());
        enableDragToOrbit();
    }

    /**
     * Placas del oceano: mismo Box low poly que las casillas del tablero,
     * en un mosaico mas grande que lo rodea (incluyendo debajo del propio
     * tablero, visible por los pequenos espacios entre placas). Cada placa
     * tiene un tono al azar de {@link #OCEAN_PALETTE} y una altura un poco
     * distinta a sus vecinas (ver {@link #OCEAN_JITTER_Y}), asi las caras
     * laterales entre placas de distinta altura quedan a la vista como
     * "acantilados" facetados en vez de una superficie plana de un solo
     * color.
     */
    private Group createOcean() {
        Group ocean = new Group();
        double min = -OCEAN_MARGIN;
        double max = BOARD_SPAN + OCEAN_MARGIN;
        for (double x = min; x < max; x += OCEAN_TILE_SIZE) {
            for (double z = min; z < max; z += OCEAN_TILE_SIZE) {
                Box tile = new Box(OCEAN_TILE_SIZE - 2, OCEAN_THICKNESS, OCEAN_TILE_SIZE - 2);
                tile.setTranslateX(x + OCEAN_TILE_SIZE / 2.0);
                tile.setTranslateZ(z + OCEAN_TILE_SIZE / 2.0);
                tile.setTranslateY(OCEAN_BASE_Y + (ambientRandom.nextDouble() - 0.5) * 2 * OCEAN_JITTER_Y);
                Color color = OCEAN_PALETTE[ambientRandom.nextInt(OCEAN_PALETTE.length)];
                tile.setMaterial(new PhongMaterial(color));
                ocean.getChildren().add(tile);
            }
        }
        return ocean;
    }

    /**
     * Nubes low poly "bloquey": racimos de 3 cajas de distinto tamano,
     * repartidos en un anillo alrededor del tablero y bien arriba (ver
     * {@link #SKY_HEIGHT}), para que se asomen en el fondo de la escena sin
     * importar el angulo de la camara. El degrade del fondo del SubScene
     * (ver el constructor) hace de cielo detras de ellas.
     */
    private Group createSky() {
        Group sky = new Group();
        int cloudCount = 8;
        double centerX = BOARD_SPAN / 2.0;
        double centerZ = BOARD_SPAN / 2.0;
        for (int i = 0; i < cloudCount; i++) {
            double angle = (2 * Math.PI / cloudCount) * i;
            double radius = SKY_RING_RADIUS + ambientRandom.nextDouble() * 100.0;
            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);
            double y = SKY_HEIGHT - ambientRandom.nextDouble() * 60.0;
            sky.getChildren().add(createCloudCluster(x, y, z));
        }
        return sky;
    }

    private Group createCloudCluster(double x, double y, double z) {
        Group cluster = new Group();
        Box core = new Box(90, 34, 60);
        core.setMaterial(new PhongMaterial(CLOUD_COLOR));
        Box left = new Box(50, 26, 46);
        left.setTranslateX(-55);
        left.setTranslateY(6);
        left.setMaterial(new PhongMaterial(CLOUD_SHADOW));
        Box right = new Box(55, 24, 40);
        right.setTranslateX(52);
        right.setTranslateY(4);
        right.setMaterial(new PhongMaterial(CLOUD_COLOR));
        cluster.getChildren().addAll(core, left, right);
        cluster.setTranslateX(x);
        cluster.setTranslateY(y);
        cluster.setTranslateZ(z);
        return cluster;
    }

    /**
     * Camara en perspectiva inclinada hacia abajo, orbitando siempre a la
     * misma distancia y elevacion alrededor del centro del tablero (ver
     * {@link #updateCameraPosition()}); solo el angulo horizontal
     * (azimuth) cambia, con el arrastre del mouse.
     */
    private PerspectiveCamera createCamera() {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(1.0);
        camera.setFarClip(1500.0);
        camera.setFieldOfView(45);
        updateCameraPosition();
        return camera;
    }

    /**
     * Recalcula la posicion y orientacion de la camara a partir de
     * coordenadas esfericas (radio, elevacion, azimuth) centradas en el
     * tablero, en vez de guardar traslacion/rotacion sueltas: asi
     * "orbitar" (cambiar solo el azimuth) no puede desalinear la camara
     * del centro, sin importar cuanto se arrastre.
     *
     * El orden de la lista de transforms importa y se verifico
     * renderizando capturas fuera de pantalla en varios angulos: la
     * rotacion de azimuth (Y) va primero en la lista y la de elevacion
     * (X) despues; con el orden invertido, la camara pierde la
     * inclinacion hacia abajo en cuanto el azimuth se aleja de 0 grados y
     * termina mirando en linea recta por encima del tablero (pantalla en
     * blanco, sin tocar la placa de ninguna celda).
     */
    private void updateCameraPosition() {
        double elevationRad = Math.toRadians(elevationDeg);
        double azimuthRad = Math.toRadians(azimuthDeg);
        double horizontalDistance = orbitRadius * Math.cos(elevationRad);

        double centerX = BOARD_SPAN / 2.0;
        double centerZ = BOARD_SPAN / 2.0;

        camera.setTranslateX(centerX + horizontalDistance * Math.sin(azimuthRad));
        camera.setTranslateY(-orbitRadius * Math.sin(elevationRad));
        camera.setTranslateZ(centerZ - horizontalDistance * Math.cos(azimuthRad));

        camera.getTransforms().setAll(
                new Rotate(-azimuthDeg, Rotate.Y_AXIS),
                new Rotate(-elevationDeg, Rotate.X_AXIS));
    }

    /**
     * Click sostenido + arrastre gira la vista alrededor del tablero (como
     * girar una mesa): el arrastre horizontal cambia el azimuth (da la
     * vuelta) y el vertical cambia la inclinacion (sube/baja la camara),
     * cada uno recortado a su rango valido (ver ELEVATION_MIN_DEG/MAX_DEG)
     * para no terminar mirando el tablero de canto o por debajo. Ademas la
     * rueda del mouse acerca/aleja la camara (zoom) dentro de
     * ORBIT_RADIUS_MIN/MAX. Todo se engancha en el SubScene (no en cada
     * celda) para que funcione arrastrando/girando la rueda desde cualquier
     * punto del tablero; un clic normal (sin arrastre) sigue disparando el
     * MOUSE_CLICKED de la celda igual que antes, porque JavaFX solo emite
     * ese evento cuando el mouse no se movio mas que un pequeno umbral
     * entre presionar y soltar.
     */
    private void enableDragToOrbit() {
        subScene.setOnMousePressed(event -> {
            lastDragSceneX = event.getSceneX();
            lastDragSceneY = event.getSceneY();
        });
        subScene.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - lastDragSceneX;
            double deltaY = event.getSceneY() - lastDragSceneY;
            lastDragSceneX = event.getSceneX();
            lastDragSceneY = event.getSceneY();

            azimuthDeg += deltaX * DRAG_SENSITIVITY;
            // arrastrar hacia arriba (deltaY negativo) inclina la camara
            // hacia una vista mas cenital, como si se subiera por encima
            // del tablero; arrastrar hacia abajo la baja hacia una vista
            // mas rasante.
            elevationDeg = clamp(elevationDeg - deltaY * DRAG_SENSITIVITY, ELEVATION_MIN_DEG, ELEVATION_MAX_DEG);
            updateCameraPosition();
        });
        subScene.setOnScroll(event -> {
            orbitRadius = clamp(orbitRadius - event.getDeltaY() * ZOOM_SENSITIVITY,
                    ORBIT_RADIUS_MIN, ORBIT_RADIUS_MAX);
            updateCameraPosition();
        });
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private Group createLights() {
        AmbientLight ambient = new AmbientLight(Color.rgb(170, 170, 180));
        PointLight point = new PointLight(Color.WHITE);
        point.setTranslateX(BOARD_SPAN / 2.0);
        point.setTranslateY(-350.0);
        point.setTranslateZ(BOARD_SPAN / 2.0 - 150.0);
        return new Group(ambient, point);
    }

    private void build(Group tilesLayer) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Coordinate coordinate = new Coordinate(row, column);
                Box tile = createTile(coordinate);
                tiles[row][column] = tile;
                tilesLayer.getChildren().add(tile);
                paintCell(coordinate);
            }
        }
    }

    private Box createTile(Coordinate coordinate) {
        Box tile = new Box(CELL_SIZE - 2, TILE_HEIGHT, CELL_SIZE - 2);
        tile.setTranslateX(coordinate.getColumn() * CELL_SIZE + CELL_SIZE / 2.0);
        tile.setTranslateZ(coordinate.getRow() * CELL_SIZE + CELL_SIZE / 2.0);
        tile.setMaterial(new PhongMaterial(EMPTY_COLOR));

        CellInteractionHandler handler = new CellInteractionHandler(coordinate);
        tile.setOnMouseClicked(handler);
        tile.setOnMouseEntered(handler);
        tile.setOnMouseExited(handler);
        return tile;
    }

    /**
     * Clase interna (no estatica: necesita los campos onCellClick/
     * onCellHoverEnter/onCellHoverExit de su Board3DView) que agrupa el
     * manejo de los tres eventos de mouse de una celda (click, entra,
     * sale) en un unico {@link EventHandler}. Un Box (nodo 3D) recibe los
     * mismos eventos de mouse que cualquier otro Node.
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
     * Muestra un "fantasma" 3D del barco que se va a colocar en las
     * casillas indicadas: la placa se tine de verde si la posicion es
     * valida, de rojo si queda fuera del tablero o se superpone con otro
     * barco. No modifica el estado real de esas celdas.
     */
    public void showPlacementPreview(List<Coordinate> coordinates, ShipType type, Orientation orientation,
                                      boolean valid) {
        clearPreview();
        Color tint = valid ? VALID_TINT : INVALID_TINT;
        Color hullDark = tint.darker();
        Color accent = Color.WHITE;

        int size = coordinates.size();
        for (int index = 0; index < size; index++) {
            Coordinate coordinate = coordinates.get(index);
            if (!coordinate.isWithinBoard(Board.SIZE)) {
                continue;
            }
            tiles[coordinate.getRow()][coordinate.getColumn()].setMaterial(new PhongMaterial(tint));
            previewedTiles.add(coordinate);

            Group shape = type == ShipType.FRIGATE
                    ? createFrigateShape(tint, hullDark, accent, Color.WHITE)
                    : createSegmentShape(type, index, size, tint, hullDark, accent, Color.WHITE);
            applyOrientation(shape, orientation);
            shape.setMouseTransparent(true);
            placeOnTile(shape, coordinate, 5.0);

            detailsLayer.getChildren().add(shape);
            activePreviewShapes.add(shape);
        }
    }

    /** Quita cualquier previsualizacion de colocacion activa. */
    public void clearPreview() {
        detailsLayer.getChildren().removeAll(activePreviewShapes);
        activePreviewShapes.clear();

        for (Coordinate coordinate : previewedTiles) {
            paintTileColor(coordinate);
        }
        previewedTiles.clear();
    }

    public void setVerificationMode(boolean verificationMode) {
        this.verificationMode = verificationMode;
        redrawAll();
    }

    public SubScene getNode() {
        return subScene;
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

    /** Centra el nodo sobre la placa de la celda indicada, apoyado en su superficie superior. */
    private void placeOnTile(Node node, Coordinate coordinate, double halfHeight) {
        node.setTranslateX(coordinate.getColumn() * CELL_SIZE + CELL_SIZE / 2.0);
        node.setTranslateZ(coordinate.getRow() * CELL_SIZE + CELL_SIZE / 2.0);
        node.setTranslateY(TILE_TOP_Y - halfHeight);
    }

    private void paintTileColor(Coordinate coordinate) {
        Cell cell = board.getCell(coordinate);
        Box tile = tiles[coordinate.getRow()][coordinate.getColumn()];
        boolean showShip = isOwnBoard || verificationMode;

        switch (cell.getState()) {
            case EMPTY:
                tile.setMaterial(new PhongMaterial(EMPTY_COLOR));
                break;
            case SHIP:
                tile.setMaterial(new PhongMaterial(showShip ? SHIP_VISIBLE_COLOR : EMPTY_COLOR));
                break;
            case WATER:
                tile.setMaterial(new PhongMaterial(WATER_COLOR));
                break;
            case HIT:
                tile.setMaterial(new PhongMaterial(HIT_COLOR));
                break;
            case SUNK:
                tile.setMaterial(new PhongMaterial(SUNK_COLOR));
                break;
            default:
                break;
        }
    }

    private void paintCell(Coordinate coordinate) {
        Cell cell = board.getCell(coordinate);
        paintTileColor(coordinate);

        Node oldDecoration = decorations[coordinate.getRow()][coordinate.getColumn()];
        if (oldDecoration != null) {
            detailsLayer.getChildren().remove(oldDecoration);
            decorations[coordinate.getRow()][coordinate.getColumn()] = null;
        }

        boolean showShip = isOwnBoard || verificationMode;
        Node newDecoration = null;
        double halfHeight = 1.5;

        switch (cell.getState()) {
            case SHIP:
                if (showShip) {
                    newDecoration = createShipDetail(cell);
                    halfHeight = 5.0;
                }
                break;
            case WATER:
                newDecoration = createWaterMark();
                break;
            case HIT:
                newDecoration = createHitMark();
                halfHeight = 6.0;
                break;
            case SUNK:
                newDecoration = createSunkMark();
                break;
            default:
                break;
        }

        if (newDecoration != null) {
            placeOnTile(newDecoration, coordinate, halfHeight);
            newDecoration.setMouseTransparent(true);
            detailsLayer.getChildren().add(newDecoration);
            decorations[coordinate.getRow()][coordinate.getColumn()] = newDecoration;
        }
    }

    /**
     * Arma la silueta 3D del barco al que pertenece esta casilla: cada
     * tipo de barco tiene una forma distinta y reconocible (portaaviones
     * con pista de vuelo e isla lateral, submarino con casco cilindrico y
     * vela, destructor con puente y cañon de proa, fragata con mastil y
     * vela), coloreada en azul con bandera azul si es la flota propia, o
     * en rojo con bandera roja si es la enemiga.
     */
    private Node createShipDetail(Cell cell) {
        Ship ship = cell.getShip();
        Color hull = isOwnBoard ? OWN_HULL : ENEMY_HULL;
        Color hullDark = isOwnBoard ? OWN_HULL_DARK : ENEMY_HULL_DARK;
        Color accent = isOwnBoard ? OWN_ACCENT : ENEMY_ACCENT;
        Color flag = isOwnBoard ? OWN_FLAG : ENEMY_FLAG;

        List<Coordinate> positions = ship.getPositions();
        int index = positions.indexOf(cell.getCoordinate());
        int size = positions.size();

        Group shape = ship.getType() == ShipType.FRIGATE
                ? createFrigateShape(hull, hullDark, accent, flag)
                : createSegmentShape(ship.getType(), index, size, hull, hullDark, accent, flag);

        applyOrientation(shape, ship.getOrientation());
        return shape;
    }

    /**
     * Orienta el grupo (construido "apuntando" hacia +X, que es la
     * direccion que corresponde a RIGHT) rotandolo alrededor del eje Y.
     * A diferencia de la version 2D -que necesitaba invertir el eje X con
     * una Scale para LEFT, porque un dibujo plano rotado 180 grados queda
     * "patas arriba"- en 3D basta con rotar 180 grados: el barco es un
     * objeto real en el espacio, no una silueta plana, asi que girarlo no
     * lo voltea al reves.
     */
    private void applyOrientation(Group shape, Orientation orientation) {
        double angle;
        switch (orientation) {
            case RIGHT:
                angle = 0;
                break;
            case DOWN:
                angle = 270;
                break;
            case LEFT:
                angle = 180;
                break;
            case UP:
                angle = 90;
                break;
            default:
                angle = 0;
        }
        shape.getTransforms().add(new Rotate(angle, Rotate.Y_AXIS));
    }

    /** La fragata ocupa una sola casilla: casco pequeño con caseta, mastil, vela, vigia y bandera. */
    private Group createFrigateShape(Color hull, Color hullDark, Color accent, Color flagColor) {
        // Mismo casco facetado low poly que el resto de la flota (ver
        // HullMeshBuilder): proa en punta (extremo -X angosto) y popa tipo
        // espejo, mas ancha, en vez de la caja lisa original.
        double hullHeight = 8;
        double fullHalfWidth = 7.0;
        double tipHalfWidth = 1.5;
        MeshView hullMesh = HullMeshBuilder.buildSegment(26, tipHalfWidth, fullHalfWidth,
                -hullHeight / 2.0, hullHeight, true, true);
        hullMesh.setMaterial(new PhongMaterial(hull));

        Box deckhouse = new Box(8, 5, 8);
        deckhouse.setTranslateY(-6.5);
        deckhouse.setMaterial(new PhongMaterial(accent));

        Cylinder mast = new Cylinder(1.0, 18);
        mast.setTranslateY(-18);
        mast.setMaterial(new PhongMaterial(hullDark));

        // vela triangular: una caja delgada inclinada da la silueta de una vela vista de perfil
        Box sail = new Box(0.5, 12, 8);
        sail.setTranslateX(2);
        sail.setTranslateY(-16);
        sail.getTransforms().add(new Rotate(20, Rotate.Z_AXIS));
        sail.setMaterial(new PhongMaterial(Color.web("#F5F1E6")));

        Group flag = createFlag(flagColor);
        flag.setTranslateY(-25.5);

        Sphere porthole = new Sphere(1.8);
        porthole.setTranslateX(-6);
        porthole.setTranslateY(2);
        porthole.setTranslateZ(7.1);
        porthole.setMaterial(new PhongMaterial(accent));

        return new Group(hullMesh, deckhouse, mast, sail, flag, porthole);
    }

    /**
     * Los demas tipos ocupan varias casillas: la primera (proa) y la
     * ultima (popa, con la estructura propia del tipo) usan el borde
     * angosto de su propia casilla; el lado que conecta con la siguiente
     * casilla se extiende (ver {@link #HULL_BRIDGE}) para que el casco se
     * vea de una sola pieza. El submarino usa un casco cilindrico en vez
     * de una caja (mucho mas reconocible); el portaaviones agrega una
     * pista de vuelo plana sobre cada segmento.
     */
    private Group createSegmentShape(ShipType type, int index, int size, Color hull, Color hullDark, Color accent,
                                      Color flagColor) {
        boolean isBow = index == 0;
        boolean isTail = index == size - 1;

        double leftEdge = isBow ? -HULL_EDGE : -HULL_BRIDGE;
        double rightEdge = isTail ? HULL_EDGE : HULL_BRIDGE;
        double length = rightEdge - leftEdge;
        double centerX = (leftEdge + rightEdge) / 2.0;

        Group group = type == ShipType.SUBMARINE
                ? createSubmarineHullSegment(centerX, length, hull, hullDark)
                : createFacetedHullSegment(centerX, length, hull, isBow, isTail,
                        type == ShipType.AIRCRAFT_CARRIER);

        if (isTail) {
            Node stern = createSternStructure(type, accent, flagColor);
            stern.setTranslateX(centerX);
            group.getChildren().add(stern);
        } else if (isBow && type == ShipType.DESTROYER) {
            Node gun = createBowGun(accent);
            gun.setTranslateX(centerX);
            group.getChildren().add(gun);
        } else if (!isBow) {
            Node middle = createMiddleDetail(type, accent);
            if (middle != null) {
                middle.setTranslateX(centerX);
                group.getChildren().add(middle);
            }
        }
        return group;
    }

    /**
     * Casco facetado "low poly" (destructor, portaaviones), armado con
     * {@link HullMeshBuilder}: seccion transversal pentagonal (quilla en V,
     * costados abiertos hacia la cubierta) en vez de una caja lisa. El
     * extremo que da hacia la proa/popa real del barco se angosta casi a
     * una arista (ver {@link #HULL_EDGE}); el que conecta con el siguiente
     * segmento se queda en el ancho maximo. Agrega ademas la pista de
     * vuelo si es un portaaviones.
     */
    private Group createFacetedHullSegment(double centerX, double length, Color hull,
                                            boolean isBow, boolean isTail, boolean isCarrier) {
        double hullHeight = isCarrier ? 8 : 10;
        double fullHalfWidth = (isCarrier ? 20 : 18) / 2.0;
        double tipHalfWidth = 2.0;

        double startHalfWidth = isBow ? tipHalfWidth : fullHalfWidth;
        double endHalfWidth = isTail ? tipHalfWidth : fullHalfWidth;
        double deckY = -hullHeight / 2.0;
        double keelY = hullHeight;

        MeshView hullMesh = HullMeshBuilder.buildSegment(length, startHalfWidth, endHalfWidth, deckY, keelY,
                isBow, isTail);
        hullMesh.setTranslateX(centerX);
        hullMesh.setMaterial(new PhongMaterial(hull));

        // A diferencia de la caja lisa original, este casco ya se angosta
        // hacia la punta; una banda de "linea de flotacion" de ancho fijo
        // sobresaldria del casco angosto en ese extremo, formando una
        // silueta irregular que con hullDark (un azul/rojo casi negro) se
        // leia como una mancha oscura dentada en vez de un detalle. La
        // quilla en V de la malla ya se ve mas oscura por su propia
        // inclinacion ante la luz, asi que no hace falta la caja aparte.
        Group group = new Group(hullMesh);

        if (isCarrier) {
            // pista de vuelo: gris, mas ancha que el casco (sobresale a los lados, como en un portaaviones real)
            Box deck = new Box(length + 2, 2, fullHalfWidth * 2 + 10);
            deck.setTranslateX(centerX);
            deck.setTranslateY(-(hullHeight / 2.0 + 1));
            deck.setMaterial(new PhongMaterial(FLIGHT_DECK_COLOR));

            Box centerline = new Box(length + 2, 0.3, 1.5);
            centerline.setTranslateX(centerX);
            centerline.setTranslateY(-(hullHeight / 2.0 + 2.2));
            centerline.setMaterial(new PhongMaterial(FLIGHT_DECK_STRIPE));

            group.getChildren().addAll(deck, centerline);
        }

        return group;
    }

    /**
     * Casco cilindrico horizontal del submarino: mucho mas reconocible que
     * una caja. Con pocas divisiones (8, en vez de las 64 por defecto) el
     * cilindro queda visiblemente facetado en vez de perfectamente
     * redondo, para que combine con el estilo low poly del resto de los
     * barcos (ver el capitulo de formas 3D: menos divisiones = menos
     * triangulos = superficie mas angulosa).
     */
    private Group createSubmarineHullSegment(double centerX, double length, Color hull, Color hullDark) {
        double radius = 8.0;
        int divisions = 8;

        Cylinder hullCylinder = new Cylinder(radius, length, divisions);
        hullCylinder.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        hullCylinder.setTranslateX(centerX);
        hullCylinder.setMaterial(new PhongMaterial(hull));

        Box waterline = new Box(length, 2, radius * 2);
        waterline.setTranslateX(centerX);
        waterline.setTranslateY(radius + 1);
        waterline.setMaterial(new PhongMaterial(hullDark));

        return new Group(hullCylinder, waterline);
    }

    /** Bandera pequeña (mastil + tela) del color de equipo, para la popa/torre de cada barco. */
    private Group createFlag(Color flagColor) {
        Cylinder pole = new Cylinder(0.4, 7);
        pole.setMaterial(new PhongMaterial(Color.web("#3A2412")));

        Box cloth = new Box(4.5, 2.6, 0.3);
        cloth.setTranslateX(2.2);
        cloth.setTranslateY(-2.6);
        cloth.setMaterial(new PhongMaterial(flagColor));

        return new Group(pole, cloth);
    }

    /** Cañon de proa del destructor: torreta esferica + cañon cilindrico apuntando hacia adelante. */
    private Node createBowGun(Color accent) {
        Sphere turret = new Sphere(3.0);
        turret.setTranslateY(-6);
        turret.setMaterial(new PhongMaterial(accent));

        Cylinder barrel = new Cylinder(0.8, 9);
        barrel.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        barrel.setTranslateX(4);
        barrel.setTranslateY(-6);
        barrel.setMaterial(new PhongMaterial(accent));

        return new Group(turret, barrel);
    }

    private Node createSternStructure(ShipType type, Color accent, Color flagColor) {
        switch (type) {
            case DESTROYER: {
                Box bridge = new Box(12, 8, 10);
                bridge.setTranslateY(-9);
                bridge.setMaterial(new PhongMaterial(accent));

                Cylinder mast = new Cylinder(0.7, 10);
                mast.setTranslateY(-18);
                mast.setMaterial(new PhongMaterial(accent));

                Box radarDish = new Box(6, 1.5, 1);
                radarDish.setTranslateY(-22.5);
                radarDish.setMaterial(new PhongMaterial(accent));

                Group flag = createFlag(flagColor);
                flag.setTranslateY(-13);
                flag.setTranslateZ(5.5);

                return new Group(bridge, mast, radarDish, flag);
            }
            case SUBMARINE: {
                // vela/torre de mando: mas alta y afilada que la version anterior
                Box sail = new Box(9, 13, 6);
                sail.setTranslateY(-14.5);
                sail.setMaterial(new PhongMaterial(accent));

                Cylinder periscope = new Cylinder(0.6, 10);
                periscope.setTranslateX(-2);
                periscope.setTranslateY(-24);
                periscope.setMaterial(new PhongMaterial(accent));

                Cylinder snorkel = new Cylinder(0.6, 8);
                snorkel.setTranslateX(2);
                snorkel.setTranslateY(-23);
                snorkel.setMaterial(new PhongMaterial(accent));

                Group flag = createFlag(flagColor);
                flag.setTranslateY(-19.5);
                flag.setTranslateZ(3.5);

                return new Group(sail, periscope, snorkel, flag);
            }
            case AIRCRAFT_CARRIER: {
                // isla desplazada hacia un costado, como en un portaaviones real (no va centrada).
                // Color gris oscuro (no el acento, casi blanco): sobre la pista de vuelo gris
                // clara se perderia por completo si usara el mismo tono palido.
                Box island = new Box(14, 16, 8);
                island.setTranslateY(-14);
                island.setTranslateZ(7);
                island.setMaterial(new PhongMaterial(ISLAND_COLOR));

                Cylinder radarMast = new Cylinder(0.7, 10);
                radarMast.setTranslateY(-27);
                radarMast.setTranslateZ(7);
                radarMast.setMaterial(new PhongMaterial(accent));

                Box radarDish = new Box(7, 1.5, 1.5);
                radarDish.setTranslateY(-31.5);
                radarDish.setTranslateZ(7);
                radarDish.setMaterial(new PhongMaterial(accent));

                Group flag = createFlag(flagColor);
                flag.setTranslateY(-31.5);
                flag.setTranslateZ(10.5);

                return new Group(island, radarMast, radarDish, flag);
            }
            default:
                return new Group();
        }
    }

    private Node createMiddleDetail(ShipType type, Color accent) {
        if (type == ShipType.AIRCRAFT_CARRIER) {
            // un par de siluetas de avion sobre la pista, a lado y lado de la
            // linea central; en gris oscuro (no blanco) para que se noten
            // sobre la pista de vuelo en vez de perderse en ella.
            Group planes = new Group();
            for (double side : new double[] {-7.0, 7.0}) {
                Box fuselage = new Box(11, 1.8, 2.5);
                fuselage.setTranslateZ(side);
                fuselage.setTranslateY(-7.6);
                fuselage.setMaterial(new PhongMaterial(PLANE_COLOR));

                Box wings = new Box(2.5, 1.2, 10);
                wings.setTranslateZ(side);
                wings.setTranslateY(-7.6);
                wings.setMaterial(new PhongMaterial(PLANE_COLOR));

                planes.getChildren().addAll(fuselage, wings);
            }
            return planes;
        }
        if (type == ShipType.SUBMARINE) {
            return null;
        }
        Sphere porthole = new Sphere(2.2);
        porthole.setTranslateY(-6);
        porthole.setMaterial(new PhongMaterial(accent));
        return porthole;
    }

    /** Marca de agua: una "X" plana de dos barras cruzadas sobre la placa. */
    private Group createWaterMark() {
        PhongMaterial material = new PhongMaterial(Color.WHITE);

        Box bar1 = new Box(16, 2, 2);
        bar1.getTransforms().add(new Rotate(45, Rotate.Y_AXIS));
        bar1.setMaterial(material);

        Box bar2 = new Box(16, 2, 2);
        bar2.getTransforms().add(new Rotate(-45, Rotate.Y_AXIS));
        bar2.setMaterial(material);

        return new Group(bar1, bar2);
    }

    private Sphere createHitMark() {
        Sphere sphere = new Sphere(6);
        sphere.setMaterial(new PhongMaterial(Color.web("#4A1B0C")));
        return sphere;
    }

    /** Marca de hundido: una "X" mas grande y gruesa que la de agua. */
    private Group createSunkMark() {
        PhongMaterial material = new PhongMaterial(Color.WHITE);

        Box bar1 = new Box(22, 2.5, 2.5);
        bar1.getTransforms().add(new Rotate(45, Rotate.Y_AXIS));
        bar1.setMaterial(material);

        Box bar2 = new Box(22, 2.5, 2.5);
        bar2.getTransforms().add(new Rotate(-45, Rotate.Y_AXIS));
        bar2.setMaterial(material);

        return new Group(bar1, bar2);
    }
}
