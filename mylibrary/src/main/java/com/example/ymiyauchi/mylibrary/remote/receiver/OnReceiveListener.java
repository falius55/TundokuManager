package com.example.ymiyauchi.mylibrary.remote.receiver;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public interface OnReceiveListener {
    void onReceive(String fromAddress, int readByte, Receiver receiver);

}
