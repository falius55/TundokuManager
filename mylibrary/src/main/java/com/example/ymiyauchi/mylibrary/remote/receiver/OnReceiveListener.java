package com.example.ymiyauchi.mylibrary.remote.receiver;

/**
 * Created by ymiyauchi on 2017/02/02.
 * <p/>
 * Client及びServerにおいて、データの受信直後に実行されるリスナーです。
 *
 * <p/>
 * このリスナーでは、常に最新のReceiverオブジェクトを受け取ります。
 *
 */

public interface OnReceiveListener {

    /**
     * @param fromAddress 送信してきた相手方のアドレス
     * @param readByte    受信容量
     * @param receiver    受信データを追加されたReceiver
     */
    void onReceive(String fromAddress, int readByte, Receiver receiver);
}
