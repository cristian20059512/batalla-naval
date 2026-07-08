package com.batallanaval.view;

import com.batallanaval.model.Board;
import com.batallanaval.model.BoardListener;
import com.batallanaval.model.Cell;
import com.batallanaval.util.Coordenada;
import com.batallanaval.util.EstadoCelda;

import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

/**
 * Representacion visual de un {@link Board} usando figuras 2D de JavaFX
 * (Rectangle, Circle, Line). Se registra como {@link BoardListener} para
 * repintar automaticamente la celda que cambio, sin que el modelo conozca
 * nada de JavaFX (Observer).
 *
 * Si es el tablero propio (de posicion) siempre muestra los barcos.
 * Si es el tablero del oponente, los barcos permanecen ocultos hasta que
 * son tocados/hundidos, salvo que se active el modo de verificacion (HU-3).
 */
public class BoardView implements BoardListener {

    private static final int TAMANIO_CELDA = 32;

    private final Board tablero;
    private final boolean esTableroPropio;
    private final GridPane grid = new GridPane();
    private final StackPane[][] celdasVisuales = new StackPane[Board.TAMANIO][Board.TAMANIO];

    private boolean modoVerificacion = false;
    private Consumer<Coordenada> onCeldaClic;

    public BoardView(Board tablero, boolean esTableroPropio) {
        this.tablero = tablero;
        this.esTableroPropio = esTableroPropio;
        grid.setHgap(2);
        grid.setVgap(2);
        construir();
    }

    private void construir() {
        for (int fila = 0; fila < Board.TAMANIO; fila++) {
            for (int columna = 0; columna < Board.TAMANIO; columna++) {
                Coordenada coordenada = new Coordenada(fila, columna);
                StackPane celda = crearCelda(coordenada);
                celdasVisuales[fila][columna] = celda;
                pintarCelda(coordenada);
                grid.add(celda, columna, fila);
            }
        }
    }

    private StackPane crearCelda(Coordenada coordenada) {
        StackPane stack = new StackPane();
        Rectangle base = new Rectangle(TAMANIO_CELDA, TAMANIO_CELDA);
        base.setArcWidth(4);
        base.setArcHeight(4);
        base.setStroke(Color.web("#5f5e5a"));
        base.setStrokeWidth(0.5);
        stack.getChildren().add(base);
        stack.setOnMouseClicked(evento -> {
            if (onCeldaClic != null) {
                onCeldaClic.accept(coordenada);
            }
        });
        return stack;
    }

    public void setOnCeldaClic(Consumer<Coordenada> manejador) {
        this.onCeldaClic = manejador;
    }

    public void setModoVerificacion(boolean modoVerificacion) {
        this.modoVerificacion = modoVerificacion;
        redibujarTodo();
    }

    public GridPane getNodo() {
        return grid;
    }

    public void redibujarTodo() {
        for (int fila = 0; fila < Board.TAMANIO; fila++) {
            for (int columna = 0; columna < Board.TAMANIO; columna++) {
                pintarCelda(new Coordenada(fila, columna));
            }
        }
    }

    @Override
    public void onCambioCelda(Coordenada coordenada, EstadoCelda nuevoEstado) {
        pintarCelda(coordenada);
    }

    private void pintarCelda(Coordenada coordenada) {
        Cell celda = tablero.getCelda(coordenada);
        StackPane stack = celdasVisuales[coordenada.getFila()][coordenada.getColumna()];

        // deja solo el rectangulo base (primer hijo) y quita marcas anteriores
        while (stack.getChildren().size() > 1) {
            stack.getChildren().remove(1);
        }

        Rectangle base = (Rectangle) stack.getChildren().get(0);
        boolean mostrarBarco = esTableroPropio || modoVerificacion;

        switch (celda.getEstado()) {
            case VACIA:
                base.setFill(Color.web("#B5D4F4"));
                break;
            case BARCO:
                base.setFill(mostrarBarco ? Color.web("#5F5E5A") : Color.web("#B5D4F4"));
                break;
            case AGUA:
                base.setFill(Color.web("#378ADD"));
                stack.getChildren().add(crearMarcaAgua());
                break;
            case TOCADO:
                base.setFill(Color.web("#F2A623"));
                stack.getChildren().add(crearMarcaTocado());
                break;
            case HUNDIDO:
                base.setFill(Color.web("#791F1F"));
                stack.getChildren().add(crearMarcaHundido());
                break;
            default:
                break;
        }
    }

    private Group crearMarcaAgua() {
        Line l1 = new Line(-6, -6, 6, 6);
        Line l2 = new Line(-6, 6, 6, -6);
        l1.setStroke(Color.WHITE);
        l2.setStroke(Color.WHITE);
        l1.setStrokeWidth(2);
        l2.setStrokeWidth(2);
        return new Group(l1, l2);
    }

    private Circle crearMarcaTocado() {
        Circle circulo = new Circle(6);
        circulo.setFill(Color.web("#4A1B0C"));
        return circulo;
    }

    private Group crearMarcaHundido() {
        Line l1 = new Line(-8, -8, 8, 8);
        Line l2 = new Line(-8, 8, 8, -8);
        l1.setStroke(Color.WHITE);
        l2.setStroke(Color.WHITE);
        l1.setStrokeWidth(3);
        l2.setStrokeWidth(3);
        return new Group(l1, l2);
    }
}
