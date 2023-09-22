package de.fhws.indoor.libsmartphoneindoormap.model;

public class Fingerprint {
    public String name;
    public boolean selected;
    public boolean recorded;

    public Fingerprint(String name, boolean selected, boolean recorded) {
        this.name = name;
        this.selected = selected;
        this.recorded = recorded;
    }
}
