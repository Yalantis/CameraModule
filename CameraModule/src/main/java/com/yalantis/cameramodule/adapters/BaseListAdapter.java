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

package com.yalantis.cameramodule.adapters;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;

public abstract class BaseListAdapter<T> extends BaseAdapter {

    private Context mContext;
    private List<T> mList;
    private int mLayoutId;

    public BaseListAdapter(Context context, List<T> list, int layout) {
        mContext = context;
        setList(list);
        mLayoutId = layout;
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    public void addItems(List<T> items) {
        mList.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(T type) {
        mList.add(type);
        notifyDataSetChanged();
    }

    @Override
    public T getItem(int position) {
        return position >= 0 && position < mList.size() ? mList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Context getContext() {
        return mContext;
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    public List<T> getList() {
        return mList;
    }

    public void setList(List<T> list) {
        this.mList = list;
    }

    public void removeItem(int position) {
        mList.remove(position);
        notifyDataSetChanged();
    }

    public View getLayout() {
        return getInflater().inflate(getLayoutId(), null);
    }

    public int getLayoutId() {
        return mLayoutId;
    }

    public LayoutInflater getInflater() {
        return LayoutInflater.from(mContext);
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

}
