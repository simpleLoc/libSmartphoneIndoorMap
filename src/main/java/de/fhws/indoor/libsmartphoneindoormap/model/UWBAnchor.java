package de.fhws.indoor.libsmartphoneindoormap.model;

public class UWBAnchor {
    public String name;
    public String deviceId;
    public String shortDeviceId;
    public MacAddress bleMac;
    public Vec3 position = new Vec3();
    public Vec3 absolutePosition = new Vec3();

    public UWBAnchor(String name, String deviceId, MacAddress bleMac, Vec3 position, float floorAtHeight) {
        this.name = name;
        this.deviceId = deviceId;
        this.shortDeviceId = deviceId.substring(deviceId.length()-4);
        this.bleMac = bleMac;
        this.position = position;
        this.absolutePosition = new Vec3(position.x, position.y, position.z + floorAtHeight);
    }

    public boolean seen = false;
}
