package com.example.ymiyauchi.tundokumanager.input;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.tundokumanager.data.BundleDataConverter;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.data.InputDataConverter;
import com.example.ymiyauchi.tundokumanager.data.ReceiveDataConverter;
import com.example.ymiyauchi.mylibrary.view.manager.TextViewManager;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * InputActivityの具体的な処理の委譲先
 */

class Conductor {
    private final Activity mActivity;
    private final TextViewManager mTextViewManager;
    private final DataConverter mData;
    private final Result.WhereFrom mWhereFrom;
    private final EditFieldBuilder mEditFieldBuilder;

    Conductor(Activity activity, View layout, Result.WhereFrom whereFrom) {
        mActivity = activity;
        mTextViewManager = new TextViewManager(layout,
                new int[]{R.id.eDate, R.id.eTxtBookName, R.id.eTxtValue, R.id.eNumPage,
                        R.id.etxtmemo, R.id.txtName, R.id.txtCount, R.id.txtCount2,
                        R.id.btnEntry, R.id.rdbtn_midoku, R.id.rdbtn_kidoku});
        mData = new ReceiveDataConverter(activity.getIntent());
        mWhereFrom = whereFrom;
        mEditFieldBuilder = new EditFieldBuilder(activity, mTextViewManager);
        init();
    }

    private void init() {

        mEditFieldBuilder.build(mData);

        Activity activity = mActivity;
        TextViewManager textViewManager = mTextViewManager;
        Type type = mData.getType();

        RadioGroup rd = (RadioGroup) activity.findViewById(R.id.rdgroup);
        InputDataConverter inputDataConverter = new InputDataConverter(mData.getId(), mData.getPosition(), mData.getType(), textViewManager, rd, mData.getCurrent());
        textViewManager.setOnClickListener(R.id.btnEntry,
                new EntryButtonClickListener(activity, inputDataConverter, mWhereFrom.getEntryResult().getCode()
                )
        );

        // レイアウトの初期設定
        textViewManager.setText(R.id.txtName, type.getName());
        textViewManager.setText(R.id.rdbtn_midoku, type.playedText(false));
        textViewManager.setText(R.id.rdbtn_kidoku, type.playedText(true));

        if (!type.hasProgress()) {
            // ページ数などを入れる行を削除
            LinearLayout countLayout = (LinearLayout) mActivity.findViewById(R.id.countLayout);
            countLayout.removeAllViews();
        } else {
            // 細かい文言の変更
            textViewManager.setText(R.id.txtCount, type.getCountUnit());
            textViewManager.setText(R.id.txtCount2, type.getCountUnit());
        }

        textViewManager.setOnClickListener(R.id.eDate, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateDialogFragment.newInstance(mTextViewManager.getText(R.id.eDate))
                        .show(mActivity.getFragmentManager(), "date fragment");
            }
        });

    }

    void onDeleteOptionSelected() {
        mActivity.setResult(mWhereFrom.getDeleteResult().getCode(), mData.toIntent());
        mActivity.finish();
    }

    void onSaveInstanceState(Bundle outState) {
        RadioGroup rd = (RadioGroup) mActivity.findViewById(R.id.rdgroup);
        DataConverter inputDataConverter
                = new InputDataConverter(mData.getId(), mData.getPosition(), mData.getType(), mTextViewManager, rd, mData.getCurrent());
        inputDataConverter.stuffInto(outState);
    }

    void onRestoreInstanceState(Bundle savedInstanceState) {
        DataConverter saveData = new BundleDataConverter(savedInstanceState);
        mEditFieldBuilder.build(saveData);
    }
}
