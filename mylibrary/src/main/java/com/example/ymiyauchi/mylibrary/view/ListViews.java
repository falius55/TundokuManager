package com.example.ymiyauchi.mylibrary.view;

import android.support.annotation.IdRes;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.example.ymiyauchi.mylibrary.IntRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymiyauchi on 2017/01/13.
 * <p>
 * リストビューに関するstaticメソッドの集まりであるユーティリティークラスです
 */

public final class ListViews {

    private ListViews() {
    }

    /**
     * リストビューの各アイテムのRootViewがCheckedTextViewである場合に、チェックされているCheckedTextViewを配列にして返します。
     *
     * @param listView
     * @return
     * @throws ClassCastException listViewのアイテムのRootViewの型がCheckedTextViewではなかった場合
     */
    public static CheckedTextView[] checkedTextViews(ListView listView) {
        return checkedTextViewsHelper(listView, -1);
    }

    /**
     * リストビューの各アイテムにCheckedTextViewが含まれている場合に、チェックされているCheckedTextViewを配列にして返します。
     *
     * @param listView
     * @param checkTextId 各アイテム内に配置されているCheckedTextViewのID
     * @return
     * @throws IllegalArgumentException checkedTextIdがlistViewに含まれるCheckedTextViewのIDではなかった場合
     * @throws ClassCastException       checkedTextIdがCheckedTextとは異なる型のIDであった場合
     */
    public static CheckedTextView[] checkedTextViews(ListView listView, @IdRes int checkTextId) {
        return checkedTextViewsHelper(listView, checkTextId);
    }

    private static CheckedTextView[] checkedTextViewsHelper(ListView listView, int checkedTextId) {
        List<CheckedTextView> list = new ArrayList<>();
        for (int i : new IntRange(listView.getChildCount())) {
            View v = listView.getChildAt(i);

            CheckedTextView ctv;
            try {
                if (checkedTextId == -1) {
                    ctv = (CheckedTextView) v;
                } else {
                    ctv = (CheckedTextView) v.findViewById(checkedTextId);  // nullをキャストしても例外は投げられないため、ここでは正常にctvへの代入がなされる
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("specified checkedTextId is not CheckedText Id Resource", e);
            }

            if (ctv == null) {
                throw new IllegalArgumentException("nothing View has specified checkedTextId in listView");
            }

            if (ctv.isChecked()) {
                list.add(ctv);
            }
        }
        return list.toArray(new CheckedTextView[0]);
    }
}
