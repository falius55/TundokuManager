package com.example.ymiyauchi.tundokumanager.data;

import android.text.TextUtils;
import android.widget.RadioGroup;

import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.mylibrary.DateTime;
import com.example.ymiyauchi.mylibrary.view.manager.TextViewManager;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * InputActivityの入力内容をデータソースとして変換するDataConverterです
 */

public class InputDataConverter extends AbstractDataConverter {
    private final TextViewManager mTextViewManager;
    private final RadioGroup mRadioGroup;
    private final long mId;

    public InputDataConverter(
            long id, int position, Type type, TextViewManager textViewManager, RadioGroup radioGroup) {
        super(type, position);
        mId = id;
        mTextViewManager = textViewManager;
        mRadioGroup = radioGroup;
    }

    public InputDataConverter(
            int position, Type type, TextViewManager textViewManager, RadioGroup radioGroup) {
        this(-1, position, type, textViewManager, radioGroup);
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
        return isPlayed() ? getCapacity() : 0;
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
