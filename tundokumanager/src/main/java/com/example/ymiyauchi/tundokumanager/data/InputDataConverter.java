package com.example.ymiyauchi.tundokumanager.data;

import android.text.TextUtils;
import android.widget.RadioGroup;

import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.mylibrary.view.manager.TextViewManager;

import jp.gr.java_conf.falius.util.datetime.DateTime;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * InputActivityの入力内容をデータソースとして変換するDataConverterです
 */

public class InputDataConverter extends AbstractDataConverter {
    private final TextViewManager mTextViewManager;
    private final RadioGroup mRadioGroup;
    private final long mId;
    private final int mCurrent;

    public InputDataConverter(
            long id, int position, Type type, TextViewManager textViewManager, RadioGroup radioGroup, int current) {
        super(type, position);
        mId = id;
        mTextViewManager = textViewManager;
        mRadioGroup = radioGroup;
        mCurrent = current;
    }

    @Override
    public String getName() {
        String ret = mTextViewManager.getText(R.id.eTxtBookName);
        return TextUtils.isEmpty(ret) ? "" : ret;
    }

    @Override
    public String getDate() {
        String ret = mTextViewManager.getText(R.id.eDate);
        if (TextUtils.isEmpty(ret)) {
            return DateTime.now().format();
        }
        return ret;
    }

    @Override
    public int getPrice() {
        return mTextViewManager.getIntText(R.id.eTxtValue, 0);
    }

    @Override
    public boolean isPlayed() {
        return mRadioGroup.getCheckedRadioButtonId() == R.id.rdbtn_kidoku;
    }

    @Override
    public int getCurrent() {
        return isPlayed() ? getCapacity() : mCurrent;
    }

    @Override
    public int getCapacity() {
        return mTextViewManager.getIntText(R.id.eNumPage, 0);
    }

    @Override
    public String getMemo() {
        String ret = mTextViewManager.getText(R.id.etxtmemo);
        return TextUtils.isEmpty(ret) ? "" : ret;
    }

    @Override
    public long getId() {
        return mId;
    }
}
