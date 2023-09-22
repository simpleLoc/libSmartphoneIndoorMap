package de.fhws.indoor.libsmartphoneindoormap.model;

import java.util.ArrayList;
import java.util.Locale;

public class FingerprintPath extends Fingerprint {
    public ArrayList<Integer> floorIdxs = new ArrayList<Integer>();
    public  ArrayList<String> floorNames = new ArrayList<String>();
    public ArrayList<String> fingerprintNames = new ArrayList<>();
    public ArrayList<Vec3> positions = new ArrayList<>();

    public FingerprintPath(boolean selected, boolean recorded, ArrayList<FingerprintPosition> fingerprints) {
        super("", selected, recorded);

        for (FingerprintPosition point : fingerprints) {
            floorIdxs.add(point.floorIdx);
            floorNames.add(point.floorName);
            fingerprintNames.add(point.name);
            positions.add(point.position);
        }

        name = String.join(";", fingerprintNames);
    }

    public static String positionsToString(ArrayList<Vec3> positions) {
        StringBuilder concatPositions = new StringBuilder("[");
        for (Vec3 pos : positions) {
            concatPositions.append(String.format(Locale.US, "\"%s\", ", pos.toString()));
        }
        // delete last comma and insert closing bracket
        concatPositions.delete(concatPositions.length() - 2, concatPositions.length());
        concatPositions.append("]");

        return  concatPositions.toString();
    }
}
