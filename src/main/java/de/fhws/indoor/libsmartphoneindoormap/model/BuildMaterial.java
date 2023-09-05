package de.fhws.indoor.libsmartphoneindoormap.model;

import android.icu.text.CaseMap;

public enum BuildMaterial {
    UNKNOWN,
    CONCRETE,
    WOOD,
    DRYWALL,
    GLASS,
    METAL,
    METALLIZED_GLAS;

    public static BuildMaterial parse(long id) {
        switch((int)id) {
            case 1: return BuildMaterial.CONCRETE;
            case 2: return BuildMaterial.WOOD;
            case 3: return BuildMaterial.DRYWALL;
            case 4: return BuildMaterial.GLASS;
            case 5: return BuildMaterial.METAL;
            case 6: return BuildMaterial.METALLIZED_GLAS;
            default: return BuildMaterial.UNKNOWN;
        }
    }
}
