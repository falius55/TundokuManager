package com.example.ymiyauchi.mylibrary.listener;

import java.util.Map;

/**
 * Created by ymiyauchi on 2016/11/11.
 */

public interface ObservableMap<K, V> extends Map<K, V>, Observable<OnMapChangedListener<K, V>> {

    @Override
    void addEventListener(OnMapChangedListener<K, V> listener);

    @Override
    void removeEventListener(OnMapChangedListener<K, V> listenr);

    V putWithNonEvent(K key, V value);
}
