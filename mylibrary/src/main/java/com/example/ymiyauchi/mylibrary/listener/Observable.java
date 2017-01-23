package com.example.ymiyauchi.mylibrary.listener;

/**
 * Created by ymiyachi on 2016/11/11.
 * <p>
 * リスナー登録機能を持ったオブジェクトが実装する必要のあるインターフェースです
 */

public interface Observable<T extends EventListener> {

    void addEventListener(T listener);

    void removeEventListener(T listener);
}
