package com.example.ymiyauchi.mylibrary.remote.server;


import com.example.ymiyauchi.mylibrary.remote.Disconnectable;
import com.example.ymiyauchi.mylibrary.remote.Remote;
import com.example.ymiyauchi.mylibrary.remote.Swapper;
import com.example.ymiyauchi.mylibrary.remote.handler.Handler;
import com.example.ymiyauchi.mylibrary.remote.handler.ReadingHandler;
import com.example.ymiyauchi.mylibrary.remote.receiver.OnReceiveListener;
import com.example.ymiyauchi.mylibrary.remote.sender.OnSendListener;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class RemoteStarter implements Handler {
    private final Swapper.SwapperFactory mSwapperFactory;
    private final Disconnectable mDisconnectable;

    private Server.OnAcceptListener mOnAcceptListener = null;
    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;

    public RemoteStarter(Disconnectable disconnectable, Swapper.SwapperFactory swapperFactory) {
        mDisconnectable = disconnectable;
        mSwapperFactory = swapperFactory;
    }

    @Override
    public void handle(SelectionKey key) {
        accept(key);
    }

    public void accept(SelectionKey key) {
        SocketChannel clientChannel = null;
        String remoteAddress = "";
        try {
            clientChannel = ((ServerSocketChannel) key.channel()).accept();
            remoteAddress = clientChannel.socket().getRemoteSocketAddress().toString();

            Remote remote = new Remote(remoteAddress, mSwapperFactory);
            remote.addOnAcceptListener(mOnAcceptListener);
            remote.addOnSendListener(mOnSendListener);
            remote.addOnReceiveListener(mOnReceiveListener);
            remote.onAccept();

            clientChannel.configureBlocking(false);
            clientChannel.register(key.selector(), SelectionKey.OP_READ,
                    new ReadingHandler(mDisconnectable, remote, false)); // 新しいチャンネルなのでregister
        } catch (Exception e) {
            if (clientChannel != null) {
                mDisconnectable.disconnect(clientChannel, key, new IOException("remote starter throws when accept new socket:" + remoteAddress, e));
            }
            e.printStackTrace();
        }
    }

    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    public void addOnAcceptListener(Server.OnAcceptListener listener) {
        mOnAcceptListener = listener;
    }
}
