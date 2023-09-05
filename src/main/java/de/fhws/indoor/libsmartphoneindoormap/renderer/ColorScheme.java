package de.fhws.indoor.libsmartphoneindoormap.renderer;

import android.graphics.Paint;

public class ColorScheme {
    int outlineColor;
    int outlineRemoveColor;
    int wallColor; int wallColorConcrete; int wallColorWood;
    int doorColor; int doorColorLocked;
    int stairColor;
    int unseenColor;
    int seenColor;
    int selectedColor;

    public ColorScheme(int outlineColor, int outlineRemoveColor, int wallColor, int wallColorConcrete, int wallColorWood, int doorColor, int doorColorLocked, int stairColor, int unseenColor, int seenColor, int selectedColor) {
        this.outlineColor = outlineColor;
        this.outlineRemoveColor = outlineRemoveColor;
        this.wallColor = wallColor;
        this.wallColorConcrete = wallColorConcrete;
        this.wallColorWood = wallColorWood;
        this.doorColor = doorColor;
        this.doorColorLocked = doorColorLocked;
        this.stairColor = stairColor;
        this.unseenColor = unseenColor;
        this.seenColor = seenColor;
        this.selectedColor = selectedColor;
    }
}
