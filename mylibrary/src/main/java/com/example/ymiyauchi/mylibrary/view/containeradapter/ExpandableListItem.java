package com.example.ymiyauchi.mylibrary.view.containeradapter;

import android.support.annotation.IdRes;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymiyauchi on 2017/01/14.
 */

public class ExpandableListItem {
    private static final int ID_KEY = -1024;
    private final long mId;
    private final SparseArray<Object> mParentData;
    private final List<SparseArray<Object>> mChildrenData = new ArrayList<>();
    private final int[] mParentTo;
    private final int[] mChildTo;

    public ExpandableListItem(long groupId, SparseArray<Object> parentData, @IdRes int[] parentTo, @IdRes int[] childTo) {
        mId = groupId;
        mParentData = parentData;
        mParentTo = parentTo;
        mChildTo = childTo;
    }

    public ExpandableListItem addChild(long id) {
        SparseArray<Object> childData = new SparseArray<>();
        childData.put(ID_KEY, id);
        mChildrenData.add(childData);
        return this;
    }

    public ExpandableListItem addChildData(int index, @IdRes int res, Object data) {
        SparseArray<Object> childData = mChildrenData.get(index);
        if (childData == null) {
            throw new IllegalStateException("need to call addChild(long) before addChildData");
        }

        childData.put(res, data);
        return this;
    }

    public long getId() {
        return mId;
    }

    public long getChildId(int index) {
        return (Long) mChildrenData.get(index).get(ID_KEY);
    }

    public int getChildCount() {
        return mChildrenData.size();
    }

    public SparseArray<Object> getParentData() {
        return mParentData;
    }

    public SparseArray<Object> getChildData(int index) {
        return mChildrenData.get(index);
    }

    public int[] getParentTo() {
        return mParentTo;
    }

    public int[] getChildTo() {
        return mChildTo;
    }

}
