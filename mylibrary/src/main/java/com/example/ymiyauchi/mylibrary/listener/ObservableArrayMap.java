package com.example.ymiyauchi.mylibrary.listener;

import android.support.annotation.NonNull;
import android.util.ArrayMap;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ymiyauchi on 2016/11/11.
 * <p>
 * リスナー登録機能を追加したArraymapです
 */

public class ObservableArrayMap<K, V> implements ObservableMap<K, V> {
    private final Map<K, V> map;
    private final List<OnMapChangedListener<K, V>> listeners = new ArrayList<>();

    public ObservableArrayMap() {
        this.map = new ArrayMap<>();
    }

    public ObservableArrayMap(int capacity) {
        this.map = new ArrayMap<>(capacity);
    }

    public ObservableArrayMap(ObservableArrayMap<K, V> map) {
        this.map = new ArrayMap<>((ArrayMap<K, V>) map.map);
    }

    @Override
    public void addEventListener(OnMapChangedListener<K, V> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(OnMapChangedListener<K, V> listener) {
        listeners.remove(listener);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    @Override
    public V get(Object o) {
        return map.get(o);
    }

    @Override
    public V put(K k, V v) {
        for (OnMapChangedListener<K, V> listener : listeners) {
            listener.onPut(this, k, v);
        }
        V ret = map.put(k, v);
        for (OnMapChangedListener<K, V> listener : listeners) {
            listener.onPuted(this, k, v);
        }
        return ret;
    }

    @Override
    public V putWithNonEvent(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(Object o) {
        return map.remove(o);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (OnMapChangedListener<K, V> listener : listeners) {
            listener.onPutAll(this, map);
        }
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
