package com.example.ymiyauchi.tundokumanager.input;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.R;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * 入力画面の登録ボタンのリスナー
 */

class EntryButtonClickListener implements View.OnClickListener {
    private final Activity mActivity;
    private final DataConverter mData;
    private final int mResultCode;

    EntryButtonClickListener(
            Activity activity, DataConverter data, int resultCode) {
        mActivity = activity;
        mData = data;
        mResultCode = resultCode;
    }

    @Override
    public void onClick(View view) {
        RadioGroup rd = (RadioGroup) mActivity.findViewById(R.id.rdgroup);

        // Activityを閉じる
        mActivity.setResult(mResultCode, mData.toIntent());
        Log.d("INPUT_ACTIVITY", "onClick: " + mData.toIntent().toString());
        mActivity.finish();

    }
}
