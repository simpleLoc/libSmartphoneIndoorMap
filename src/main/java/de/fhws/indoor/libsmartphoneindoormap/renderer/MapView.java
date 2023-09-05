package de.fhws.indoor.libsmartphoneindoormap.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inspector.StaticInspectionCompanionProvider;

import androidx.annotation.RequiresApi;

import java.lang.invoke.ConstantCallSite;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Queue;

import de.fhws.indoor.libsmartphoneindoormap.R;
import de.fhws.indoor.libsmartphoneindoormap.model.AccessPoint;
import de.fhws.indoor.libsmartphoneindoormap.model.Beacon;
import de.fhws.indoor.libsmartphoneindoormap.model.BuildMaterial;
import de.fhws.indoor.libsmartphoneindoormap.model.Fingerprint;
import de.fhws.indoor.libsmartphoneindoormap.model.FingerprintPath;
import de.fhws.indoor.libsmartphoneindoormap.model.FingerprintPosition;
import de.fhws.indoor.libsmartphoneindoormap.model.Floor;
import de.fhws.indoor.libsmartphoneindoormap.model.Map;
import de.fhws.indoor.libsmartphoneindoormap.model.Outline;
import de.fhws.indoor.libsmartphoneindoormap.model.Stair;
import de.fhws.indoor.libsmartphoneindoormap.model.UWBAnchor;
import de.fhws.indoor.libsmartphoneindoormap.model.Vec2;
import de.fhws.indoor.libsmartphoneindoormap.model.Vec3;
import de.fhws.indoor.libsmartphoneindoormap.model.Wall;

public class MapView extends View {
    // ColorScheme
    private boolean initialized = false;
    private Paint outlinePaint;
    private Paint outlineRemovePaint;
    private Paint stairPaint;
    private Paint wallPaintOther;
    private Paint wallPaintConcrete;
    private Paint wallPaintWood;
    private Paint doorPaint;
    private Paint doorLockedPaint;
    private Paint unseenPaint;
    private Paint seenPaint;
    private Paint selectedPaint;

    // this is required to draw small texts with text sizes around 1dp
    private final float textScale = 16.f;

    private static final int INVALID_POINTER_ID = 1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private final TwoFingerGestureDetector mTwoFingerGestureDetector;

    private float mLastTouchX;
    private float mLastTouchY;

    private final Matrix mModelMatrix = new Matrix();
    private final Matrix mViewMatrix = new Matrix();
    private final Matrix mMVMatrix = new Matrix();
    private final Matrix mMVMatrixInverse = new Matrix();

    private Map map = null;
    private Floor floor = null;
    private ViewConfig viewConfig = new ViewConfig();

    private static final long mLongPressDuration = 1000;   // in ms
    private static final float mMaxPressMoveDistance = 10; // in pixel
    private Vec2 mTouchDownScreenPos = new Vec2();
    private ArrayList<IMapEventListener> eventListeners = new ArrayList<>();

    private Fingerprint highlightFingerprint = null;


    public static class ViewConfig {
        public boolean showWiFi = true;
        public boolean showBluetooth = true;
        public boolean showUWB = true;
        public boolean showFingerprint = true;
    }


    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTwoFingerGestureDetector = new TwoFingerGestureDetector();

