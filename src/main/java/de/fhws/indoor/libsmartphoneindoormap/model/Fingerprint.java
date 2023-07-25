package de.fhws.indoor.libsmartphoneindoormap.model;

public class Fingerprint {
    public String name;

    public int floorIdx;
    public String floorName;

    public boolean selected;
    public boolean recorded;

    public Fingerprint(String name, int floorIdx, String floorName, boolean selected, boolean recorded) {
        this.name = name;
        this.floorIdx = floorIdx;
        this.floorName = floorName;
        this.selected = selected;
        this.recorded = recorded;
    }
}
