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

package com.yalantis.cameramodule.manager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.text.TextUtils;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import com.squareup.picasso.Target;
import com.yalantis.cameramodule.interfaces.Initializer;
import com.yalantis.cameramodule.interfaces.PhotoSavedListener;
import com.yalantis.cameramodule.interfaces.StorageCallback;
import com.yalantis.cameramodule.util.CropPhotoTask;
import com.yalantis.cameramodule.util.ManagedTarget;
import com.yalantis.cameramodule.util.RotatePhotoTask;
import com.yalantis.cameramodule.util.ScaleTransformation;

public enum ImageManager implements Initializer, StorageCallback {
    i;

    private Context context;
    private Picasso picasso;

    private HashSet<ManagedTarget> targets;
    private Map<String, WeakReference<Bitmap>> bitmapMap;

    @Override
    public void init(Context context) {
        this.context = context;
        this.picasso = Picasso.with(context);
        bitmapMap = new HashMap<>();
        targets = new HashSet<>();
    }

    public void loadPhoto(String path, int width, int height, Target target) {
        File photo = !TextUtils.isEmpty(path) ? new File(path) : null;
        if (path == null) {
            target.onBitmapFailed(null);
        }
        Bitmap bitmap = getBitmap(path);
        if (bitmap != null && !bitmap.isRecycled()) {
            target.onBitmapLoaded(bitmap, Picasso.LoadedFrom.MEMORY);
        } else {
            ManagedTarget managedTarget = new ManagedTarget(target, path, this);
            Picasso.with(context)
                    .load(photo)
                    .skipMemoryCache()
                    .config(Bitmap.Config.ARGB_8888)
                    .transform(new ScaleTransformation(width, height))
                    .into(managedTarget);
        }
    }

    public void cropBitmap(String path, int width, int height, Bitmap croppedBitmap, RectF rect, PhotoSavedListener callback) {
        setBitmap(path, croppedBitmap);
        new CropPhotoTask(path, width, height, rect, callback).execute();
    }

    public Bitmap rotatePhoto(String path, float angle) {
        Bitmap bitmap = getBitmap(path);
        if (bitmap != null && !bitmap.isRecycled()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        setBitmap(path, bitmap);
        new RotatePhotoTask(path, angle, null).execute();

        return bitmap;
    }

    private Bitmap getBitmap(String path) {
        return bitmapMap.get(path) != null ? bitmapMap.get(path).get() : null;
    }

    @Override
    public void clear() {
        synchronized (bitmapMap) {
            for (WeakReference<Bitmap> reference : bitmapMap.values()) {
                if (reference != null) {
                    Bitmap bitmap = reference.get();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }
            bitmapMap.clear();
        }
        PicassoTools.clearCache(picasso);
    }

    @Override
    public void setBitmap(String path, Bitmap bitmap) {
        bitmapMap.put(path, new WeakReference<>(bitmap));
    }

    @Override
    public void addTarget(ManagedTarget target) {
        removeTarget(target);
        targets.add(target);
    }

    @Override
    public void removeTarget(ManagedTarget target) {
        if (targets.contains(target)) {
            targets.remove(target);
        }
    }

}
