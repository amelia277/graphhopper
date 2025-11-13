/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper.util.shapes;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Robin Boldt
 */
public class PolygonTest {

    @Test
    public void testContains(){

        /*
         * |----|
         * |    |
         * |----|
         */
        Polygon square = new Polygon(new double[]{0,0,20,20}, new double[]{0,20,20,0});
        assertTrue(square.contains(10,10));
        assertTrue(square.contains(16,10));
        assertFalse(square.contains(10,-20));
        assertTrue(square.contains(10,0.1));
        assertFalse(square.contains(10,20));
        assertTrue(square.contains(10,16));
        assertFalse(square.contains(20,20));

        /*
         * \-----|
         *   --| |
         *   --| |
         *  /----|
         */
        Polygon squareHole = new Polygon(new double[]{0,0,20,20,15,15,5,5}, new double[]{0,20,20,0,5,15,15,5});
        assertFalse(squareHole.contains(10,10));
        assertTrue(squareHole.contains(16,10));
        assertFalse(squareHole.contains(10,-20));
        assertFalse(squareHole.contains(10,0));
        assertFalse(squareHole.contains(10,20));
        assertTrue(squareHole.contains(10,16));
        assertFalse(squareHole.contains(20,20));



        /*
         * |----|
         * |    |
         * |----|
         */
        square = new Polygon(new double[]{1, 1, 2, 2}, new double[]{1, 2, 2, 1});

        assertTrue(square.contains(1.5,1.5));
        assertFalse(square.contains(0.5,1.5));

        /*
         * |----|
         * | /\ |
         * |/  \|
         */
        squareHole = new Polygon(new double[]{1, 1, 2, 1.1, 2}, new double[]{1, 2, 2, 1.5, 1});

        assertTrue(squareHole.contains(1.1,1.1));
        assertFalse(squareHole.contains(1.5,1.5));
        assertFalse(squareHole.contains(0.5,1.5));

    }
    /**
     * testPolygonBoundaryCoordinates
     *
     * but : vérifier que les méthodes de limites retournent bien les coordonnées min et max.
     * idée : s’assurer que getMinLat(), getMinLon(), getMaxLat(), getMaxLon()
     * donnent bien les valeurs extrêmes des sommets du polygone.
     * tests :
     *  - rectangle simple (50–52, 10–12)
     *  - triangle différent (0,0 / 5,0 / 2.5,4)
     * attendu :
     *  - rectangle = min lat 50, min lon 10, max lat 52, max lon 12
     *  - triangle = min lat/lon 0, max lat 5, max lon 4
     * tolérance : 1e-10
     */
    @Test
    public void testPolygonBoundaryCoordinates() {
        // rectangle simple
        double[] lats = {50.0, 50.0, 52.0, 52.0};
        double[] lons = {10.0, 12.0, 12.0, 10.0};

        Polygon polygon = new Polygon(lats, lons);

        // test des valeurs minimales
        assertEquals(50.0, polygon.getMinLat(), 1e-10,
                "La latitude minimale doit être la plus basse");
        assertEquals(10.0, polygon.getMinLon(), 1e-10,
                "La longitude minimale doit être la plus basse");

        // test des valeurs maximales
        assertEquals(52.0, polygon.getMaxLat(), 1e-10,
                "La latitude maximale doit être la plus haute");
        assertEquals(12.0, polygon.getMaxLon(), 1e-10,
                "La longitude maximale doit être la plus haute");

        // autre forme : triangle
        double[] triangleLats = {0.0, 5.0, 2.5};
        double[] triangleLons = {0.0, 0.0, 4.0};
        Polygon triangle = new Polygon(triangleLats, triangleLons);

        assertEquals(0.0, triangle.getMinLat(), 1e-10);
        assertEquals(0.0, triangle.getMinLon(), 1e-10);
        assertEquals(5.0, triangle.getMaxLat(), 1e-10);
        assertEquals(4.0, triangle.getMaxLon(), 1e-10);
    }

