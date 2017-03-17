package com.example.ymiyauchi.tundokumanager.mainfragment;

import android.content.ContentValues;
import android.support.v4.app.Fragment;

import com.example.ymiyauchi.mylibrary.AndroidDatabase;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.database.BasicDatabase;
import com.example.ymiyauchi.tundokumanager.database.HistoryColumns;

import jp.gr.java_conf.falius.util.datetime.DateTime;


/**
 * Created by ymiyauchi on 2017/01/25.
 */

public class HistoryController {
    private final Fragment mFragment;
    private final Type mType;

    public HistoryController(Fragment fragment, Type type) {
        mFragment = fragment;
        mType = type;
    }

    void initHistory(long itemId, AndroidDatabase db, DataConverter data) {
        // 新規入力された場合のDataConverterにはまだIDがない(-1が返ってくる)ため、別途itemIdを受け取る必要がある
        if (!data.getType().hasProgress()) {
            return;
        }
        if (itemId == -1) {
            return; // アイテムデータの挿入に失敗している
        }

        ContentValues values = new ContentValues();
        values.put(HistoryColumns.BASIC_ID.getName(), itemId);
        values.put(HistoryColumns.TODAY_PAGE.getName(), data.getCurrent());  // 未読なら０、既読ならcapacityが登録される
        values.put(HistoryColumns.DATE.getName(), data.getDateForDB());
        db.insert(mType.historyTable(), values);
    }

    void deleteHistory(long itemId, AndroidDatabase db) {
        if (!mType.hasProgress()) {
            return;
        }
        db.delete(mType.historyTable(), HistoryColumns.BASIC_ID.getName() + "=?", Long.toString(itemId));
    }

    public void updateDayResult(DataConverter itemData, DateTime modifyDate, int newDayResult) {
        if (!mType.hasProgress()) {
            return;
        }
        // 指定日の実績を指定された値に置き換え
        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            String strItemId = Long.toString(itemData.getId());
            String strModifyDay = modifyDate.formatTo(DateTime.SQLITE_DATE_FORMAT);
            db.selectAllColumn(mType.historyTable(), "basic_id=? and date=?", strItemId, strModifyDay);
            if (db.next()) {
                int id = db.getInt(HistoryColumns.ID.getName());
                ContentValues values = new ContentValues();
                values.put(HistoryColumns.TODAY_PAGE.getName(), newDayResult);
                db.update(mType.historyTable(), values, "_id=?", Integer.toString(id));
            } else {
                ContentValues values = new ContentValues();
                values.put(HistoryColumns.TODAY_PAGE.getName(), newDayResult);
                values.put(HistoryColumns.BASIC_ID.getName(), strItemId);
                values.put(HistoryColumns.DATE.getName(), strModifyDay);
                db.insert(mType.historyTable(), values);
            }

            // 一日の実績を増やした結果容量を超えた場合の処理
            // 新しい日付から減らしていく
            int dayResultSum = db.sum(mType.historyTable(), HistoryColumns.TODAY_PAGE.getName(), "basic_id=?", strItemId);
            if (dayResultSum > itemData.getCapacity()) {
                decreaseCurrent(db, itemData.getId(), itemData.getCapacity());
            }
        }
    }

    void onUpdateItem(DataConverter itemData) {
        if (!mType.hasProgress()) {
            return;
        }
        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            String strItemId = Long.toString(itemData.getId());

            // 購入日が変更されて、それ以前の日付でデータが存在した場合の処理
            // 購入日以前の累計を購入日の実績に上乗せ
            int cumulativeBefore = db.sum(mType.historyTable(), HistoryColumns.TODAY_PAGE.getName(),
                    "basic_id=? and date < ?", strItemId, itemData.getDateForDB());
            System.out.println("cumulative before:" + cumulativeBefore);
            if (cumulativeBefore > 0) {
                db.selectAllColumn(mType.historyTable(), "basic_id=? and date=?", strItemId, itemData.getDateForDB());
                if (db.next()) {
                    int buyDateResult = db.getInt(HistoryColumns.TODAY_PAGE.getName());
                    updateDayResult(itemData, DateTime.newInstance(itemData.getDate()), buyDateResult + cumulativeBefore);
                } else {
                    updateDayResult(itemData, DateTime.newInstance(itemData.getDate()), cumulativeBefore);
                }
            }
            db.delete(mType.historyTable(), "basic_id=? and date < ?", strItemId, itemData.getDateForDB());


            // 増えた場合は今日の実績に加え(今日のデータがなければ新たに作る)
            // 減った場合は日付が新しいものから実績を減らしていく
            int newCurrent = itemData.getCurrent();
            int oldCurrent = db.sum(mType.historyTable(), HistoryColumns.TODAY_PAGE.getName(), "basic_id=?", strItemId);
            int delta = newCurrent - oldCurrent;
            if (delta >= 0) {
                String today = DateTime.now().formatTo(DateTime.SQLITE_DATE_FORMAT);
                db.selectAllColumn(mType.historyTable(), "basic_id=? and date=?", strItemId, today);
                if (db.next()) {
                    updateDayResult(itemData, DateTime.now(), db.getInt(HistoryColumns.TODAY_PAGE.getName()) + delta);
                } else {
                    updateDayResult(itemData, DateTime.now(), delta);
                }
            } else {
                // 累計がnewCurrentを上回った日を上回った分だけ減らし、それ以降の日のデータがあれば削除
                decreaseCurrent(db, itemData.getId(), newCurrent);
            }

        }
    }

    private void decreaseCurrent(AndroidDatabase db, long itemId, int newCurrent) {
        // 累計を計算していき、newCurrentを超えた分を削除
        String strItemId = Long.toString(itemId);
        db.selectWithOrder(mType.historyTable(), HistoryColumns.values(), HistoryColumns.DATE.getName(), "basic_id=?", strItemId);
        int cumulative = 0;
        while (db.next()) {
            cumulative += db.getInt(HistoryColumns.TODAY_PAGE.getName());
            if (cumulative > newCurrent) {
                int id = db.getInt(HistoryColumns.ID.getName());
                String date = db.getString(HistoryColumns.DATE.getName());
                int diff = cumulative - newCurrent;
                int newDayResult = db.getInt(HistoryColumns.TODAY_PAGE.getName()) - diff;
                ContentValues values = new ContentValues();
                values.put(HistoryColumns.TODAY_PAGE.getName(), newDayResult);
                db.update(mType.historyTable(), values, "_id=?", Integer.toString(id));
                db.delete(mType.historyTable(), "basic_id=? and date > ?", strItemId, date);
                break;
            }
        }
    }
}
