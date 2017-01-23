package com.example.ymiyauchi.mylibrary.listener;

import java.util.Map;

/**
 * Created by ymiyauchi on 2016/11/11.
 * <p>
 * ObservableArrayMapにイベントを登録するためのインターフェースです
 * このインターフェースに定義されたメソッド内で引数に渡されたObservableMapの状態を変更しないでください。無限再帰等が発生するおそれがあります
 */

public interface OnMapChangedListener<K, V> extends EventListener {

    /**
     * マップにputする直前に実行されるメソッドです
     *
     * @param oldMap putする直前のマップ
     * @param key    新たにputするキー
     * @param value  新たにputする値
     */
    void onPut(ObservableMap<K, V> oldMap, K key, V value);

    /**
     * マップにputした直後に実行されるメソッドです
     *
     * @param newMap putした直後のマップ
     * @param key    putしたキー
     * @param value  putした値
     */
    void onPuted(ObservableMap<K, V> newMap, K key, V value);

    /**
     * putAllする直前に実行されるメソッドです
     *
     * @param oldMap putAllする直前のマップ
     * @param putMap 新たにputAllするキーと値の組み合わせが格納されたマップ
     */
    void onPutAll(ObservableMap<K, V> oldMap, Map<? extends K, ? extends V> putMap);

}
