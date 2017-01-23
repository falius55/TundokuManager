package com.example.ymiyauchi.tundokumanager.data;

import android.os.Bundle;

import com.example.ymiyauchi.tundokumanager.database.ItemColumns;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.mylibrary.DateTime;

/**
 * Created by ymiyauchi on 2017/01/19.
 */

public class BundleDataConverter extends AbstractDataConverter {
    private final Bundle mData;

    public BundleDataConverter(Bundle bundle) {
        super(Type.fromCode(bundle.getInt(TYPE)), bundle.getInt(POSITION));
        mData = bundle;
    }

    @Override
    public String getName() {
        return mData.getString(ItemColumns.NAME.getName(), "");
    }

    @Override
    public String getDate() {
        String defaultValue = DateTime.now().format();
        return mData.getString(ItemColumns.DATE.getName(), defaultValue);
    }

    @Override
    public int getPrice() {
        return mData.getInt(ItemColumns.PRICE.getName());
    }

    @Override
    public boolean isPlayed() {
        return mData.getBoolean(ItemColumns.PLAYED.getName());
    }

    @Override
    public int getCurrent() {
        return mData.getInt(ItemColumns.CURRENT.getName());
    }

    @Override
    public int getCapacity() {
        return mData.getInt(ItemColumns.CAPACITY.getName());
    }

    @Override
    public String getMemo() {
        return mData.getString(ItemColumns.MEMO.getName(), "");
    }

    @Override
    public long getId() {
        return mData.getLong(ItemColumns.ID.getName(), -1);
    }
}
