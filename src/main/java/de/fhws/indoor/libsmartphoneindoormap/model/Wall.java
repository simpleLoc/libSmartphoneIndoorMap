package de.fhws.indoor.libsmartphoneindoormap.model;

import java.util.ArrayList;

public class Wall {
    public Vec2 p0 = new Vec2();
    public Vec2 p1 = new Vec2();
    public float thickness;
    public BuildMaterial material;
    public final ArrayList<Door> doors = new ArrayList<>();

    public void addDoor(Door door) {
        this.doors.add(door);
    }

    public enum DoorLockType {
        UNKNOWN,
        LOCKED,
        CAN_OPEN,
        OPEN;

        public static DoorLockType parse(long id) {
            switch((int)id) {
                case 1: return LOCKED;
                case 2: return CAN_OPEN;
                case 3: return OPEN;
                default: return UNKNOWN;
            }
        }
    }

    public static class Door {
        public float x01;
        public float width;
        public float height;
        public boolean insideOut;
        public boolean leftRight;
        public DoorLockType lockType;
    }

}
