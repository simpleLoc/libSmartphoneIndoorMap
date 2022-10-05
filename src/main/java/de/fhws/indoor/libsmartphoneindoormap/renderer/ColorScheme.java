package de.fhws.indoor.libsmartphoneindoormap.renderer;

import android.graphics.Paint;

public class ColorScheme {
    int wallColor;
    int unseenColor;
    int seenColor;
    int selectedColor;

    public ColorScheme(int wallColor, int unseenColor, int seenColor, int selectedColor) {
        this.wallColor = wallColor;
        this.unseenColor = unseenColor;
        this.seenColor = seenColor;
        this.selectedColor = selectedColor;
    }
}
