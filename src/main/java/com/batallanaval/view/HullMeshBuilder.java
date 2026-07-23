package com.batallanaval.view;

import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.util.ArrayList;
import java.util.List;

/**
 * Genera un segmento de casco de barco "low poly" facetado (sin imagenes,
 * solo caras planas) como una malla de triangulos hecha a mano con
 * {@link TriangleMesh}: un "tubo" con seccion transversal pentagonal
 * (quilla en V, costados que se abren hacia la cubierta) entre dos anillos,
 * uno en cada extremo del segmento. Si el ancho de un extremo es (casi)
 * cero, ese anillo colapsa en una arista vertical angosta en vez de un
 * punto matematico exacto - el efecto visual es una proa/popa en punta,
 * igual que la roda de un barco real.
 *
 * Se arma por segmento (no un barco entero de una pieza) para poder seguir
 * revelando solo la parte tocada de un barco cuando recibe un disparo
 * (HU-2: "aparecera esa parte del barco"), en vez de todo el barco de
 * golpe: cada celda del barco sigue siendo su propio segmento, igual que
 * en la version con Box, solo que ahora con una seccion transversal
 * facetada en vez de una caja lisa.
 *
 * El eje X local es el largo del segmento (dos anillos, uno en -length/2 y
 * otro en +length/2), Y es altura (Y+ hacia abajo, como en toda la escena)
 * y Z es el ancho (babor/estribor).
 */
public final class HullMeshBuilder {

    private static final int RING_POINTS = 5;

    private HullMeshBuilder() {
    }

    /**
     * capStart/capEnd controlan si cada extremo se tapa con un abanico de
     * triangulos o se deja abierto. Solo hay que tapar la punta REAL del
     * barco (la proa del primer segmento, la popa del ultimo); el lado que
     * conecta con el siguiente segmento (ver HULL_BRIDGE en Board3DView) no
     * se tapa, porque ese anillo es identico (misma X, mismo ancho) al
     * anillo con el que arranca el segmento vecino, y las dos tapas
     * superpuestas en el mismo plano hacian z-fighting (un mordisco/muesca
     * parpadeante justo en la union, en vez de un casco de una sola pieza).
     */
    public static MeshView buildSegment(double length, double startHalfWidth, double endHalfWidth,
                                         double deckY, double keelY, boolean capStart, boolean capEnd) {
        float[] points = new float[RING_POINTS * 2 * 3];
        int p = 0;
        p = putRing(points, p, -length / 2.0, startHalfWidth, deckY, keelY);
        putRing(points, p, length / 2.0, endHalfWidth, deckY, keelY);

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(0.5f, 0.5f);
        mesh.getFaces().addAll(buildFaces(capStart, capEnd));

        MeshView meshView = new MeshView(mesh);
        // Con una unica formula de triangulado para los 5 "costados" del
        // anillo (ver buildFaces), el lado de babor queda con la normal
        // invertida respecto al de estribor (son simetricos, no iguales) -
        // sin esto, esa pared se recortaba (culling) y el barco se veia
        // "hueco" mirandolo justo desde ese lado.
        meshView.setCullFace(CullFace.NONE);
        return meshView;
    }

    private static int putPoint(float[] points, int p, double x, double y, double z) {
        points[p++] = (float) x;
        points[p++] = (float) y;
        points[p++] = (float) z;
        return p;
    }

    /** Anillo de 5 puntos en X fijo: quilla, inferior-derecha, cubierta-derecha, cubierta-izquierda, inferior-izquierda. */
    private static int putRing(float[] points, int p, double x, double halfWidth, double deckY, double keelY) {
        double midY = keelY * 0.35;
        p = putPoint(points, p, x, keelY, 0);
        p = putPoint(points, p, x, midY, halfWidth * 0.7);
        p = putPoint(points, p, x, deckY, halfWidth);
        p = putPoint(points, p, x, deckY, -halfWidth);
        p = putPoint(points, p, x, midY, -halfWidth * 0.7);
        return p;
    }

    /**
     * Indices de vertices: 0-4 = anillo de inicio, 5-9 = anillo de fin.
     * Ademas de las caras laterales (el "tubo" entre los dos anillos), el
     * extremo pedido se cierra con un abanico de triangulos (tapa) para que
     * no se vea "a traves" del casco mirando casi de frente a la proa o la
     * popa (angulo de camara bajo + zoom).
     */
    private static int[] buildFaces(boolean capStart, boolean capEnd) {
        List<int[]> triangles = new ArrayList<>();
        int[] ringA = {0, 1, 2, 3, 4};
        int[] ringB = {5, 6, 7, 8, 9};

        for (int i = 0; i < RING_POINTS; i++) {
            int a = ringA[i];
            int aNext = ringA[(i + 1) % RING_POINTS];
            int b = ringB[i];
            int bNext = ringB[(i + 1) % RING_POINTS];
            triangles.add(new int[] {a, b, bNext});
            triangles.add(new int[] {a, bNext, aNext});
        }

        if (capStart) {
            // Tapa del anillo de inicio (mira hacia -X): abanico desde el
            // punto 0 (quilla), en orden inverso al de la tapa del otro
            // extremo porque da al lado contrario.
            for (int i = 1; i < RING_POINTS - 1; i++) {
                triangles.add(new int[] {ringA[0], ringA[i + 1], ringA[i]});
            }
        }
        if (capEnd) {
            // Tapa del anillo de fin (mira hacia +X): mismo abanico, orden normal.
            for (int i = 1; i < RING_POINTS - 1; i++) {
                triangles.add(new int[] {ringB[0], ringB[i], ringB[i + 1]});
            }
        }

        int[] faces = new int[triangles.size() * 6];
        int f = 0;
        for (int[] tri : triangles) {
            faces[f++] = tri[0];
            faces[f++] = 0;
            faces[f++] = tri[1];
            faces[f++] = 0;
            faces[f++] = tri[2];
            faces[f++] = 0;
        }
        return faces;
    }
}
