package com.example.ymiyauchi.mylibrary.view.containeradapter;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.List;

/**
 * Created by ymiyauchi on 2017/01/14.
 */

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {
    private static final int ID_KEY = -1024;

    private final List<ExpandableListItem> mData;
    private final int mParentLayout;
    private final int mChildLayout;

    public CustomExpandableListAdapter(List<ExpandableListItem> data, @LayoutRes int parentLayout, @LayoutRes int childLayout) {
        mData = data;
        mParentLayout = parentLayout;
        mChildLayout = childLayout;
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mData.get(groupPosition).getChildCount();
    }

    @Override
    public Object getGroup(int i) {
        return mData.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return mData.get(i).getChildData(i1);
    }

    @Override
    public long getGroupId(int i) {
        return mData.get(i).getId();
    }

    @Override
    public long getChildId(int i, int i1) {
        return mData.get(i).getChildId(i1);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View contentView, ViewGroup parent) {
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
