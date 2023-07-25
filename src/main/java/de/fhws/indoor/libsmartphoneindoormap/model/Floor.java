package de.fhws.indoor.libsmartphoneindoormap.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Floor {
    private int idx;
    private String name;

    private float atHeight;
    private float height;

    private final ArrayList<Wall> walls = new ArrayList<>();
    private final HashMap<MacAddress, AccessPoint> accessPoints = new HashMap<>();
    private final HashMap<String, UWBAnchor> uwbAnchors = new HashMap<>();
    private final HashMap<MacAddress, Beacon> beacons = new HashMap<>();
    private final HashMap<String, Fingerprint> fingerprints = new HashMap<>();


    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAtHeight() {
        return atHeight;
    }

    public void setAtHeight(float atHeight) {
        this.atHeight = atHeight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public ArrayList<Wall> getWalls() {
        return walls;
    }

    public void addWall(Wall w) {
        walls.add(w);
    }

    public HashMap<MacAddress, AccessPoint> getAccessPoints() {
        return accessPoints;
    }

    public void addAP(AccessPoint ap) {
        accessPoints.put(ap.mac, ap);
    }

    public HashMap<String, UWBAnchor> getUwbAnchors() {
        return uwbAnchors;
    }

    public void addUWB(UWBAnchor uwb) {
        uwbAnchors.put(uwb.shortDeviceId, uwb);
    }

    public HashMap<MacAddress, Beacon> getBeacons() {
        return beacons;
    }

    public void addBeacon(Beacon b) {
        beacons.put(b.mac, b);
    }

    public HashMap<String, Fingerprint> getFingerprints() {
        return fingerprints;
    }

    public void addFingerprint(Fingerprint fingerprint) {
        fingerprints.put(fingerprint.name, fingerprint);
    }

    public void resetSeen(boolean value) {
        for (AccessPoint ap : accessPoints.values()) {
            ap.seen = value;
        }

        for (UWBAnchor uwbAnchor : uwbAnchors.values()) {
            uwbAnchor.seen = value;
        }

        for (Beacon beacon : beacons.values()) {
            beacon.seen = value;
        }
    }
}
