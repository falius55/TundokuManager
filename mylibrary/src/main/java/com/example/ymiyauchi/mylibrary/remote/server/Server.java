package com.example.ymiyauchi.mylibrary.remote.server;

import com.example.ymiyauchi.mylibrary.remote.receiver.OnReceiveListener;
import com.example.ymiyauchi.mylibrary.remote.sender.OnSendListener;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public interface Server extends Callable<Throwable>, AutoCloseable {

    Future<?> startOnNewThread();

    void shutdown() throws IOException;

    void addOnSendListener(OnSendListener listener);

    void addOnReceiveListener(OnReceiveListener listener);

    void addOnAcceptListener(Server.OnAcceptListener callback);

    void addOnShutdownCallback(Server.OnShutdownCallback callback);

    void close() throws IOException;

    interface OnAcceptListener {

        void onAccept(String remoteAddress);
    }

    interface OnShutdownCallback {
        void onShutdown();
    }
}
