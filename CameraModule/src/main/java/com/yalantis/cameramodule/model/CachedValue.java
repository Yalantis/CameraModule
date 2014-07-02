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

package com.yalantis.cameramodule.model;

import android.content.SharedPreferences;

public class CachedValue<T> {

    private static SharedPreferences sharedPref;

    private SharedPreferences sp;

    private T value;
    private T defValue;
    private Class type;
    private String name;
    private boolean loaded = false;

    public CachedValue(String name, Class type) {
        this(name, null, null, type);
    }

    public CachedValue(String name, T defValue, Class type) {
        this(name, null, defValue, type);
    }

    public CachedValue(String name, T value, T defValue, Class type) {
        this.sp = sharedPref;
        this.name = name;
        this.type = type;
        this.loaded = value != null;
        this.value = value;
        this.defValue = defValue;
    }

    public void setValue(T value) {
        loaded = true;
        write(this.value = value);
    }

    public T getValue() {
        if (!loaded) {
            this.value = load();
            loaded = true;
        }
        return this.value;
    }

    public String getName() {
        return name;
    }

    private void write(T value) {
        SharedPreferences.Editor editor = sp.edit();

        if (value instanceof String) {

            editor.putString(name, (String) value);

        } else if (value instanceof Integer) {

            editor.putInt(name, (Integer) value);

        } else if (value instanceof Float) {

            editor.putFloat(name, (Float) value);

        } else if (value instanceof Long) {

            editor.putLong(name, (Long) value);

        } else if (value instanceof Boolean) {

            editor.putBoolean(name, (Boolean) value);

        }

        editor.commit();
    }

    @SuppressWarnings("unchecked")
    private T load() {

        if (type == String.class) {

            return (T) sp.getString(name, (String) defValue);

        } else if (type == Integer.class) {

            return (T) Integer.valueOf(sp.getInt(name, (Integer) defValue));

        } else if (type == Float.class) {

            return (T) Float.valueOf(sp.getFloat(name, (Float) defValue));

        } else if (type == Long.class) {

            return (T) Long.valueOf(sp.getLong(name, (Long) defValue));

        } else if (type == Boolean.class) {

            return (T) Boolean.valueOf(sp.getBoolean(name, (Boolean) defValue));

        }

        return null;
    }

    public void delete() {
        sp.edit().remove(name).commit();
        clear();
    }

    public static void initialize(SharedPreferences sp) {
        CachedValue.sharedPref = sp;
    }

    public void setSharedPreferences(SharedPreferences sp) {
        this.sp = sp;
    }

    public void clear() {
        loaded = false;
        this.value = null;
    }

}
