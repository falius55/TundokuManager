package com.example.ymiyauchi.mylibrary.remote.handler;


import com.example.ymiyauchi.mylibrary.remote.Disconnectable;
import com.example.ymiyauchi.mylibrary.remote.Remote;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class ReadingHandler implements Handler {
    private final Disconnectable mDisconnectable;
    private final Remote mRemote;
    private final boolean mIsClient;

    public ReadingHandler(Disconnectable disconnectable, Remote remote, boolean isClient) {
        mDisconnectable = disconnectable;
        mRemote = remote;
        mIsClient = isClient;
    }

    @Override
    public void handle(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();

        try {

            Receiver receiver = mRemote.receiver();

            if (receiver.receive(channel) < 0) {
                System.err.println("receive error");
                mDisconnectable.disconnect(channel, key);
                return;
            }

            if (!mIsClient || mRemote.isContinue()) {
                key.interestOps(SelectionKey.OP_WRITE);
                key.attach(new WritingHandler(mDisconnectable, mRemote, mIsClient));
            } else {
                mDisconnectable.disconnect(channel, key);
            }

        } catch (IOException e) {
            mDisconnectable.disconnect(channel, key);
            e.printStackTrace();
        }
    }
}
