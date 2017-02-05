package com.example.ymiyauchi.mylibrary.remote.sender;

import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public interface Sender {
    enum Result {
        FINISHED, UNFINISHED,
    }

    Result send(SocketChannel channel) throws IOException;

    /**
     * Client及びServerにて内部的に使用するメソッドです。
     * このメソッドでリスナーを登録しても無効となりますので注意してください。
     * 送信時のリスナーを登録するにはClient及びServerのaddOnSendListenerメソッドを利用してください。
     *
     * @param listener
     * @return
     */
    Sender addOnSendListener(OnSendListener listener);

    Sender put(ByteBuffer buf);

    Sender put(ByteBuffer[] bufs);

    Sender put(byte[] bytes);

    Sender put(int num);

    Sender put(String msg);

    Sender put(InputStream in) throws IOException;
}
