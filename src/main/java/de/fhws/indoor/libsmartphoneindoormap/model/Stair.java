package de.fhws.indoor.libsmartphoneindoormap.model;

import android.renderscript.Matrix2f;

import java.util.ArrayList;

public class Stair {

    public static class StairPart {
        public boolean connect = false;
        public final Vec3 p0 = new Vec3();
        public final Vec3 p1 = new Vec3();
        public float w;
    }

    public final ArrayList<StairPart> parts = new ArrayList<>();

}
