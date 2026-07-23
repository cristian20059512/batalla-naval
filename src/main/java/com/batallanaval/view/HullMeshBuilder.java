package com.batallanaval.view;

import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a faceted "low poly" ship hull segment (no images, only flat
 * faces) as a hand-built {@link TriangleMesh}: a "tube" with a pentagonal
 * cross-section (V-shaped keel, sides opening up toward the deck) between
 * two rings, one at each end of the segment. If an end's width is (almost)
 * zero, that ring collapses into a narrow vertical edge instead of an
 * exact mathematical point - the visual effect is a pointed bow/stern,
 * just like a real ship's stem.
 *
 * It is built per segment (not a whole ship in one piece) so it can keep
 * revealing only the part of a ship that was hit when it takes a shot
 * (HU-2: "that part of the ship will appear"), instead of the whole ship
 * at once: each cell of the ship stays its own segment, just like in the
 * Box version, only now with a faceted cross-section instead of a plain
 * box.
 *
 * The local X axis is the segment's length (two rings, one at -length/2
 * and another at +length/2), Y is height (Y+ downward, as in the whole
 * scene), and Z is width (port/starboard).
 */
public final class HullMeshBuilder {

    private static final int RING_POINTS = 5;

    private HullMeshBuilder() {
    }

    /**
     * capStart/capEnd control whether each end is closed off with a
     * triangle fan or left open. Only the ship's ACTUAL tip needs to be
     * capped (the first segment's bow, the last one's stern); the side
     * that connects to the next segment (see HULL_BRIDGE in Board3DView)
     * is not capped, because that ring is identical (same X, same width)
     * to the ring the neighboring segment starts with, and the two caps
     * overlapping on the same plane caused z-fighting (a flickering
     * notch/bite right at the joint, instead of a single-piece hull).
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

    /** Ring of 5 points at a fixed X: keel, lower-right, deck-right, deck-left, lower-left. */
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
     * Vertex indices: 0-4 = start ring, 5-9 = end ring. Besides the side
     * faces (the "tube" between the two rings), the requested end is
     * closed off with a triangle fan (cap) so the hull cannot be seen
     * "through" when looking almost straight at the bow or stern (low
     * camera angle + zoom).
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
