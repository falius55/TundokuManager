package com.example.ymiyauchi.mylibrary.view.manager;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.android.annotations.NonNull;

import java.util.Objects;

/**
 * Created by ymiyauchi on 2016/11/15.
 * <p>
 * IDを直接使用してTextViewの操作をするクラスです
 * TextViewのサブクラス(EditText, Buttonなど)でも利用できます
 * findViewByIdなどをActivity内で使わずにテキストの設定、取得などができます
 */

public final class TextViewManager {
    private SparseArray<TextView> textViews;

    public TextViewManager(View layout, @IdRes int[] textViewIds) {
        this.textViews = findViews(layout, textViewIds);
    }

    /**
     * @param layout      各TextViewを含んでいる親レイアウト
     * @param textViewIds
     * @throws NullPointerException     layoutにnullが渡された場合
     * @throws IllegalArgumentException textViewIdsにTextViewおよびそのサブタイプ以外のIDが渡された場合
     */
    private SparseArray<TextView> findViews(@NonNull View layout, @IdRes int[] textViewIds) {
        Objects.requireNonNull(layout, "layout is null");
        SparseArray<TextView> textViews = new SparseArray<>(textViewIds.length);
        try {
            for (int id : textViewIds) {
                TextView view = (TextView) layout.findViewById(id);
                textViews.put(id, view);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("not TextView or subtype ID");
        }
        return textViews;
    }

    /**
     * @param id
     * @return
     * @throws IllegalArgumentException 指定されたIDのTextViewが内部に保持されていない場合
     */
    public String getText(@IdRes int id) {
        TextView view = textViews.get(id);
        if (view == null)
            throw new IllegalArgumentException("not found view by id(" + id + ")");
        return view.getText().toString();
    }

    public int getIntText(@IdRes int id, int defaultIfNotFound) {
        String result = getText(id);
        if (TextUtils.isEmpty(result))
            return defaultIfNotFound;
        return Integer.parseInt(result);
    }

    /**
     * @param id
     * @param text
     * @throws IllegalArgumentException 指定されたIDのTextViewが内部に保持されていない場合
     */
    public void setText(@IdRes int id, Object text, Object... formatArgs) {
        TextView view = get(id);
        if (formatArgs.length == 0) {
            view.setText(text.toString());
        } else if (formatArgs.length > 0) {
            view.setText(String.format(text.toString(), formatArgs));
        }
    }

    public void setTextFromRes(@IdRes int id, @StringRes int strRes, Object... formatArgs) {
        TextView view = get(id);
        if (formatArgs.length == 0) {
            view.setText(strRes);
        } else if (formatArgs.length > 0) {
            String text = view.getContext().getString(strRes, formatArgs);
            view.setText(text);
        }
    }

    public TextView get(@IdRes int id) {
        TextView view = textViews.get(id);
        if (view == null)
            throw new IllegalArgumentException("not found view by id(" + id + ")");
        return view;
    }

    /**
     * @param id
     * @param listener
     * @throws IllegalArgumentException 指定されたIDのTextViewが内部に保持されていない場合
     */
    public void setOnClickListener(@IdRes int id, View.OnClickListener listener) {
        TextView view = textViews.get(id);
        if (view == null)
            throw new IllegalArgumentException("not found view by id(" + id + ")");
        view.setOnClickListener(listener);
    }

}
