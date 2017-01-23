package com.example.ymiyauchi.tundokumanager.data;

import android.content.ContentValues;

import com.example.ymiyauchi.tundokumanager.database.ItemColumns;
import com.example.ymiyauchi.mylibrary.DateTime;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * 可変オブジェクトであるDataConverterです。元データへの影響はありません。
 */

public class MutableDataConverter extends ReceiveDataConverter {

    /**
     * @param defaultValue putしていないデータを参照した際に取得されるデフォルトの値を格納したデータ
     */
    public MutableDataConverter(DataConverter defaultValue) {
        super(defaultValue.toIntent());
    }

    public MutableDataConverter putName(String name) {
        mIntent.putExtra(ItemColumns.NAME.getName(), name);
        return this;
    }

    public MutableDataConverter putDate(String date) {
        try {
            DateTime.newInstance(date);  // 指定フォーマットでなければ例外を投げる
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "date is not default format(yyyy/MM/dd) string : " + date);
        }
        mIntent.putExtra(ItemColumns.DATE.getName(), date);
        return this;
    }

    public MutableDataConverter putPrice(int price) {
        mIntent.putExtra(ItemColumns.PRICE.getName(), price);
        return this;
    }

    public MutableDataConverter putPlayed(String played) {
        return putPlayed(getType().isPlayed(played));
    }

    public MutableDataConverter putPlayed(boolean played) {
        mIntent.putExtra(ItemColumns.PLAYED.getName(), played);
        return this;
    }

    public MutableDataConverter putCurrent(int current) {
        mIntent.putExtra(ItemColumns.CURRENT.getName(), current);
        return this;
    }

    public MutableDataConverter putCapacity(int capacity) {
        mIntent.putExtra(ItemColumns.CAPACITY.getName(), capacity);
        return this;
    }

    public MutableDataConverter putMemo(String memo) {
        mIntent.putExtra(ItemColumns.MEMO.getName(), memo);
        return this;
    }

    @Override
    public ContentValues toContentValuesForDB() {
        ContentValues values = new ContentValues();

        if (hasKey(ItemColumns.NAME.getName())) {
            values.put(ItemColumns.NAME.getName(), getName());
        }
        if (hasKey(ItemColumns.DATE.getName())) {
            values.put(ItemColumns.DATE.getName(), getDateForDB());
        }
        if (hasKey(ItemColumns.PRICE.getName())) {
            values.put(ItemColumns.PRICE.getName(), getPrice());
        }
        if (hasKey(ItemColumns.PLAYED.getName())) {
            values.put(ItemColumns.PLAYED.getName(), getPlayedText());
        }
        if (hasKey(ItemColumns.CURRENT.getName())) {
            values.put(ItemColumns.CURRENT.getName(), getCurrent());
        }
        if (hasKey(ItemColumns.CAPACITY.getName())) {
            values.put(ItemColumns.CAPACITY.getName(), getCapacity());
        }
        if (hasKey(ItemColumns.MEMO.getName())) {
            values.put(ItemColumns.MEMO.getName(), getMemo());
        }
        return values;
    }

    private boolean hasKey(String key) {
        return mIntent.hasExtra((key));
    }

}
