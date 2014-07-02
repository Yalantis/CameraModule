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

import timber.log.Timber;
import android.content.Context;

import com.yalantis.cameramodule.CameraConst;
import com.yalantis.cameramodule.interfaces.Initializer;

public enum LoggerManager implements Initializer {
    i;

    private Context context;

    @Override
    public void init(Context context) {
        this.context = context;
        if (CameraConst.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.DebugTree {

        @Override
        public void d(String message, Object... args) {
        }

        @Override
        public void d(Throwable t, String message, Object... args) {
        }

        @Override
        public void i(String message, Object... args) {
        }

        @Override
        public void i(Throwable t, String message, Object... args) {
        }

        @Override
        public void w(String message, Object... args) {
        }

        @Override
        public void w(Throwable t, String message, Object... args) {
        }

        @Override
        public void e(String message, Object... args) {
            super.e(message, args);
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            super.e(t, message, args);
        }
    }

    @Override
    public void clear() {
    }

}
