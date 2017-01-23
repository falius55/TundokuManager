package com.example.ymiyauchi.tundokumanager.input;

import android.app.Activity;
import android.util.Log;
import android.widget.RadioButton;

import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.mylibrary.view.manager.TextViewManager;

/**
 * Created by ymiyauchi on 2017/01/20.
 */

class EditFieldBuilder {
    private final Activity mActivity;
    private final TextViewManager mTextViewManager;

    EditFieldBuilder(Activity activity, TextViewManager textViewManager) {
        mActivity = activity;
        mTextViewManager = textViewManager;
    }

    void build(DataConverter data) {
        TextViewManager textViewManager = mTextViewManager;

        textViewManager.setText(R.id.eDate, data.getDate());
        textViewManager.setText(R.id.eTxtBookName, data.getName());
        textViewManager.setText(R.id.eTxtValue, data.getPrice());
        textViewManager.setText(R.id.eNumPage, data.getCapacity());
        textViewManager.setText(R.id.etxtmemo, data.getMemo());

        // 既読の場合、既読の方のラジオボタンにチェックをつける
        RadioButton rdBtn2 = (RadioButton) mActivity.findViewById(R.id.rdbtn_kidoku);
        rdBtn2.setChecked(data.isPlayed());

        Log.d("BOOKS", "set edit : \n" + data.toString());
    }
}
