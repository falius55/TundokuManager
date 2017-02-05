package com.example.ymiyauchi.tundokumanager.tree;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by ymiyauchi on 2017/02/03.
 */
public interface TreeElement extends Parcelable {
    // View内の各Viewの保持も行うこと

    TreeElement getChild(int index);

    /**
     * @param view 自身を担当するView
     */
    void setView(View view);

    void load(TreeFragment fragment);

    boolean isLoadable();

    long getId();

    boolean hasChild();

    int getChildCount();

    TreeElement getParent();

    TreeElement root();

    @NonNull
    TreeManager getManager();

    interface Each {
        void each(TreeElement element);
    }

    interface Predicate {
        boolean test(TreeElement element);
    }

    interface Reducer<R> {
        R reduce(R result, TreeElement element);
    }
}
