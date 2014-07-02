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

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.yalantis.cameramodule.R;
import com.yalantis.cameramodule.fragment.PhotoPreviewFragment;

public class PhotoPreviewActivity extends BasePhotoActivity {

    private PhotoPreviewFragment previewFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(EXTRAS.FROM_CAMERA, false)) {
            setTitle(R.string.lbl_take_another);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.photo_preview_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void deletePhoto(MenuItem item) {
        deletePhoto();
    }

    public void rotateLeft(MenuItem item) {
        rotatePhoto(-90);
    }

    public void rotateRight(MenuItem item) {
        rotatePhoto(90);
    }

    public void openPhotoCropper(MenuItem item) {
        Intent intent = new Intent(this, PhotoCropActivity.class);
        startActivityForResult(setIntentData(intent), EXTRAS.REQUEST_PHOTO_EDIT);
    }

    @Override
    protected void showPhoto(Bitmap bitmap) {
        if (previewFragment == null) {
            previewFragment = PhotoPreviewFragment.newInstance(bitmap);
            setFragment(previewFragment);
        } else {
            previewFragment.setBitmap(bitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXTRAS.REQUEST_PHOTO_EDIT) {
            if (resultCode == EXTRAS.RESULT_EDITED) {
                setResult(EXTRAS.RESULT_EDITED, setIntentData());
                loadPhoto();
            }
        }
    }

}
