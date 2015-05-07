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

package com.yalantis.cameramodule.fragment;

import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.yalantis.cameramodule.CameraConst;
import com.yalantis.cameramodule.R;
import com.yalantis.cameramodule.control.CameraPreview;
import com.yalantis.cameramodule.interfaces.CameraParamsChangedListener;
import com.yalantis.cameramodule.interfaces.FocusCallback;
import com.yalantis.cameramodule.interfaces.KeyEventsListener;
import com.yalantis.cameramodule.interfaces.PhotoSavedListener;
import com.yalantis.cameramodule.interfaces.PhotoTakenCallback;
import com.yalantis.cameramodule.interfaces.RawPhotoTakenCallback;
import com.yalantis.cameramodule.model.FlashMode;
import com.yalantis.cameramodule.model.FocusMode;
import com.yalantis.cameramodule.model.HDRMode;
import com.yalantis.cameramodule.model.Quality;
import com.yalantis.cameramodule.model.Ratio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class CameraFragment extends com.yalantis.cameramodule.fragment.BaseFragment implements PhotoSavedListener, KeyEventsListener, CameraParamsChangedListener, FocusCallback {

    public static final String QUALITY = "quality";
    public static final String RATIO = "ratio";
    public static final String FOCUS_MODE = "focus_mode";
    public static final String FLASH_MODE = "flash_mode";
    public static final String HDR_MODE = "hdr_mode";
    public static final String FRONT_CAMERA = "front_camera";
    private PhotoTakenCallback callback;
    private RawPhotoTakenCallback rawCallback;
    private CameraParamsChangedListener paramsChangedListener;
    private OrientationEventListener orientationListener;

    private Quality quality;
    private Ratio ratio;
    private FlashMode flashMode;
    private FocusMode focusMode;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mNavigationBarHeight;
    private int mStatusBarHeight;
    private List<Integer> zoomRatios;
    private int zoomIndex;
    private int minZoomIndex;
    private int maxZoomIndex;

    private Map<Ratio, Camera.Size> previewSizes;
    private Map<Ratio, Map<Quality, Camera.Size>> pictureSizes;

    private int layoutId;
    private Camera camera;
    private Camera.Parameters parameters;
    private CameraPreview cameraPreview;
    private ViewGroup previewContainer;
    private View mCapture;
    private ProgressBar progressBar;
    private ImageButton flashModeButton;
    private TextView mZoomRatioTextView;
    private HDRMode hdrMode;
    private boolean supportedHDR = false;
    private boolean supportedFlash = false;

    private int cameraId;
    private int outputOrientation;

    public static CameraFragment newInstance(int layoutId, PhotoTakenCallback callback, Bundle params) {
        CameraFragment fragment = new CameraFragment();
        fragment.layoutId = layoutId;
        fragment.callback = callback;
        fragment.setArguments(params);

        return fragment;
    }

    public static CameraFragment newInstance(PhotoTakenCallback callback, Bundle params) {
        CameraFragment fragment = new CameraFragment();
        fragment.callback = callback;
        fragment.layoutId = R.layout.fragment_camera;
        fragment.setArguments(params);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean useFrontCamera = getArguments().getBoolean(FRONT_CAMERA, false);
        camera = getCameraInstance(useFrontCamera);
        if (camera == null) {
            return;
        }
        initScreenParams();
        parameters = camera.getParameters();
        zoomRatios = parameters.getZoomRatios();
        zoomIndex = minZoomIndex = 0;
        maxZoomIndex = parameters.getMaxZoom();
        previewSizes = buildPreviewSizesRatioMap(parameters.getSupportedPreviewSizes());
        pictureSizes = buildPictureSizesRatioMap(parameters.getSupportedPictureSizes());
        List<String> sceneModes = parameters.getSupportedSceneModes();
        if (sceneModes != null) {
            for (String mode : sceneModes) {
                if (mode.equals(Camera.Parameters.SCENE_MODE_HDR)) {
                    supportedHDR = true;
                    break;
                }
            }
        }
        //it returns false positive
        /*getActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);*/
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null || flashModes.size() <= 1) { /* Device has no flash */
            supportedFlash = false;
        } else {
            supportedFlash = true;
        }
        if (CameraConst.DEBUG) {
            Timber.d("PictureSizesRatioMap:");
            for (Ratio r : pictureSizes.keySet()) {
                Timber.d(r.toString() + ":");
                for (Quality q : pictureSizes.get(r).keySet()) {
                    Camera.Size size = pictureSizes.get(r).get(q);
                    if (size != null) {
                        Timber.d(q.toString() + ": " + size.width + "x" + size.height);
                    }
                }
            }
        }
        expandParams(getArguments());
        initParams();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (camera == null) {
            return inflater.inflate(R.layout.fragment_no_camera, container, false);
        }
        View view = inflater.inflate(layoutId, container, false);

        try {
            previewContainer = (ViewGroup) view.findViewById(R.id.camera_preview);
        } catch (NullPointerException e) {
            throw new RuntimeException("You should add container that extends ViewGroup for CameraPreview.");
        }
        ImageView canvasFrame = new ImageView(activity);
        cameraPreview = new CameraPreview(activity, camera, canvasFrame, this, this);
        previewContainer.addView(cameraPreview);
        previewContainer.addView(canvasFrame);
        cameraPreview.setFocusMode(focusMode);

        progressBar = (ProgressBar) view.findViewById(R.id.progress);

        mCapture = view.findViewById(R.id.capture);
        if (mCapture != null) {
            mCapture.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    takePhoto();
                }

            });
        }

        flashModeButton = (ImageButton) view.findViewById(R.id.flash_mode);
        if (flashModeButton != null) {
            if (supportedFlash) {
                flashModeButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        switchFlashMode();
                        onFlashModeChanged(flashMode.getId());
                    }
                });
                setFlashModeImage(flashMode);
            } else {
                flashModeButton.setVisibility(Button.GONE);
            }
        }

        setPreviewContainerSize(mScreenWidth, mScreenHeight, ratio);

        mZoomRatioTextView = (TextView) view.findViewById(R.id.zoom_ratio);
        if (mZoomRatioTextView != null) {
            setZoomRatioText(zoomIndex);
        }

        View cameraSettings = view.findViewById(R.id.camera_settings);
        if (cameraSettings != null) {
            view.findViewById(R.id.camera_settings).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    com.yalantis.cameramodule.fragment.CameraSettingsDialogFragment.newInstance(packSettings(), CameraFragment.this).show(getFragmentManager());
                }
            });
        }

        View controls = view.findViewById(R.id.controls_layout);
        if (controls != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            params.topMargin = mStatusBarHeight;
            params.bottomMargin = mNavigationBarHeight;
            controls.setLayoutParams(params);
        }

        return view;
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private Camera getCameraInstance(boolean useFrontCamera) {
        Camera c = null;
        try {
            c = Camera.open(getCameraId(useFrontCamera));
        } catch (Exception e) {
            Timber.e(e, getString(R.string.lbl_camera_unavailable));
        }
        return c;
    }

    private int getCameraId(boolean useFrontCamera) {
        int count = Camera.getNumberOfCameras();
        int result = -1;

        if (count > 0) {
            result = 0;

            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int i = 0; i < count; i++) {
                Camera.getCameraInfo(i, info);

                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK
                        && !useFrontCamera) {
                    result = i;
                    break;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
                        && useFrontCamera) {
                    result = i;
                    break;
                }
            }
        }
        cameraId = result;
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera != null) {
            try {
                camera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (orientationListener == null) {
            initOrientationListener();
        }
        orientationListener.enable();
    }

    private void initScreenParams() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mNavigationBarHeight = getNavigationBarHeight();
        mStatusBarHeight = getStatusBarHeight();
    }

    private int getNavigationBarHeight() {
        return getPixelSizeByName("navigation_bar_height");
    }

    private int getStatusBarHeight() {
        return getPixelSizeByName("status_bar_height");
    }

    private int getPixelSizeByName(String name) {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier(name, "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public void setCallback(PhotoTakenCallback callback) {
        this.callback = callback;
    }

    public void setRawCallback(RawPhotoTakenCallback rawCallback) {
        this.rawCallback = rawCallback;
    }

    public void setParamsChangedListener(CameraParamsChangedListener paramsChangedListener) {
        this.paramsChangedListener = paramsChangedListener;
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (callback != null) {
                callback.photoTaken(data.clone(), outputOrientation);
            }
            camera.startPreview();
            cameraPreview.onPictureTaken();
        }

    };

    @Override
    public void onFocused(Camera camera) {
        camera.takePicture(null, rawPictureCallback, pictureCallback);
    }

    private Camera.PictureCallback rawPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (rawCallback != null && data != null) {
                rawCallback.rawPhotoTaken(data.clone());
            }
        }

    };

    @Override
    public void onPause() {
        super.onPause();
        if (orientationListener != null) {
            orientationListener.disable();
            orientationListener = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void expandParams(Bundle params) {
        if (params == null) {
            params = new Bundle();
        }
        int id = 0;
        if (params.containsKey(RATIO)) {
            id = params.getInt(RATIO, 1);
        }
        ratio = Ratio.getRatioById(id);
        id = 0;
        if (params.containsKey(QUALITY)) {
            id = params.getInt(QUALITY, 0);
        }
        quality = Quality.getQualityById(id);
        id = 0;
        if (params.containsKey(FOCUS_MODE)) {
            id = params.getInt(FOCUS_MODE);
        }
        focusMode = FocusMode.getFocusModeById(id);
        id = 0;
        if (params.containsKey(FLASH_MODE)) {
            id = params.getInt(FLASH_MODE);
        }
        flashMode = FlashMode.getFlashModeById(id);
        id = 0;
        if (params.containsKey(HDR_MODE)) {
            id = params.getInt(HDR_MODE);
        }
        hdrMode = HDRMode.getHDRModeById(id);
    }

    private Bundle packSettings() {
        Bundle params = new Bundle();

        params.putInt(QUALITY, quality.getId());
        params.putInt(RATIO, ratio.getId());
        params.putInt(FOCUS_MODE, focusMode.getId());
        params.putInt(HDR_MODE, hdrMode.getId());
        return params;
    }

    private void initParams() {
        setFlashMode(parameters, flashMode);

        setPreviewSize(parameters, ratio);
        setHDRMode(parameters, hdrMode);
        setPictureSize(parameters, quality, ratio);

        camera.setParameters(parameters);
    }

    @Override
    public void onQualityChanged(int id) {
        quality = Quality.getQualityById(id);
        setPictureSize(parameters, quality, ratio);
        camera.setParameters(parameters);
        if (paramsChangedListener != null) {
            paramsChangedListener.onQualityChanged(id);
        }
    }

    @Override
    public void onRatioChanged(int id) {
        ratio = Ratio.getRatioById(id);
        setPreviewSize(parameters, ratio);
        setPictureSize(parameters, quality, ratio);
        camera.setParameters(parameters);
        setPreviewContainerSize(mScreenWidth, mScreenHeight, ratio);
        if (paramsChangedListener != null) {
            paramsChangedListener.onRatioChanged(id);
        }
    }

    @Override
    public void onHDRChanged(int id) {
        hdrMode = HDRMode.getHDRModeById(id);
        setHDRMode(parameters, hdrMode);
        camera.setParameters(parameters);
        if (paramsChangedListener != null) {
            paramsChangedListener.onHDRChanged(id);
        }
    }

    @Override
    public void onFlashModeChanged(int id) {
        if (paramsChangedListener != null) {
            paramsChangedListener.onFlashModeChanged(id);
        }
    }

    @Override
    public void onFocusModeChanged(int id) {
        focusMode = FocusMode.getFocusModeById(id);
        cameraPreview.setFocusMode(focusMode);
        if (paramsChangedListener != null) {
            paramsChangedListener.onFocusModeChanged(id);
        }
    }

    @Override
    public void zoomIn() {
        if (++zoomIndex > maxZoomIndex) {
            zoomIndex = maxZoomIndex;
        }
        setZoom(zoomIndex);
    }

    @Override
    public void zoomOut() {
        if (--zoomIndex < minZoomIndex) {
            zoomIndex = minZoomIndex;
        }
        setZoom(zoomIndex);
    }

    @Override
    public void takePhoto() {
        mCapture.setEnabled(false);
        mCapture.setVisibility(View.INVISIBLE);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        cameraPreview.takePicture();
    }

    private void setZoom(int index) {
        parameters.setZoom(index);
        camera.setParameters(parameters);
        setZoomRatioText(index);
    }

    private void setZoomRatioText(int index) {
        if (mZoomRatioTextView != null) {
            float value = zoomRatios.get(index) / 100.0f;
            mZoomRatioTextView.setText(getString(R.string.lbl_zoom_ratio_value, value));
        }
    }

    private void switchFlashMode() {
        switch (flashMode) {
            case AUTO:
                flashMode = FlashMode.ON;
                break;
            case ON:
                flashMode = FlashMode.OFF;
                break;
            case OFF:
                flashMode = FlashMode.AUTO;
                break;
        }
        setFlashMode(parameters, flashMode);
        setFlashModeImage(flashMode);
        camera.setParameters(parameters);
    }

    private void setHDRMode(Camera.Parameters parameters, HDRMode hdrMode) {
        if (supportedHDR && hdrMode == HDRMode.NONE) {
            hdrMode = HDRMode.OFF;
        }
        switch (hdrMode) {
            case ON:
                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
                break;
            case OFF:
                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
                break;
        }
    }

    private void setFlashMode(Camera.Parameters parameters, FlashMode flashMode) {
        switch (flashMode) {
            case ON:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                break;
            case OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case AUTO:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
        }
    }

    private void setFlashModeImage(FlashMode flashMode) {
        switch (flashMode) {
            case ON:
                flashModeButton.setImageResource(R.drawable.cam_flash_fill_flash_icn);
                break;
            case OFF:
                flashModeButton.setImageResource(R.drawable.cam_flash_off_icn);
                break;
            case AUTO:
                flashModeButton.setImageResource(R.drawable.cam_flash_auto_icn);
                break;
        }
    }

    private void setPictureSize(Camera.Parameters parameters, Quality quality, Ratio ratio) {
        Camera.Size size = pictureSizes.get(ratio).get(quality);
        if (size != null) {
            parameters.setPictureSize(size.width, size.height);
        }
    }

    private void setPreviewSize(Camera.Parameters parameters, Ratio ratio) {
        Camera.Size size = previewSizes.get(ratio);
        parameters.setPreviewSize(size.width, size.height);
    }

    /**
     * @param width  Screen width
     * @param height Screen height
     * @param ratio  Required ratio
     */
    private void setPreviewContainerSize(int width, int height, Ratio ratio) {
        height = (width / ratio.h) * ratio.w;
        previewContainer.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
    }

    private Map<Ratio, Map<Quality, Camera.Size>> buildPictureSizesRatioMap(List<Camera.Size> sizes) {
        Map<Ratio, Map<Quality, Camera.Size>> map = new HashMap<>();

        Map<Ratio, List<Camera.Size>> ratioListMap = new HashMap<>();
        for (Camera.Size size : sizes) {
            Ratio ratio = Ratio.pickRatio(size.width, size.height);
            if (ratio != null) {
                List<Camera.Size> sizeList = ratioListMap.get(ratio);
                if (sizeList == null) {
                    sizeList = new ArrayList<>();
                    ratioListMap.put(ratio, sizeList);
                }
                sizeList.add(size);
            }
        }
        for (Ratio r : ratioListMap.keySet()) {
            List<Camera.Size> list = ratioListMap.get(r);
            ratioListMap.put(r, sortSizes(list));
            Map<Quality, Camera.Size> sizeMap = new HashMap<>();
            int i = 0;
            for (Quality q : Quality.values()) {
                Camera.Size size = null;
                if (i < list.size()) {
                    size = list.get(i++);
                }
                sizeMap.put(q, size);
            }
            map.put(r, sizeMap);
        }

        return map;
    }

    private List<Camera.Size> sortSizes(List<Camera.Size> sizes) {
        int count = sizes.size();

        while (count > 2) {
            for (int i = 0; i < count - 1; i++) {
                Camera.Size current = sizes.get(i);
                Camera.Size next = sizes.get(i + 1);

                if (current.width < next.width || current.height < next.height) {
                    sizes.set(i, next);
                    sizes.set(i + 1, current);
                }
            }
            count--;
        }

        return sizes;
    }

    private Map<Ratio, Camera.Size> buildPreviewSizesRatioMap(List<Camera.Size> sizes) {
        Map<Ratio, Camera.Size> map = new HashMap<>();

        for (Camera.Size size : sizes) {
            Ratio ratio = Ratio.pickRatio(size.width, size.height);
            if (ratio != null) {
                Camera.Size oldSize = map.get(ratio);
                if (oldSize == null || (oldSize.width < size.width || oldSize.height < size.height)) {
                    map.put(ratio, size);
                }
            }
        }

        return map;
    }

    @Override
    public void photoSaved(String path, String name) {
        mCapture.setEnabled(true);
        mCapture.setVisibility(View.VISIBLE);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void initOrientationListener() {
        orientationListener = new OrientationEventListener(activity) {

            @Override
            public void onOrientationChanged(int orientation) {
                if (camera != null && orientation != ORIENTATION_UNKNOWN) {
                    int newOutputOrientation = getCameraPictureRotation(orientation);

                    if (newOutputOrientation != outputOrientation) {
                        outputOrientation = newOutputOrientation;

                        Camera.Parameters params = camera.getParameters();
                        params.setRotation(outputOrientation);
                        try {
                            camera.setParameters(params);
                        } catch (Exception e) {
                            Timber.e(e, "Exception updating camera parameters in orientation change");
                        }
                    }
                }
            }
        };
    }

    private int getCameraPictureRotation(int orientation) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation;

        orientation = (orientation + 45) / 90 * 90;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else { // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }

        return (rotation);
    }
}