        mModelMatrix.setScale(10, 10);
    }

    public Fingerprint findNearestFingerprint(Vec2 mapPosition, float maxSearchRadius) {
        if (floor == null) return null;

        Fingerprint nearest = null;
        float distance = maxSearchRadius;
        for (Fingerprint fingerprint : floor.getFingerprints().values()) {
            if (fingerprint instanceof FingerprintPosition) {
                FingerprintPosition fpPos = (FingerprintPosition) fingerprint;
                float d = (float) new Vec2(fpPos.position.x, fpPos.position.y).sub(mapPosition).length();
                if (d < distance) {
                    distance = d;
                    nearest = fingerprint;
                }
            } else if (fingerprint instanceof FingerprintPath) {
                FingerprintPath fpPath = (FingerprintPath) fingerprint;
                FingerprintPosition last = null;
                for (String fpName : fpPath.fingerprintNames) {
                    FingerprintPosition fp = (FingerprintPosition) floor.getFingerprints().get(fpName);
                    if (last != null && fp != null) {
                        Vec2 lineCenter = new Vec2(last.position.x, last.position.y).add(new Vec2(fp.position.x, fp.position.y)).mul(0.5f);
                        float d = (float) lineCenter.sub(mapPosition).length();
                        if (d < distance) {
                            distance = d;
                            nearest = fingerprint;
                            break;
                        }
                    }
                    last = fp;
                }
            }
        }
        return nearest;
    }

    public void addEventListener(IMapEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(IMapEventListener listener) {
        eventListeners.remove(listener);
    }

    public void setViewConfig(ViewConfig viewConfig) {
        this.viewConfig = viewConfig;
        invalidate();
    }

    public void setColorScheme(ColorScheme colorScheme) {
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(getResources().getColor(colorScheme.outlineColor, getContext().getTheme()));
        outlineRemovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlineRemovePaint.setColor(getResources().getColor(colorScheme.outlineRemoveColor, getContext().getTheme()));

        stairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stairPaint.setColor(getResources().getColor(colorScheme.stairColor, getContext().getTheme()));

        wallPaintOther = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallPaintConcrete = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallPaintWood = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallPaintOther.setColor(getResources().getColor(colorScheme.wallColor, getContext().getTheme()));
        wallPaintConcrete.setColor(getResources().getColor(colorScheme.wallColorConcrete, getContext().getTheme()));
        wallPaintWood.setColor(getResources().getColor(colorScheme.wallColorWood, getContext().getTheme()));
        doorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        doorPaint.setColor(getResources().getColor(colorScheme.doorColor, getContext().getTheme()));
        doorLockedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        doorLockedPaint.setColor(getResources().getColor(colorScheme.doorColorLocked, getContext().getTheme()));

        unseenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unseenPaint.setColor(getResources().getColor(colorScheme.unseenColor, getContext().getTheme()));
        unseenPaint.setTextSize(8);

        seenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        seenPaint.setColor(getResources().getColor(colorScheme.seenColor, getContext().getTheme()));
        seenPaint.setTextSize(8);

        selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedPaint.setColor(getResources().getColor(colorScheme.selectedColor, getContext().getTheme()));
        selectedPaint.setTextSize(16);

        initialized = true;
        invalidate();
    }

    public void setMap(Map map) {
        this.map = map;
        if (this.map == null) { return; }

        this.map.addChangedListener(this::invalidate);

        Optional<Floor> first = map.getFloors().stream().findFirst();
        floor = first.orElse(null);
        invalidate();
    }

    public void selectFloor(int idx) {
        if (map == null) { return; }
        try {
            floor = map.getFloors().get(idx);
            invalidate();
        } catch (IndexOutOfBoundsException exception) {
            Log.e("MapView", exception.toString());
            exception.printStackTrace();
            // floor index not available
        }
    }

    public Floor getCurrentFloor() {
        return floor;
    }

    public void setHighlightFingerprint(Fingerprint fingerprint) {
        this.highlightFingerprint = fingerprint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(initialized) {
            mViewMatrix.reset();
            mViewMatrix.preTranslate(getWidth()/2f, getHeight()/2f);
            mViewMatrix.preScale(1, -1);
            updateMVMatrix();

            canvas.setMatrix(mMVMatrix);
            super.onDraw(canvas);

            if (map == null || floor == null) { return; }

            drawOutline(floor, canvas);

            floor.getWalls().forEach(wall -> drawWall(wall, canvas));
            floor.getStairs().forEach(stair -> drawStair(stair, canvas));
            if(viewConfig.showBluetooth) {
                floor.getBeacons().values().forEach(beacon -> drawBeacon(beacon, canvas));
            }
            if(viewConfig.showUWB) {
                floor.getUwbAnchors().values().forEach(uwbAnchor -> drawUWB(uwbAnchor, canvas));
            }
            if(viewConfig.showWiFi) {
                floor.getAccessPoints().values().forEach(accessPoint -> drawAP(accessPoint, canvas));
            }

            if(viewConfig.showFingerprint) {
                floor.getFingerprints().values().forEach(fingerprint -> drawFP(fingerprint, canvas));
                if (highlightFingerprint != null) {
                    drawFP(highlightFingerprint, canvas);
                }
            }
        }
    }

    private void updateMVMatrix() {
        mMVMatrix.reset();
        mMVMatrix.postConcat(mModelMatrix);
        mMVMatrix.postConcat(mViewMatrix);
        mMVMatrix.invert(mMVMatrixInverse);
    }

    private void drawStair(Stair stair, Canvas canvas) {
        for(Stair.StairPart stairPart : stair.parts) {
            stairPaint.setStrokeWidth(stairPart.w);
            canvas.drawLine(stairPart.p0.x, stairPart.p0.y, stairPart.p1.x, stairPart.p1.y, stairPaint);
        }
    }

    private void drawOutline(Floor floor, Canvas canvas) {
        for(Outline.Polygon outlinePolygon : floor.getOutline().getPolygons()) {
            if(outlinePolygon.method == Outline.PolygonMethod.ADD) {
                Path outlinePath = new Path();
                outlinePath.reset();
                for(int i = 0; i < outlinePolygon.points.size(); ++i) {
                    Vec3 pt = outlinePolygon.points.get(i);
                    if(i == 0) {
                        outlinePath.moveTo(pt.x, pt.y);
                    } else {
                        outlinePath.lineTo(pt.x, pt.y);
                    }
                }
                canvas.drawPath(outlinePath, outlinePaint);
            }
        }
        for(Outline.Polygon outlinePolygon : floor.getOutline().getPolygons()) {
            if(outlinePolygon.method == Outline.PolygonMethod.REMOVE) {
                Path outlinePath = new Path();
                outlinePath.reset();
                for(int i = 0; i < outlinePolygon.points.size(); ++i) {
                    Vec3 pt = outlinePolygon.points.get(i);
                    if(i == 0) {
                        outlinePath.moveTo(pt.x, pt.y);
                    } else {
                        outlinePath.lineTo(pt.x, pt.y);
                    }
                }
                canvas.drawPath(outlinePath, outlineRemovePaint);
            }
        }
    }

    private void drawWall(Wall wall, Canvas canvas) {
        class DrawSegment {
            float start;
            float end;
            Wall.Door door;
            Paint paint;

            public DrawSegment(float start, float end, Wall.Door door, Paint paint) {
                this.start = start;
                this.end = end;
                this.door = door;
                this.paint = paint;
            }
            public boolean contains(DrawSegment other) {
                return (start <= other.start && end >= other.end);
            }
        }
        class DrawSegments {
            final ArrayList<DrawSegment> segments = new ArrayList<>();
            public void add(DrawSegment newSegment) {
                DrawSegment intersectSegment = segments.stream().filter(seg -> seg.contains(newSegment)).findAny().orElse(null);
                if(intersectSegment != null) {
                    segments.remove(intersectSegment);
                    segments.add(new DrawSegment(intersectSegment.start, newSegment.start, intersectSegment.door, intersectSegment.paint));
                    segments.add(newSegment);
                    segments.add(new DrawSegment(newSegment.end, intersectSegment.end, intersectSegment.door, intersectSegment.paint));
                } else {
                    segments.add(newSegment);
                }
            }
            public void sort() {
                segments.sort((a, b) -> Float.compare(a.start, b.start));
            }
        }

        Paint wallPaint = null;
        if(wall.material == BuildMaterial.CONCRETE) { wallPaint = wallPaintConcrete; }
        else if(wall.material == BuildMaterial.WOOD) { wallPaint = wallPaintWood; }
        else { wallPaint = wallPaintOther; }
        wallPaint.setStrokeWidth(wall.thickness);

        Vec2 wallDir = wall.p1.sub(wall.p0);
        float wallLen = (float)wallDir.length();
        DrawSegments drawSegments = new DrawSegments();
        drawSegments.add(new DrawSegment(0, 1, null, wallPaint));
        for(Wall.Door door : wall.doors) {
            float widthAlpha = door.width / wallLen;
            Paint drawPaint = (door.lockType == Wall.DoorLockType.OPEN) ? doorPaint : doorLockedPaint;
            if(door.leftRight == false) {
                drawSegments.add(new DrawSegment(door.x01, door.x01 + widthAlpha, door, drawPaint));
            } else {
                drawSegments.add(new DrawSegment(door.x01 - widthAlpha, door.x01, door, drawPaint));
            }
        }
        drawSegments.sort();

        for(DrawSegment segment : drawSegments.segments) {
            Vec2 startPt = wall.p0.add(wallDir.mul(segment.start));
            Vec2 endPt = wall.p0.add(wallDir.mul(segment.end));
            canvas.drawLine(startPt.x, startPt.y, endPt.x, endPt.y, segment.paint);
            if(segment.door != null) {
                Vec2 doorPerpDir = endPt.sub(startPt).getPerpendicular();
                if(segment.door.insideOut) { doorPerpDir = doorPerpDir.mul(-1); }
                if(segment.door.leftRight) {
                    Vec2 tmp = endPt; endPt = startPt; startPt = tmp;
                }
                canvas.drawLine(startPt.x, startPt.y, startPt.x + doorPerpDir.x, startPt.y + doorPerpDir.y, segment.paint);
                canvas.drawLine(startPt.x + doorPerpDir.x, startPt.y + doorPerpDir.y, endPt.x, endPt.y, segment.paint);
            }
        }
    }

    private void drawBeacon(Beacon beacon, Canvas canvas) {
        final float size = (float) Math.sqrt(0.15 * 0.15 * 2);
        Vec2 dir = new Vec2(0, size);
        final Vec2 p0 = new Vec2(beacon.position.x, beacon.position.y).add(dir);
        dir = dir.rotated(Math.PI/2);
        final Vec2 p1 = new Vec2(beacon.position.x, beacon.position.y).add(dir);
        dir = dir.rotated(Math.PI/2);
        final Vec2 p2 = new Vec2(beacon.position.x, beacon.position.y).add(dir);
        dir = dir.rotated(Math.PI/2);
        final Vec2 p3 = new Vec2(beacon.position.x, beacon.position.y).add(dir);

        Path path = new Path();
        path.moveTo(p0.x, p0.y);
        path.lineTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.close();
        canvas.drawPath(path, beacon.seen ? seenPaint : unseenPaint);

        if (!beacon.seen) {
            // flip y axis to draw text
            canvas.scale(1/textScale, -1/textScale);
            canvas.drawText(beacon.name,
                    (beacon.position.x + 0.15f) * textScale,
                    (-beacon.position.y - 0.15f) * textScale,
                    unseenPaint);
            canvas.scale(textScale, -textScale);
        }
    }

    private void drawUWB(UWBAnchor uwbAnchor, Canvas canvas) {
        final float size = 0.15f;
        canvas.drawRect(uwbAnchor.position.x - size, uwbAnchor.position.y + size,
                uwbAnchor.position.x + size, uwbAnchor.position.y - size,
                uwbAnchor.seen ? seenPaint : unseenPaint);

        // flip y axis to draw text
        if (!uwbAnchor.seen) {
            canvas.scale(1/textScale, -1/textScale);
            float height = unseenPaint.descent() - unseenPaint.ascent();
            canvas.drawText(uwbAnchor.name,
                    (uwbAnchor.position.x + 0.15f) * textScale,
                    -uwbAnchor.position.y * textScale + height,
                    unseenPaint);
            canvas.scale(textScale, -textScale);
        }
    }

    private void drawAP(AccessPoint accessPoint, Canvas canvas) {
        canvas.drawCircle(accessPoint.position.x, accessPoint.position.y, 0.15f,
                accessPoint.seen ? seenPaint : unseenPaint);
        if(accessPoint.hasFtm) {
            canvas.drawCircle(accessPoint.position.x, accessPoint.position.y, 0.075f,
                    accessPoint.ftmSeen ? seenPaint : unseenPaint);
        }

        // flip y axis to draw text
        if (!accessPoint.seen) {
            canvas.scale(1/textScale, -1/textScale);
            canvas.drawText(accessPoint.name,
                    accessPoint.position.x * textScale - unseenPaint.measureText(accessPoint.name),
                    (-accessPoint.position.y - 0.15f) * textScale,
                    unseenPaint);
            canvas.scale(textScale, -textScale);
        }
    }

    private void drawFP(Fingerprint fingerprint, Canvas canvas) {
        if (fingerprint instanceof FingerprintPosition) {
            drawFPPosition((FingerprintPosition) fingerprint, canvas);
        } else if (fingerprint instanceof FingerprintPath) {
            drawFPPath((FingerprintPath) fingerprint, canvas);
        }
    }

    private void drawFPPosition(FingerprintPosition fingerprint, Canvas canvas) {
        Paint curPaint = fingerprint.recorded ? seenPaint : unseenPaint;
        curPaint = fingerprint.selected ? selectedPaint : curPaint;
        Paint.Style prevStyle = curPaint.getStyle();

        canvas.drawCircle(fingerprint.position.x, fingerprint.position.y, 0.10f, curPaint);
        curPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(fingerprint.position.x, fingerprint.position.y, 0.17f, curPaint);

        // reset paint style
        curPaint.setStyle(prevStyle);

        // flip y axis to draw text
        canvas.scale(1/textScale, -1/textScale);
        canvas.drawText(fingerprint.name,
                fingerprint.position.x * textScale - curPaint.measureText(fingerprint.name),
                (-fingerprint.position.y - 0.15f) * textScale,
                curPaint);
        canvas.scale(textScale, -textScale);
    }

    private void drawFPPath(FingerprintPath fingerprint, Canvas canvas) {
        Paint curPaint = fingerprint.recorded ? seenPaint : unseenPaint;
        curPaint = fingerprint.selected ? selectedPaint : curPaint;
        float prevStrokeWidth = curPaint.getStrokeWidth();

        curPaint.setStrokeWidth(0.1f);

        Vec2 center = new Vec2();
        int cnt = 0;
        FingerprintPosition last = null;
        for (String fpName : fingerprint.fingerprintNames) {
            FingerprintPosition fp = (FingerprintPosition) floor.getFingerprints().get(fpName);
            if (fp != null) {
                center = center.add(new Vec2(fp.position.x, fp.position.y));
                cnt++;
                if (last != null) {
                    canvas.drawLine(last.position.x, last.position.y, fp.position.x, fp.position.y, curPaint);
                }
            }
            last = fp;
        }

        center = center.mul(1.0f / cnt);

        // reset paint style
        curPaint.setStrokeWidth(prevStrokeWidth);

        // flip y axis to draw text
        canvas.scale(1/textScale, -1/textScale);
        canvas.drawText(fingerprint.name,
                center.x * textScale - curPaint.measureText(fingerprint.name) / 2,
                (-center.y - 0.15f) * textScale,
                curPaint);
        canvas.scale(textScale, -textScale);
    }

    private void raiseOnTap(Vec2 mapPosition) {
        for (IMapEventListener listener : eventListeners) {
            listener.onTap(mapPosition);
        }
    }

    private void raiseOnLongPress(Vec2 mapPosition) {
        for (IMapEventListener listener : eventListeners) {
            listener.onLongPress(mapPosition);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mTwoFingerGestureDetector.onTouchEvent(ev)) { return true; } // rotation scale detector consumed event

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                float[] vec = {x, y};
                mMVMatrixInverse.mapPoints(vec);

                mLastTouchX = vec[0];
                mLastTouchY = vec[1];
                mTouchDownScreenPos = new Vec2(x, y);

                // Save the ID of this pointer
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);

                float[] preTransformPoint = {ev.getX(pointerIndex), ev.getY(pointerIndex)};
                mMVMatrixInverse.mapPoints(preTransformPoint);

                final float dx = preTransformPoint[0] - mLastTouchX;
                final float dy = preTransformPoint[1] - mLastTouchY;

                Matrix offset = new Matrix();
                offset.postTranslate(dx, dy);
                mModelMatrix.preConcat(offset);
                updateMVMatrix();

                float[] postTransformPoint = {ev.getX(pointerIndex), ev.getY(pointerIndex)};
                mMVMatrixInverse.mapPoints(postTransformPoint);
                mLastTouchX = postTransformPoint[0];
                mLastTouchY = postTransformPoint[1];

                invalidate();
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                float[] vec = {x, y};
                mMVMatrixInverse.mapPoints(vec);
                float pointerMoveDist = (float)(new Vec2(x, y).sub(mTouchDownScreenPos)).length();
                if (pointerMoveDist < mMaxPressMoveDistance) {
                    if (ev.getEventTime() - ev.getDownTime() < mLongPressDuration) {
                        raiseOnTap(new Vec2(vec[0], vec[1]));
                    } else {
                        raiseOnLongPress(new Vec2(vec[0], vec[1]));
                    }
                }

                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                // Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    final float x = ev.getX(newPointerIndex);
                    final float y = ev.getY(newPointerIndex);
                    float[] vec = {x, y};
                    mMVMatrixInverse.mapPoints(vec);
                    mLastTouchX = vec[0];
                    mLastTouchY = vec[1];
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                } else {
                    final float x = ev.getX(mActivePointerId);
                    final float y = ev.getY(mActivePointerId);
                    float[] vec = {x, y};
                    mMVMatrixInverse.mapPoints(vec);
                    mLastTouchX = vec[0];
                    mLastTouchY = vec[1];
                }
                break;
            }
        }
        return true;
    }

    private class TwoFingerGestureDetector {
        private static final int INVALID_POINTER_ID = -1;
        private float x1, y1, x2, y2;
        private int ptrID1 = INVALID_POINTER_ID, ptrID2 = INVALID_POINTER_ID;

        public TwoFingerGestureDetector(){
        }

        public boolean onTouchEvent(MotionEvent event){
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    ptrID1 = event.getPointerId(event.getActionIndex());
                    break;
                case MotionEvent.ACTION_POINTER_DOWN: {
                    ptrID2 = event.getPointerId(event.getActionIndex());
                    setCurrentFingerState(event);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (ptrID1 != INVALID_POINTER_ID && ptrID2 != INVALID_POINTER_ID && ptrID1 != ptrID2) {
                        float nx2, ny2, nx1, ny1;
                        nx1 = event.getX(event.findPointerIndex(ptrID1));
                        ny1 = event.getY(event.findPointerIndex(ptrID1));
                        nx2 = event.getX(event.findPointerIndex(ptrID2));
                        ny2 = event.getY(event.findPointerIndex(ptrID2));
                        float[] vec = {nx1, ny1, nx2, ny2};
                        mMVMatrixInverse.mapPoints(vec);
                        nx1 = vec[0];
                        ny1 = vec[1];
                        nx2 = vec[2];
                        ny2 = vec[3];
                        float cx = (x1 + x2)/2, cy = (y1 + y2)/2;
                        float ncx = (nx2 + nx1)/2, ncy = (ny2 + ny1)/2;

                        float rotation = angleBetweenLines(x1, y1, x2, y2, nx1, ny1, nx2, ny2);
                        float scale = Math.max(0.01f, Math.min(10.0f, scaleBetweenLines(x1, y1, x2, y2, nx1, ny1, nx2, ny2)));
                        float dx = ncx - cx, dy = ncy - cy;


                        Matrix offset = new Matrix();
                        offset.postTranslate(-cx, -cy);
                        offset.postRotate(rotation);
                        offset.postScale(scale, scale);
                        offset.postTranslate(cx, cy);
                        offset.postTranslate(dx, dy);
                        mModelMatrix.preConcat(offset);
                        invalidate();

                        updateMVMatrix();
                        setCurrentFingerState(event);

                        return true;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                    ptrID1 = INVALID_POINTER_ID;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    ptrID2 = INVALID_POINTER_ID;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    ptrID1 = INVALID_POINTER_ID;
                    ptrID2 = INVALID_POINTER_ID;
                    break;
            }
            return false;
        }

        private float angleBetweenLines (float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY) {
            float angle1 = (float) Math.atan2( (fY - sY), (fX - sX) );
            float angle2 = (float) Math.atan2( (nfY - nsY), (nfX - nsX) );

            float angle = ((float)Math.toDegrees(angle2 - angle1)) % 360;
            if (angle < -180.f) angle += 360.0f;
            if (angle > 180.f) angle -= 360.0f;
            return angle;
        }

        private float scaleBetweenLines(float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY) {
            return lineLength(nfX, nfY, nsX, nsY) / lineLength(fX, fY, sX, sY);
        }

        private float lineLength(float ox, float oy, float tx, float ty) {
            float dx = tx - ox;
            float dy = ty - oy;
            return (float) Math.sqrt(dx*dx + dy*dy);
        }

        private void setCurrentFingerState(MotionEvent event) {
            x1 = event.getX(event.findPointerIndex(ptrID1));
            y1 = event.getY(event.findPointerIndex(ptrID1));
            x2 = event.getX(event.findPointerIndex(ptrID2));
            y2 = event.getY(event.findPointerIndex(ptrID2));
            float[] vec = {x1, y1, x2, y2};
            mMVMatrixInverse.mapPoints(vec);
            x1 = vec[0];
            y1 = vec[1];
            x2 = vec[2];
            y2 = vec[3];
        }
    }
}
