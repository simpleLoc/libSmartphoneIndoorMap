package de.fhws.indoor.libsmartphoneindoormap.model;

import java.util.ArrayList;

public class Outline {

    public static enum PolygonMethod {
        ADD,
        REMOVE;

        public static PolygonMethod parse(long id) {
            if(id == 0) { return ADD; }
            return REMOVE;
        }
    }

    public static class Polygon {
        public final ArrayList<Vec3> points = new ArrayList<>();
        public PolygonMethod method;

        public void addPoint(float x, float y, float z) {
            points.add(new Vec3(x, y, z));
        }
    }

    final ArrayList<Polygon> polygons = new ArrayList<>();


    public void addPolygon(Polygon polygon) {
        this.polygons.add(polygon);
    }
    public ArrayList<Polygon> getPolygons() { return polygons; }
}
