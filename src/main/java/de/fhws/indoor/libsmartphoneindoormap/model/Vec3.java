package de.fhws.indoor.libsmartphoneindoormap.model;

import androidx.annotation.NonNull;

import java.util.Locale;

public class Vec3 {
    public float x = 0;
    public float y = 0;
    public float z = 0;

    public Vec3() {
    }

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @NonNull
    public String toString() {
        return String.format(Locale.US,"%.3f %.3f %.3f", x, y, z);
    }
}
