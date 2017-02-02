package com.example.ymiyauchi.mylibrary.remote.handler;

import java.nio.channels.SelectionKey;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public interface Handler {

    void handle(SelectionKey key);
}
