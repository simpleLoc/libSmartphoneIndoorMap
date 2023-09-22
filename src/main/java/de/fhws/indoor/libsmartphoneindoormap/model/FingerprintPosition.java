package de.fhws.indoor.libsmartphoneindoormap.model;

public class FingerprintPosition extends Fingerprint {
    public int floorIdx;
    public String floorName;

    public Vec3 position;

    public FingerprintPosition(String name, int floorIdx, String floorName, boolean selected, boolean recorded, Vec3 position) {
        super(name, selected, recorded);
        this.floorIdx = floorIdx;
        this.floorName = floorName;
        this.position = position;
    }
}
