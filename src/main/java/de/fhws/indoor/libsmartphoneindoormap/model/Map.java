package de.fhws.indoor.libsmartphoneindoormap.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.fhws.indoor.libsmartphoneindoormap.parser.MapSeenSerializer;

public class Map {
    public interface ChangedListener {
        void onChange();
    }

    private final ArrayList<ChangedListener> changedListeners = new ArrayList<>();
    private final ArrayList<Floor> floors = new ArrayList<>();
    private MapSeenSerializer serializer = null;

    public void addChangedListener(ChangedListener listener) {
        changedListeners.add(listener);
    }

    public void removeChangedListener(ChangedListener listener) {
        changedListeners.remove(listener);
    }

    public ArrayList<Floor> getFloors() {
        return floors;
    }

    public void addFloor(Floor floor) {
        floors.add(floor);
    }

    public HashMap<MacAddress, Beacon> getBeacons() {
        HashMap<MacAddress, Beacon> out = new HashMap<>();
        for (Floor f : floors) {
            out.putAll(f.getBeacons());
        }
        return out;
    }

    public HashMap<MacAddress, AccessPoint> getAccessPoints() {
        HashMap<MacAddress, AccessPoint> out = new HashMap<>();
        for (Floor f : floors) {
            out.putAll(f.getAccessPoints());
        }
        return out;
    }

    public void resetSeen(boolean value) {
        for (Floor floor : floors) {
            floor.resetSeen(value);
        }
        serializer.clearSeenStates();
        raiseChangedListeners();
    }

    public void setSerializer(MapSeenSerializer serializer) {
        this.serializer = serializer;
        this.serializer.loadSeenStates(this);
    }

    public void setSeenBeacon(String macStr) {
        setSeenBeacon(macStr, true);
    }

    public void setSeenBeacon(String macStr, boolean save) {
        MacAddress mac = new MacAddress(macStr);
        Optional<Beacon> beacon = floors.stream()
                .flatMap(floor -> floor.getBeacons().values().stream())
                .filter(b -> b.mac.equals(mac)).findFirst();
        beacon.ifPresent(b -> {
            if (!b.seen) {
                b.seen = true;
                if (serializer != null && save) {
                    serializer.saveSeenStateBeacon(macStr);
                }
                raiseChangedListeners();
            }
        });
    }


    public void setSeenUWB(String shortDeviceId) {
        setSeenUWB(shortDeviceId, true);
    }

    public void setSeenUWB(String shortDeviceId, boolean save) {
        Optional<UWBAnchor> anchor = floors.stream()
                .flatMap(floor -> floor.getUwbAnchors().values().stream())
                .filter(a -> a.shortDeviceId.equalsIgnoreCase(shortDeviceId)).findFirst();
        anchor.ifPresent(a -> {
            if (!a.seen) {
                a.seen = true;
                if (serializer != null && save) {
                    serializer.saveSeenStateUWB(shortDeviceId);
                }
                raiseChangedListeners();
            }
        });
    }

    public void setSeenWiFi(String macStr) {
        setSeenWiFi(macStr, true);
    }

    public void setSeenWiFi(String macStr, boolean save) {
        MacAddress mac = new MacAddress(macStr);
        Optional<AccessPoint> ap = floors.stream()
                .flatMap(floor -> floor.getAccessPoints().values().stream())
                .filter(a -> a.mac.equals(mac)).findFirst();
        ap.ifPresent(a -> {
            if (!a.seen) {
                a.seen = true;
                if (serializer != null && save) {
                    serializer.saveSeenStateWiFi(macStr);
                }
                raiseChangedListeners();
            }
        });
    }


    public void setSeenFtm(String macStr) { setSeenFtm(macStr, true); }

    public void setSeenFtm(String macStr, boolean save) {
        MacAddress mac = new MacAddress(macStr);
        Optional<AccessPoint> ap = floors.stream()
                .flatMap(floor -> floor.getAccessPoints().values().stream())
                .filter(a -> a.mac.equals(mac)).findFirst();
        ap.ifPresent(a -> {
            if (!a.ftmSeen) {
                a.ftmSeen = true;
                if (serializer != null && save) {
                    serializer.saveSeenStateFtm(macStr);
                }
                raiseChangedListeners();
            }
        });
    }

    private void raiseChangedListeners() {
        changedListeners.forEach(ChangedListener::onChange);
    }

    public List<UWBAnchor> getUwbAnchors() {
        return floors.stream()
                .flatMap(floor -> floor.getUwbAnchors().values().stream())
                .collect(Collectors.toList());
    }
}
