package com.example.ymiyauchi.mylibrary.listener;

import java.util.Collection;
import java.util.List;

/**
 * Created by ymiyauchi on 2016/11/11.
 */

public interface ObservableList<E> extends List<E>, Observable<OnListChangedListener<E>> {

    @Override
    void addEventListener(OnListChangedListener<E> listener);

    @Override
    void removeEventListener(OnListChangedListener<E> listener);

    boolean addWithNonEvent(E e);

    /**
     * すべて追加し終えた後に一度だけイベントが発生する複数追加の操作です(オプションの操作)
     *
     * @param e
     * @return
     */
    boolean addAllWithLastEvent(Collection<E> e);
}
