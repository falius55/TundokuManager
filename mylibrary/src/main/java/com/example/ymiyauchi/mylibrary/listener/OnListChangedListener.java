package com.example.ymiyauchi.mylibrary.listener;

import java.util.Collection;

/**
 * Created by ymiyauchi on 2016/11/11.
 * <p>
 * ObservableListにイベントを登録するためのインターフェースです
 */

public interface OnListChangedListener<E> extends EventListener {

    /**
     * リストにaddする直前に実行されるメソッドです
     *
     * @param oldList addする直前のリスト
     * @param index   新たにaddする位置のインデックス。インデックスを指定せずにaddした場合はインデックス＝リストのサイズとなります
     * @param element 新たにaddする要素
     */
    void onAdd(ObservableList<E> oldList, int index, E element);

    /**
     * リストにaddした直後に実行されるメソッドです
     *
     * @param newList addした直後のリスト
     * @param index   addされたインデックス。インデックスを指定せずにaddした場合はインデックス＝リストのサイズ－１となります
     */
    void onAdded(ObservableList<E> newList, int index);

    /**
     * リストから要素を削除する直前に実行されるメソッドです
     *
     * @param oldList 要素を削除する直前のリスト
     * @param index   削除しようとしているインデックス
     */
    void onRemove(ObservableList<E> oldList, int index);

    /**
     * リストにaddAllする直前に実行されるメソッドです
     *
     * @param oldList    addAllする直前のリスト
     * @param index      addAllするインデックス。インデックスを指定せずにaddAllした場合はインデックス＝リストのサイズとなります
     * @param collection 新たにaddAllする要素のコレクション
     */
    void onAddAll(ObservableList<E> oldList, int index, Collection<? extends E> collection);

    /**
     * リストにaddAllした直後に実行されるメソッドです
     *
     * @param newList    addAllした直後のリスト
     * @param index      このインデックスから始まる位置に挿入されている。インデックスを指定せずにaddAllした場合はインデックス＝リストのサイズー挿入した要素のコレクションのサイズとなります
     * @param collection addAllした要素のコレクション
     */
    void onAddedAll(ObservableList<E> newList, int index, Collection<? extends E> collection);

    /**
     * リストの要素を置き換える直前に実行されるメソッドです
     *
     * @param oldList 要素を置き換える直前のリスト
     * @param index   置き換えるインデックス
     * @param element 　新たに置き換える要素
     */
    void onSet(ObservableList<E> oldList, int index, E element);

    void onRemoveAll(ObservableList<E> oldList, Collection<?> collection);

    void onRetainAll(ObservableList<E> oldList, Collection<?> collection);

    void onClear(ObservableList<E> oldList);
}
