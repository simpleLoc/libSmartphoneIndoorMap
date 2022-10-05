package de.fhws.indoor.libsmartphoneindoormap.renderer;

import de.fhws.indoor.libsmartphoneindoormap.model.Vec2;

public interface IMapEventListener {
    void onTap(Vec2 mapPosition);
    void onLongPress(Vec2 mapPosition);
    void onDragStart(Vec2 mapPosition);
    void onDragEnd(Vec2 mapPosition);
}
