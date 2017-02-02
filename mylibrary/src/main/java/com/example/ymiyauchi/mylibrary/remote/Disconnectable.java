package com.example.ymiyauchi.mylibrary.remote;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public interface Disconnectable {

    void disconnect(SocketChannel channel, SelectionKey key);
}
