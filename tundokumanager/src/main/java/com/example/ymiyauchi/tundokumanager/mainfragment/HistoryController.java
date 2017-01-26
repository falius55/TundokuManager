package com.example.ymiyauchi.tundokumanager.mainfragment;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;

import com.example.ymiyauchi.mylibrary.AndroidDatabase;
import com.example.ymiyauchi.mylibrary.DateTime;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.database.BasicDatabase;
import com.example.ymiyauchi.tundokumanager.database.HistoryColumns;

import java.util.Objects;

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

    public void initHistory(long itemId, AndroidDatabase db, DataConverter data) {
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
        values.put(HistoryColumns.CUMULATIVE_PAGE.getName(), data.getCurrent());
        values.put(HistoryColumns.DATE.getName(), data.getDateForDB());
        db.insert(mType.historyTable(), values);
    }

    public void deleteHistory(long itemId, AndroidDatabase db) {
        db.delete(mType.historyTable(), HistoryColumns.BASIC_ID.getName() + "=?", Long.toString(itemId));
    }

    /**
     * 増分が0と計算されたら何もしない
     *
     * @param data
     * @param modifyDate
     * @param newCumulativePlayed
     */
    public void updateHistoryCumulativePlayed(DataConverter data, DateTime modifyDate, int newCumulativePlayed) {
        // 増分の計算が主目的
        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            SQLiteDatabase sdb = db.beginTransaction();
            try {
                String id = Long.toString(data.getId());
                String modifyDay = modifyDate.formatTo(DateTime.SQLITE_DATE_FORMAT);
                db.selectAllColumn(mType.historyTable(), "basic_id=? and date=?", id, modifyDay);

                if (db.next()) {
                    int oldCumulative = db.getInt(HistoryColumns.CUMULATIVE_PAGE.getName());
                    int delta = newCumulativePlayed - oldCumulative;
                    if (delta == 0)
                        return;
                    updateHistoryRecord(db, data, modifyDate, delta);
                } else {
                    /*
                    1/1 20 20
                    1/3 30 50
                    1/4 20 70
                    の場合に1/2に新しい累計が40で実行された場合、
                    1/1 20 20
                    1/2 0  20  // この部分だけ先に挿入してしまう(他はいじらず)
                    1/3 30 50
                    1/4 20 70
                    にいったんした上で、増分20でupdateHistoryRecord()を実行する
                    すると、最終的には
                    1/1 20 20
                    1/2 20 40
                    1/3 10 50
                    1/4 20 70
                    となる
                     */
                    int maxCumulative = db.max(mType.historyTable(), "cumulative_page", "basic_id=? and date < ?", id, modifyDay);
                    int delta = newCumulativePlayed - maxCumulative;
                    if (delta == 0)
                        return;
                    ContentValues values = new ContentValues();
                    values.put(HistoryColumns.DATE.getName(), modifyDay);
                    values.put(HistoryColumns.TODAY_PAGE.getName(), 0);
                    values.put(HistoryColumns.CUMULATIVE_PAGE.getName(), maxCumulative);
                    values.put(HistoryColumns.BASIC_ID.getName(), data.getId());
                    db.insert(mType.historyTable(), values);
                    updateHistoryRecord(db, data, modifyDate, delta);
                }
                sdb.setTransactionSuccessful();
            } finally {
                sdb.endTransaction();
            }
        }
    }

    public void updateHistoryTodayPlayed(DataConverter data, DateTime modifyDate, int newTodayPlayed) {
        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            SQLiteDatabase sdb = db.beginTransaction();
            try {
                String id = Long.toString(data.getId());
                String modifyDay = modifyDate.formatTo(DateTime.SQLITE_DATE_FORMAT);
                db.selectAllColumn(mType.historyTable(), "basic_id=? and date=?", id, modifyDay);

                if (db.next()) {
                    int oldTodayPlayed = db.getInt(HistoryColumns.TODAY_PAGE.getName());
                    int delta = newTodayPlayed - oldTodayPlayed;
                    if (delta == 0)
                        return;
                    updateHistoryRecord(db, data, modifyDate, delta);
                } else {
                    /*
                    1/1 20 20
                    1/3 30 50
                    1/4 20 70
                    の場合に1/2に新しい当日分が15で実行された場合、
                    1/1 20 20
                    1/2 0  20  // この部分だけ先に挿入してしまう(他はいじらず)
                    1/3 30 50
                    1/4 20 70
                    にいったんした上で、増分15でupdateHistoryRecord()を実行する
                    すると、最終的には
                    1/1 20 20
                    1/2 15 35
                    1/3 15 50
                    1/4 20 70
                    となる
                     */
                    int maxCumulative = db.max(mType.historyTable(), "cumulative_page", "basic_id=? and date < ?", id, modifyDay);
                    int delta = newTodayPlayed;
                    if (delta == 0)
                        return;
                    ContentValues values = new ContentValues();
                    values.put(HistoryColumns.DATE.getName(), modifyDay);
                    values.put(HistoryColumns.TODAY_PAGE.getName(), 0);
                    values.put(HistoryColumns.CUMULATIVE_PAGE.getName(), maxCumulative);
                    values.put(HistoryColumns.BASIC_ID.getName(), data.getId());
                    db.insert(mType.historyTable(), values);
                    updateHistoryRecord(db, data, modifyDate, delta);
                }
                sdb.setTransactionSuccessful();
            } finally {
                sdb.endTransaction();
            }
        }
    }

    private void updateHistoryRecord(AndroidDatabase db, DataConverter data, DateTime modifyDate, int delta) {
        /*
        1/1 20 20
        1/2 15 35
        1/3 40 75
        1/4 35 110
        から、1/2 の当日分を15 --> 30に増やしたとすると、
        1/1 20 20
        1/2 30 50  // 新しい数値の30と古い数値の15の差分である15がdeltaの値
        1/3 25 75  // その次に当たるデータの当日分は逆にdelta分減る
        1/4 35 110
        となる
        1/2の当日分を15 --> 60に増やしたとすると、
        1/1 20 20
        1/2 60 80
                      // 1/3は40-delta(45) <= 0なので削除される
        1/4 30 110  // 1/3の受けきれなかったマイナス分5をその次の1/4から引く(40-delta=-5)
        となる
         */
        String id = Long.toString(data.getId());
        String modifyDay = modifyDate.formatTo(DateTime.SQLITE_DATE_FORMAT);
        db.selectAllColumn(mType.historyTable(), "basic_id=? and date=?", id, modifyDay);
        db.next();

        {
            /*
            上の例では、
            1/2の当日と累積をともに15(delta)増やす部分(自身の操作)
            自身の当日分がマイナスになれば自身より前の部分を操作する
             */
            int oldTodayPlayed = db.getInt(HistoryColumns.TODAY_PAGE.getName());
            int oldCumulativePlayed = db.getInt(HistoryColumns.CUMULATIVE_PAGE.getName());
            int newTodayPlayed = oldTodayPlayed + delta;
            int newCumulativePlayed = oldCumulativePlayed + delta;

            if (newTodayPlayed > 0
                    || Objects.equals(modifyDay, data.getDateForDB())) {
                ContentValues values = new ContentValues();
                values.put(HistoryColumns.DATE.getName(), modifyDay);
                values.put(HistoryColumns.TODAY_PAGE.getName(), newTodayPlayed);
                values.put(HistoryColumns.CUMULATIVE_PAGE.getName(), newCumulativePlayed);
                db.update(mType.historyTable(), values, "basic_id=? and date=?", id, modifyDay);
            }
            /*
            1/1 20 20
            1/2 30 50
            1/3 25 75
            1/4 15 90
            のから、1/3の累積75 --> 40に減らしたとすると(delta=-35)、
            1/1 20 20
            1/2 20 40   // 25+delta(-35)=-10だけ食い込まれるので減らす
            1/3         // 25 + delta(-35) <= 0なので削除(この値がnewTodayPlayed)
            1/4 50 90   // 15 - delta(-35) = 50 (この部分は下部で実行)
            となる
             */
            if (newTodayPlayed == 0) {
                db.delete(mType.historyTable(), "basic_id=? and date=?", id, modifyDay);
            }
            if (newTodayPlayed < 0) {
                db.delete(mType.historyTable(), "basic_id=? and date=?", id, modifyDay);
                // 修正日より前の操作
                db.selectWithOrder(mType.historyTable(),
                        HistoryColumns.values(),
                        /* order by */ HistoryColumns.DATE.getName() + " desc",
                        /* where */ HistoryColumns.BASIC_ID.getName() + "=? and " + HistoryColumns.DATE.getName() + " < ?", id, modifyDay);
                int prevDelta = newTodayPlayed;
                while (true) {
                    if (db.next()) {
                        int prevId = db.getInt(HistoryColumns.ID.getName());
                        int prevTodayPlayed = db.getInt(HistoryColumns.TODAY_PAGE.getName());
                        int newPrevTodayPlayed = prevTodayPlayed + prevDelta;
                        int prevCumulativePlayed = db.getInt(HistoryColumns.CUMULATIVE_PAGE.getName());
                        int newPrevCumulativePlayed = prevCumulativePlayed + prevDelta;

                        if (newPrevTodayPlayed > 0) {
                            ContentValues values = new ContentValues();
                            values.put(HistoryColumns.TODAY_PAGE.getName(), newPrevTodayPlayed);
                            values.put(HistoryColumns.CUMULATIVE_PAGE.getName(), newPrevCumulativePlayed);
                            db.update(mType.historyTable(), values, "_id=?", Integer.toString(prevId));
                            break;
                        } else if (newPrevTodayPlayed == 0) {
                            db.delete(mType.historyTable(), "_id=?", Integer.toString(prevId));
                            break;
                        } else {
                            db.delete(mType.historyTable(), "_id=?", Integer.toString(prevId));
                            prevDelta = newPrevTodayPlayed;
                        }
                    } else {
                        throw new IllegalStateException("new today played is minus");
                    }
                }
            }

        }

        // 修正日より後ろの操作
        db.selectWithOrder(mType.historyTable(),
                HistoryColumns.values(),
                HistoryColumns.DATE.getName(),
                HistoryColumns.BASIC_ID.getName() + "=? and " + HistoryColumns.DATE.getName() + " > ?", id, modifyDay);
        int nextDelta = -delta;
        while (db.next()) {
            int nextId = db.getInt(HistoryColumns.ID.getName());
            int nextTodayPlayed = db.getInt(HistoryColumns.TODAY_PAGE.getName());
            int newNextTodayPlayed = nextTodayPlayed + nextDelta;
            ContentValues values = new ContentValues();
            values.put(HistoryColumns.TODAY_PAGE.getName(), newNextTodayPlayed);
            if (newNextTodayPlayed > 0) {
                db.update(mType.historyTable(), values, "_id=?", Integer.toString(nextId));
                break;
            } else if (newNextTodayPlayed == 0) {
                db.delete(mType.historyTable(), "_id=?", Integer.toString(nextId));
                break;
            } else {
                db.delete(mType.historyTable(), "_id=?", Integer.toString(nextId));
                nextDelta = newNextTodayPlayed;
            }
        }
    }
}
