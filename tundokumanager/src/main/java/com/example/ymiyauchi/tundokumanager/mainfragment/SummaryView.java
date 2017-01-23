package com.example.ymiyauchi.tundokumanager.mainfragment;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.example.ymiyauchi.tundokumanager.database.BasicDatabase;
import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.mylibrary.AndroidDatabase;
import com.example.ymiyauchi.mylibrary.view.manager.TextViewManager;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * 集計部分
 */

public class SummaryView {
    private final Type mType;
    private final Fragment mFragment;
    private final TextViewManager mTextViewManager;

    SummaryView(Fragment fragment, View layout, Type type) {
        mFragment = fragment;
        mType = type;
        mTextViewManager
                = new TextViewManager(layout, new int[]{R.id.txt_sum, R.id.txt_getloss, R.id.rate, R.id.txtRate,});
        init(type, mTextViewManager);
    }

    private void init(Type type, TextViewManager textViewManager) {
        String text = TextUtils.concat(
                type.playedText(false), "/所持", type.getUnit(), "数=", type.playedText(false), type.getName(), "の割合"
        ).toString();
        textViewManager.setText(R.id.txtRate, text);
        build();

    }

    /**
     * データベースから読み出した最新の結果を用いて、集計を更新します
     */
    public void build() {
        TextViewManager textViewManager = mTextViewManager;
        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            String table = mType.table();

            Cursor sumCursor = db.query("select sum(price) as sum from " + table);
            if (sumCursor.moveToNext()) {
                textViewManager.setText(R.id.txt_sum, db.getInt("sum"));
            }

            Cursor lossCursor = db.query("select sum(price) as loss from " + table + " where played == ?", mType.playedText(false));
            if (lossCursor.moveToNext()) {
                textViewManager.setText(R.id.txt_getloss, db.getInt("loss"));
            }

            db.query("select count(*) as num from " + table + " where played == ?", mType.playedText(false));
            int yetNum = 0;
            if (db.next()) {
                yetNum = db.getInt("num");
            }

            db.query("select count(*) as count from " + table);
            int all = 0;
            if (db.next()) {
                all = db.getInt("count");
            }

            int rate = all == 0 ? 100 : (int) ((double) yetNum / all * 100);
            textViewManager.setTextFromRes(R.id.rate, R.string.rate, yetNum, all, rate);

        }
    }
}
