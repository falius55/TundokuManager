package com.example.ymiyauchi.mylibrary.view.containeradapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ymiyauchi.mylibrary.IntRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by ymiyauchi on 2016/12/30.
 * <p>
 * 基本的なViewに対するViewSetterの実装を提供するEnumです。
 * CustomAdapterのspecifyViewSetterの引数への利用を想定しています。
 */
public enum ViewType implements CustomAdapter.ViewSetter {
    /**
     * dataをtoStringメソッドで文字列化し、TextViewに表示します。
     * dataの型に制限はありません。
     * dataがnullの場合は"null"が表示されます。
     */
    TEXT {
        /**
         * @param context コンテキスト
         * @param view リストアイテムのレイアウトに含まれるTextView
         * @param data 表示するデータ。このパラメータのtoStringメソッドで得られる文字列がviewに表示される
         * @throws IllegalArgumentException viewがTextViewとそのサブタイプのViewでなかった場合
         */
        @Override
        public void setView(Context context, int pos, @NonNull View view, Object data) {
            try {
                TextView textView = (TextView) view;
                textView.setText(Objects.toString(data));
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("found not TextView");
            }
        }
    },
    CHECKED_TEXT {
        @Override
        public void setView(Context context, int pos, @NonNull View view, Object data) {
            TEXT.setView(context, pos, view, data);
        }
    },
    IMAGE {
        /**
         * データで表される画像をImageViewに表示します。
         * データは基本的にdrawableIdかBitmapオブジェクトである必要があります。
         * データがdrawableIdあるいはBitmapオブジェクトでなかった場合は、そのViewを非表示にします。
         * @param context コンテキスト
         * @param view リストアイテムのレイアウトに含まれるImageView
         * @param data　R.drawableの値を表すIntegerあるいはBitmap
         * @throws IllegalArgumentException viewがImageViewとそのサブタイプのViewでなかった場合
         */
        @Override
        public void setView(Context context, int pos, @NonNull View view, Object data) {
            try {
                ImageView imageView = (ImageView) view;

                Bitmap bmp;
                if (data instanceof Integer) {
                    int drawable = (Integer) data;
                    Resources resources = context.getResources();
                    bmp = BitmapFactory.decodeResource(resources, drawable);
                    if (bmp == null) {
                        HIDE.setView(context, pos, view, data);
                        return;
                    }
                } else if (data instanceof Bitmap) {
                    bmp = (Bitmap) data;
                } else {
                    HIDE.setView(context, pos, view, data);
                    return;
                }

                imageView.setImageBitmap(bmp);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("found not ImageView");
            }
        }
    },
    HIDE {
        /**
         * Viewを非表示にします
         * @param context コンテキスト
         * @param view コンテナアイテムのレイアウトに含まれるView。種類は問わない
         * @param data データの中身は問わない(nullであっても結果に影響はない)
         */
        @Override
        public void setView(Context context, int pos, @NonNull View view, Object data) {
            view.setVisibility(View.INVISIBLE);  // 隠すだけで領域は詰めない
        }
    },
    NONE {
        @Override
        public void setView(Context context, int pos, @NonNull View view, Object data) {
            view.setVisibility(View.GONE);  // 領域を詰めてそのViewがなかったかのように扱う
        }
    },
    DEFAULT {
        /**
         * 何もしません。レイアウトのデフォルト状態のまま表示されます
         * @param context
         * @param view
         * @param data
         */
        @Override
        public void setView(Context context, int pos, @NonNull View view, Object data) {
        }
    }
}