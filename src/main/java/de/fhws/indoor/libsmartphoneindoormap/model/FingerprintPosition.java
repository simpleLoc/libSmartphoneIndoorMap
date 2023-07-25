package de.fhws.indoor.libsmartphoneindoormap.model;

public class FingerprintPosition extends Fingerprint {
    public Vec3 position;

    public FingerprintPosition(String name, int floorIdx, String floorName, boolean selected, boolean recorded, Vec3 position) {
        super(name, floorIdx, floorName, selected, recorded);
        this.position = position;
    }
}
