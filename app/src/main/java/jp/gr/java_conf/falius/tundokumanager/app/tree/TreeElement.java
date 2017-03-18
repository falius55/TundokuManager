package jp.gr.java_conf.falius.tundokumanager.app.tree;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by ymiyauchi on 2017/02/03.
 *
 * 木構造の要素を表すクラスのインターフェースです。
 */
public interface TreeElement extends Parcelable {
    // View内の各Viewの保持も行うこと

    TreeElement getChild(int index);

    /**
     * 子要素を新たに読み込む必要があれば読み込みます。
     *
     * @param fragment
     */
    void load(TreeFragment fragment);

    /**
     * @return 子要素を新たに読み込む必要があるかどうか
     */
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