    /**
     * testPolygonStaticFactoryCreation
     *
     * but : vérifier que la méthode create() fonctionne avec un objet Polygon de JTS.
     * idée : utile pour s’assurer que la création depuis une autre bibliothèque marche bien.
     * test :
     *  - création d’un carré unitaire (0,0) = (1,1)
     *  - points de test : (0.5, 0.5) à l’intérieur, (2.0, 2.0) à l’extérieur
     * attendu :
     *  - create() ne renvoie pas null
     *  - getBounds() non null
     *  - contains(0.5, 0.5) = true, contains(2.0, 2.0) = false
     *  - limites correctes (0 à 1)
     */
    @Test
    public void testPolygonStaticFactoryCreation() {
        GeometryFactory geoFactory = new GeometryFactory();
        Coordinate[] coords = {
                new Coordinate(0.0, 0.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(1.0, 1.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(0.0, 0.0) // fermeture du polygone
        };
        org.locationtech.jts.geom.Polygon jtsPolygon = geoFactory.createPolygon(coords);


        // création via la méthode statique
        Polygon polygon = Polygon.create(jtsPolygon);

        assertNotNull(polygon, "Le polygone créé ne doit pas être null");
        assertNotNull(polygon.getBounds(), "Le polygone doit avoir des limites valides");

        // vérifie le comportement du polygone
        assertTrue(polygon.contains(0.5, 0.5), "Le point (0.5,0.5) doit être à l’intérieur");
        assertFalse(polygon.contains(2.0, 2.0), "Le point (2.0,2.0) doit être à l’extérieur");

        // vérifie les limites
        assertEquals(0.0, polygon.getMinLat(), 1e-10);
        assertEquals(0.0, polygon.getMinLon(), 1e-10);
        assertEquals(1.0, polygon.getMaxLat(), 1e-10);
        assertEquals(1.0, polygon.getMaxLon(), 1e-10);
    }

    /**
     * testPolygonRectangleDetection
     *
     * but : vérifier que isRectangle() détecte bien les rectangles et carrés.
     * idée : utile pour optimiser les calculs selon la forme.
     * test :
     *  - rectangle 10×10 = doit renvoyer true
     *  - triangle = false
     *  - pentagone = false
     *  - carré = true
     */

    @Test
    public void testPolygonRectangleDetection() {
        // rectangle = true

        double[] rectLats = {10.0, 10.0, 20.0, 20.0};
        double[] rectLons = {5.0, 15.0, 15.0, 5.0};
        Polygon rectangle = new Polygon(rectLats, rectLons);

        assertTrue(rectangle.isRectangle(),
                "Un rectangle parfait doit être reconnu");

        // triangle = false
        double[] triLats = {0.0, 3.0, 1.5};
        double[] triLons = {0.0, 0.0, 2.0};
        Polygon triangle = new Polygon(triLats, triLons);

        assertFalse(triangle.isRectangle(),
                "Un triangle ne doit pas être reconnu comme rectangle");

        // pentagone = false
        double[] pentLats = {0.0, 1.0, 1.5, 1.0, 0.0};
        double[] pentLons = {0.0, 0.0, 0.5, 1.0, 1.0};
        Polygon pentagon = new Polygon(pentLats, pentLons);

        assertFalse(pentagon.isRectangle(),
                "Un pentagone ne doit pas être reconnu comme rectangle");

        // carré = true
        double[] squareLats = {0.0, 0.0, 1.0, 1.0};
        double[] squareLons = {0.0, 1.0, 1.0, 0.0};
        Polygon square = new Polygon(squareLats, squareLons);

        assertTrue(square.isRectangle(),
                "Un carré doit être reconnu comme rectangle");
    }

    /**
     * testPolygonStringRepresentation
     *
     * but : vérifier que toString() renvoie une description claire et utile du polygone.
     * test :
     *  - un triangle simple
     *  - un rectangle 10×10
     * attendu :
     *  - la chaîne n’est jamais vide ni null
     *  - contient les mots "polygon", "points", "geometries" et des chiffres
     *  - chaque forme a une chaîne différente
     */
    @Test
    public void testPolygonStringRepresentation() {
        // triangle simple
        double[] triLats = {0.0, 1.0, 0.5};
        double[] triLons = {0.0, 0.0, 1.0};
        Polygon triangle = new Polygon(triLats, triLons);

        String triString = triangle.toString();
        assertNotNull(triString, "toString() ne doit jamais être null");
        assertFalse(triString.isEmpty(), "toString() ne doit pas être vide");

        // rectangle différent
        assertTrue(triString.contains("polygon"), "Doit mentionner le type 'polygon'");
        assertTrue(triString.contains("points"), "Doit mentionner le nombre de points");
        assertTrue(triString.contains("geometries"), "Doit mentionner le nombre de géométries");

        // rectangle
        double[] rectLats = {10.0, 10.0, 20.0, 20.0};
        double[] rectLons = {5.0, 15.0, 15.0, 5.0};
        Polygon rectangle = new Polygon(rectLats, rectLons);

        String rectString = rectangle.toString();
        assertNotNull(rectString, "toString du rectangle ne doit pas être null");

        assertNotEquals(triString, rectString,
                "Les deux polygones doivent avoir des chaînes différentes");

        assertTrue(rectString.toLowerCase().contains("polygon"),
                "Doit contenir 'polygon'");

        assertTrue(rectString.matches(".*\\d+.*"),
                "Doit contenir des valeurs numériques");
    }

    /**
     * testPolygonWithFaker
     *
     * but : vérifier que le comportement reste correct avec des données générées aléatoirement.
     * idée : s’assurer que le code fonctionne aussi hors des cas fixes.
     * test :
     *  - 5 rectangles aléatoires (seed fixe pour reproductibilité)
     * attendu :
     *  - getBounds() ≠ null
     *  - isRectangle() → true
     *  - les limites correspondent bien aux coordonnées générées
     */

    @Test
    public void testPolygonWithFaker() {
        Faker faker = new Faker(new Random(42)); // seed fixe

        for (int i = 0; i < 5; i++) {
            // coordonnées aléatoires de base
            double baseLat = Double.parseDouble(faker.address().latitude());
            double baseLon = Double.parseDouble(faker.address().longitude());

            // Créer un rectangle de taille variable
            double width = 0.1 + faker.number().randomDouble(2, 0, 19) / 10.0;  // 0.1° à 2.0°
            double height = 0.1 + faker.number().randomDouble(2, 0, 19) / 10.0; // 0.1° à 2.0°

            // Définir les coins du rectangle
            double[] lats = {baseLat, baseLat, baseLat + height, baseLat + height};
            double[] lons = {baseLon, baseLon + width, baseLon + width, baseLon};

            Polygon polygon = new Polygon(lats, lons);

            // Tests:
            assertNotNull(polygon.getBounds(),
                    "getBounds ne doit jamais être null");
            assertTrue(polygon.isRectangle(),
                    "Le rectangle généré doit être détecté comme rectangle");
            assertEquals(baseLat, polygon.getMinLat(), 1e-10,
                    "La latitude min doit être correcte");
            assertEquals(baseLon, polygon.getMinLon(), 1e-10,
                    "La longitude min doit être correcte");
            assertEquals(baseLat + height, polygon.getMaxLat(), 1e-10,
                    "La latitude max doit être correcte");
            assertEquals(baseLon + width, polygon.getMaxLon(), 1e-10,
                    "La longitude max doit être correcte");
        }
    }

}
