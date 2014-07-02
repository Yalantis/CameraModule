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

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edmodo.cropper.CropImageView;
import com.yalantis.cameramodule.R;
import com.yalantis.cameramodule.interfaces.PhotoCroppedCallback;

public class PhotoCropFragment extends BaseFragment {

    private Bitmap bitmap;
    private CropImageView cropView;

    private PhotoCroppedCallback callback;

    public static PhotoCropFragment newInstance(Bitmap bitmap) {
        PhotoCropFragment fragment = new PhotoCropFragment();
        fragment.bitmap = bitmap;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_crop, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cropView = (CropImageView) view.findViewById(R.id.photo);
        cropView.setImageBitmap(bitmap);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof PhotoCroppedCallback) {
            callback = (PhotoCroppedCallback) activity;
        } else {
            throw new RuntimeException(activity.getClass().getName() + " must implement " + PhotoCroppedCallback.class.getName());
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        cropView.setImageBitmap(bitmap);
    }

    public void applyCrop() {
        callback.onPhotoCropped(bitmap.getWidth(), bitmap.getHeight(), cropView.getCroppedImage(), cropView.getActualCropRect());
    }

}
