package com.example.ymiyauchi.mylibrary.remote;

import com.android.annotations.Nullable;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public interface Disconnectable {

    /**
     * @param channel
     * @param key
     * @param cause   切断の原因。正常終了ならnullが渡される
     */
    void disconnect(SocketChannel channel, SelectionKey key, @Nullable Throwable cause);
}
