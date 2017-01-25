package com.example.ymiyauchi.tundokumanager.input;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.data.InputDataConverter;
import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.mylibrary.view.manager.TextViewManager;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * 入力画面の登録ボタンのリスナー
 */

class EntryButtonClickListener implements View.OnClickListener {
    private final Activity mActivity;
    private final TextViewManager mTextViewManager;
    private final Type mType;
    private final int mPosition;
    private final int mResultCode;
    private final long mId;

    EntryButtonClickListener(
            Activity activity, TextViewManager textViewManager, Type type,
            long id, int position, int resultCode) {
        mActivity = activity;
        mTextViewManager = textViewManager;
        mType = type;
        mId = id;
        mPosition = position;
        mResultCode = resultCode;
    }

    @Override
    public void onClick(View view) {
        RadioGroup rd = (RadioGroup) mActivity.findViewById(R.id.rdgroup);
        DataConverter data = new InputDataConverter(mId, mPosition, mType, mTextViewManager, rd);

        // Activityを閉じる
        mActivity.setResult(mResultCode, data.toIntent());
        Log.d("INPUT_ACTIVITY", "onClick: " + data.toIntent().toString());
        mActivity.finish();

    }
}
