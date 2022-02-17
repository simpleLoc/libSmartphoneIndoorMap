package de.fhws.indoor.libsmartphoneindoormap.model;

public class AccessPoint {
    public String name;
    public MacAddress mac;
    public Vec3 position = new Vec3();
    public RadioModel mdl = new RadioModel();

    public boolean seen = false;
}
