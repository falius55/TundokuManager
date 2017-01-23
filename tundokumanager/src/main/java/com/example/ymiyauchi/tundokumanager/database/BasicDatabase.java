package com.example.ymiyauchi.tundokumanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.mylibrary.AndroidDatabase;

/**
 * Created by ymiyauchi on 2016/11/12.
 * <p>
 * データベースの初期化を行う
 */

public class BasicDatabase extends AndroidDatabase {
    private final static String DATABASE_NAME = "TundokuManager";
    private final static int VERSION = 10;

    public BasicDatabase(Context context) {
        super(context, DATABASE_NAME, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Type type : Type.values()) {
            createTable(db, type.table(),
                    ItemColumns.ID.toColumnString(type),
                    ItemColumns.NAME.toColumnString(type),
                    ItemColumns.PRICE.toColumnString(type),  // 購入金額
                    ItemColumns.DATE.toColumnString(type),  // 購入日
                    ItemColumns.PLAYED.toColumnString(type),
                    ItemColumns.CURRENT.toColumnString(type),  // capacityのうち、どこまで読んだか、見たか gameは0
                    ItemColumns.CAPACITY.toColumnString(type),  // 最大ページ数・全話数、ゲームは0
                    ItemColumns.MEMO.toColumnString(type)
            );
        }

        for (Type type : Type.values()) {
            if (!type.hasProgress()) {
                continue;
            }
            createTable(db, type.historyTable(),
                    HistoryColumns.ID.toColumnString(),
                    HistoryColumns.DATE.toColumnString(),
                    HistoryColumns.TODAY_PAGE.toColumnString(),
                    HistoryColumns.CUMULATIVE_PAGE.toColumnString(),
                    HistoryColumns.BASIC_ID.toColumnString()
            );
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Type type : Type.values()) {
            deleteTable(db, type.table());
            deleteTable(db, type.historyTable());
        }
        onCreate(db);
    }
}
