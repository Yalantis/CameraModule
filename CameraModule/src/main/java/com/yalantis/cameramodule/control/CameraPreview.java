/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Zillow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.yalantis.cameramodule.control;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.yalantis.cameramodule.interfaces.FocusCallback;
import com.yalantis.cameramodule.interfaces.KeyEventsListener;
import com.yalantis.cameramodule.model.FocusMode;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private static final int DISPLAY_ORIENTATION = 90;
    private static final float FOCUS_AREA_SIZE = 75f;
    private static final float STROKE_WIDTH = 5f;
    private static final float FOCUS_AREA_FULL_SIZE = 2000f;
    private static final int ACCURACY = 3;

    private Activity activity;
    private Camera camera;

    private ImageView canvasFrame;
    private Canvas canvas;
    private Paint paint;
    private FocusMode focusMode = FocusMode.AUTO;

    private boolean hasAutoFocus;
    private boolean focusing;
    private boolean focused;
    private float focusKoefW;
    private float focusKoefH;
    private float prevScaleFactor;
    private FocusCallback focusCallback;
    private Rect tapArea;
    private KeyEventsListener keyEventsListener;

    public CameraPreview(Activity activity, Camera camera, ImageView canvasFrame, FocusCallback focusCallback, KeyEventsListener keyEventsListener) {
        super(activity);
        this.activity = activity;
        this.camera = camera;
        this.canvasFrame = canvasFrame;
        this.focusCallback = focusCallback;
        this.keyEventsListener = keyEventsListener;

        List<String> supportedFocusModes = camera.getParameters().getSupportedFocusModes();
        hasAutoFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO);

        initHolder();
    }

    private void initHolder() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        SurfaceHolder holder = getHolder();
        if (holder != null) {
            holder.addCallback(this);
            holder.setKeepScreenOn(true);
        }
    }

    private void initFocusDrawingTools(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(STROKE_WIDTH);
        canvasFrame.setImageBitmap(bitmap);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Timber.d("surfaceCreated");
        // The Surface has been created, now tell the camera where to draw the preview.
        startPreview(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Timber.d("surfaceDestroyed");
        stopPreview();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Timber.d("surfaceChanged(%1d, %2d)", width, height);
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        initFocusDrawingTools(width, height);
        initFocusKoefs(width, height);
        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        stopPreview();

        startPreview(holder);
        setOnTouchListener(new CameraTouchListener());
    }

    private void initFocusKoefs(float width, float height) {
        focusKoefW = width / FOCUS_AREA_FULL_SIZE;
        focusKoefH = height / FOCUS_AREA_FULL_SIZE;
    }

    public void setFocusMode(FocusMode focusMode) {
        clearCameraFocus();
        this.focusMode = focusMode;
        focusing = false;
        setOnTouchListener(new CameraTouchListener());
    }

    private void startFocusing() {
        if (!focusing) {
            focused = false;
            focusing = true;
            if (focusMode == FocusMode.AUTO || (focusMode == FocusMode.TOUCH && tapArea == null)) {
                drawFocusFrame(createAutoFocusRect());
            }
            camera.autoFocus(this);
        }
    }

    public void takePicture() {
        if (hasAutoFocus) {
            if (focusMode == FocusMode.AUTO) {
                startFocusing();
            }
            if (focusMode == FocusMode.TOUCH) {
                if (focused && tapArea != null) {
                    focused();
                } else {
                    startFocusing();
                }
            }
        } else {
            focused();
        }
    }

    private Rect createAutoFocusRect() {
        int left = (int) (getWidth() / 2 - FOCUS_AREA_SIZE);
        int right = (int) (getWidth() / 2 + FOCUS_AREA_SIZE);
        int top = (int) (getHeight() / 2 - FOCUS_AREA_SIZE);
        int bottom = (int) (getHeight() / 2 + FOCUS_AREA_SIZE);
        return new Rect(left, top, right, bottom);
    }

    private void startPreview(SurfaceHolder holder) {
        Timber.d("startPreview");
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(DISPLAY_ORIENTATION);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(parameters);
            camera.startPreview();
        } catch (Exception e) {
            Timber.e(e, "Error starting camera preview: " + e.getMessage());
        }
    }

    private void stopPreview() {
        Timber.d("stopPreview");
        try {
            camera.stopPreview();
        } catch (Exception e) {
            Timber.e(e, "Error stopping camera preview: " + e.getMessage());
        }
    }

    private void drawFocusFrame(Rect rect) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
        canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);
        canvas.drawLine(rect.right, rect.bottom, rect.left, rect.bottom, paint);
        canvas.drawLine(rect.left, rect.bottom, rect.left, rect.top, paint);
        canvasFrame.draw(canvas);
        canvasFrame.invalidate();
    }

    private void clearCameraFocus() {
        if (hasAutoFocus) {
            focused = false;
            camera.cancelAutoFocus();
            if (canvas != null) {
                tapArea = null;
                try {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFocusAreas(null);
                    parameters.setMeteringAreas(null);
                    camera.setParameters(parameters);
                } catch (Exception e) {
                    Timber.e(e, "clearCameraFocus");
                } finally {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    canvasFrame.draw(canvas);
                    canvasFrame.invalidate();
                }
            }
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        focusing = false;
        focused = true;
        if (focusMode == FocusMode.AUTO) {
            focused();
        }
        if (focusMode == FocusMode.TOUCH && tapArea == null) {
            focused();
        }
    }

    private void focused() {
        focusing = false;
        if (focusCallback != null) {
            focusCallback.onFocused(camera);
        }
    }

    public void onPictureTaken() {
        clearCameraFocus();
    }

    protected void focusOnTouch(MotionEvent event) {
        tapArea = calculateTapArea(event.getX(), event.getY(), 1f);
        Camera.Parameters parameters = camera.getParameters();
        int maxFocusAreas = parameters.getMaxNumFocusAreas();
        if (maxFocusAreas > 0) {
            Camera.Area area = new Camera.Area(convert(tapArea), 100);
            parameters.setFocusAreas(Arrays.asList(area));
        }
        maxFocusAreas = parameters.getMaxNumMeteringAreas();
        if (maxFocusAreas > 0) {
            Rect rectMetering = calculateTapArea(event.getX(), event.getY(), 1.5f);
            Camera.Area area = new Camera.Area(convert(rectMetering), 100);
            parameters.setMeteringAreas(Arrays.asList(area));
        }
        camera.setParameters(parameters);
        drawFocusFrame(tapArea);
        startFocusing();
    }

    /**
     * Convert touch position x:y to {@link android.hardware.Camera.Area} position -1000:-1000 to 1000:1000.
     */
    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(FOCUS_AREA_SIZE * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, getHeight() - areaSize);

        RectF rect = new RectF(left, top, left + areaSize, top + areaSize);
        Timber.d("tap: " + rect.toShortString());

        return round(rect);
    }

    private Rect round(RectF rect) {
        return new Rect(Math.round(rect.left), Math.round(rect.top), Math.round(rect.right), Math.round(rect.bottom));
    }

    private Rect convert(Rect rect) {
        Rect result = new Rect();

        result.top = normalize(rect.top / focusKoefH - 1000);
        result.left = normalize(rect.left / focusKoefW - 1000);
        result.right = normalize(rect.right / focusKoefW - 1000);
        result.bottom = normalize(rect.bottom / focusKoefH - 1000);
        Timber.d("convert: " + result.toShortString());

        return result;
    }

    private int normalize(float value) {
        if (value > 1000) {
            return 1000;
        }
        if (value < -1000) {
            return -1000;
        }
        return Math.round(value);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private void scale(float scaleFactor) {
        scaleFactor = BigDecimal.valueOf(scaleFactor).setScale(ACCURACY, BigDecimal.ROUND_HALF_UP).floatValue();
        if (Float.compare(scaleFactor, 1.0f) == 0 || Float.compare(scaleFactor, prevScaleFactor) == 0) {
            return;
        }
        if (scaleFactor > 1f) {
            keyEventsListener.zoomIn();
        }
        if (scaleFactor < 1f) {
            keyEventsListener.zoomOut();
        }
        prevScaleFactor = scaleFactor;
    }

    private class CameraTouchListener implements OnTouchListener {

        private ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(activity, new ScaleListener());
        private GestureDetector mTapDetector = new GestureDetector(activity, new TapListener());

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            clearCameraFocus();
            if (event.getPointerCount() > 1) {
                mScaleDetector.onTouchEvent(event);
                return true;
            }
            if (hasAutoFocus && focusMode == FocusMode.TOUCH) {
                mTapDetector.onTouchEvent(event);
                return true;
            }
            return true;
        }

        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scale(detector.getScaleFactor());
                return true;
            }

        }

        private class TapListener extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                focusOnTouch(event);
                return true;
            }

        }

    }

}
