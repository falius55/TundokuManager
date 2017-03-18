package jp.gr.java_conf.falius.tundokumanager.app.input;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import jp.gr.java_conf.falius.tundokumanager.app.data.DataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.data.InputDataConverter;

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
            Activity activity, InputDataConverter data, int resultCode) {
        mActivity = activity;
        mData = data;
        mResultCode = resultCode;
    }

    @Override
    public void onClick(View view) {

        // Activityを閉じる
        mActivity.setResult(mResultCode, mData.toIntent());
        Log.d("INPUT_ACTIVITY", "onClick: " + mData.toIntent().toString());
        mActivity.finish();

    }
}
