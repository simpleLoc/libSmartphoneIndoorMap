package de.fhws.indoor.libsmartphoneindoormap.renderer;

public interface IMapEventListener {
    void onTap(float[] mapPosition);
    void onLongPress(float[] mapPosition);
    void onDragStart(float[] mapPosition);
    void onDragEnd(float[] mapPosition);
}
