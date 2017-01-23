package com.example.ymiyauchi.tundokumanager.data;

import android.content.Intent;
import android.text.TextUtils;

import com.example.ymiyauchi.tundokumanager.database.ItemColumns;
import com.example.ymiyauchi.mylibrary.DateTime;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * アクティビティの移動があった際に受け渡されたIntentをデータソースとして変換するDataConverterです
 */

public class ReceiveDataConverter extends AbstractDataConverter {
    final Intent mIntent;

    public ReceiveDataConverter(Intent intent) {
        super(intent);
        mIntent = intent;
    }

    @Override
    public Intent stuffInto(Intent intent) {
        return intent.putExtras(mIntent);
    }

    @Override
    public String getName() {
        String ret = mIntent.getStringExtra(ItemColumns.NAME.getName());
        return TextUtils.isEmpty(ret) ? "" : ret;
    }

    @Override
    public String getDate() {
        String ret = mIntent.getStringExtra(ItemColumns.DATE.getName());
        if (TextUtils.isEmpty(ret)) {
            return DateTime.now().format();
        }
        return ret;
    }

    @Override
    public int getPrice() {
        return mIntent.getIntExtra(ItemColumns.PRICE.getName(), 0);
    }

    @Override
    public boolean isPlayed() {
        return mIntent.getBooleanExtra(ItemColumns.PLAYED.getName(), false);
    }

    @Override
    public int getCurrent() {
        return mIntent.getIntExtra(ItemColumns.CURRENT.getName(), 0);
    }

    @Override
    public int getCapacity() {
        return mIntent.getIntExtra(ItemColumns.CAPACITY.getName(), 0);
    }

    @Override
    public String getMemo() {
        String ret = mIntent.getStringExtra(ItemColumns.MEMO.getName());
        return TextUtils.isEmpty(ret) ? "" : ret;
    }

    @Override
    public long getId() {
        return mIntent.getLongExtra(ItemColumns.ID.getName(), -1);
    }
}
