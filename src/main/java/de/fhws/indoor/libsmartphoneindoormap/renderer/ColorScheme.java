package de.fhws.indoor.libsmartphoneindoormap.renderer;

import android.graphics.Paint;

public class ColorScheme {
    int wallColor;
    int unseenColor;
    int seenColor;

    public ColorScheme(int wallColor, int unseenColor, int seenColor) {
        this.wallColor = wallColor;
        this.unseenColor = unseenColor;
        this.seenColor = seenColor;
    }
}
