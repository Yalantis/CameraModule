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

package com.yalantis.cameramodule.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.squareup.picasso.Transformation;

public class ScaleTransformation implements Transformation {

    private float width;
    private float height;

    public ScaleTransformation(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        float sWidth = source.getWidth();
        float sHeight = source.getHeight();

        float xScale;
        float yScale;
        if (sWidth < sHeight) {
            yScale = height / sHeight;
            xScale = yScale;
        } else {
            xScale = width / sWidth;
            yScale = xScale;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);
        Bitmap scaledBitmap = Bitmap.createBitmap(source, 0, 0, (int) sWidth, (int) sHeight, matrix, true);

        scaledBitmap.getWidth();
        scaledBitmap.getHeight();
        if (scaledBitmap != source) {
            source.recycle();
        }

        return scaledBitmap;
    }

    @Override
    public String key() {
        return "scaleTo" + width + "x" + height;
    }

}
