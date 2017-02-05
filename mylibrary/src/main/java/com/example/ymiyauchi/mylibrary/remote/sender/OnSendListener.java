package com.example.ymiyauchi.mylibrary.remote.sender;

/**
 * Created by ymiyauchi on 2017/02/02.
 *
 * 送信直後に実行されるリスナーです。
 */

public interface OnSendListener {

    void onSend(int writeSize);
}
