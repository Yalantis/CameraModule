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

package com.yalantis.cameramodule.activity;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.yalantis.cameramodule.R;
import com.yalantis.cameramodule.fragment.PhotoCropFragment;
import com.yalantis.cameramodule.interfaces.PhotoCroppedCallback;
import com.yalantis.cameramodule.interfaces.PhotoSavedListener;
import com.yalantis.cameramodule.manager.ImageManager;

public class PhotoCropActivity extends BasePhotoActivity implements PhotoCroppedCallback {

    private PhotoCropFragment cropFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.photo_crop_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void apply(MenuItem ite) {
        cropFragment.applyCrop();
    }

    public void cancel(MenuItem ite) {
        finish();
    }

    @Override
    protected void showPhoto(Bitmap bitmap) {
        if (cropFragment == null) {
            cropFragment = PhotoCropFragment.newInstance(bitmap);
            setFragment(cropFragment);
        } else {
            cropFragment.setBitmap(bitmap);
        }
    }

    @Override
    public void onPhotoCropped(int width, int height, Bitmap croppedBitmap, RectF cropRect) {
        ImageManager.i.cropBitmap(path, width, height, croppedBitmap, cropRect, new PhotoSavedListener() {

            @Override
            public void photoSaved(String path, String name) {
                setResult(EXTRAS.RESULT_EDITED, setIntentData());
                finish();
            }
        });
    }

}
