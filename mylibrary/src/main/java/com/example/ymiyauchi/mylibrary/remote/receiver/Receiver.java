package com.example.ymiyauchi.mylibrary.remote.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public interface Receiver {

    /**
     * 内部的に使用するメソッドです。
     * 受信時のリスナーを登録する場合はClient及びServerのaddOnReceiveLisnerメソッドを使用してください。
     *
     * @param listener
     */
    void addOnReceiveListener(OnReceiveListener listener);

    int receive(SocketChannel channel) throws IOException;

    /**
     * 保持しているデータの個数を返します。
     * getXXXメソッドを呼ぶ度に保持しているデータの個数は減少します。
     *
     * @return
     */
    int dataCount();

    ByteBuffer get();

    ByteBuffer[] getAll();

    String getString();

    int getInt();

    void getAndOutput(OutputStream os) throws IOException;

    void clear();

}
