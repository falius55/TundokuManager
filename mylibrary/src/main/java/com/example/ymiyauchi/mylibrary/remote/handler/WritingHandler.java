package com.example.ymiyauchi.mylibrary.remote.handler;

import com.example.ymiyauchi.mylibrary.remote.Disconnectable;
import com.example.ymiyauchi.mylibrary.remote.Remote;
import com.example.ymiyauchi.mylibrary.remote.sender.Sender;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class WritingHandler implements Handler {
    private final Disconnectable mDisconnectable;
    private final Remote mRemote;
    private final boolean mIsClient;

    public WritingHandler(Disconnectable disconnectable, Remote remote, boolean isClient) {
        mDisconnectable = disconnectable;
        mRemote = remote;
        mIsClient = isClient;
    }

    @Override
    public void handle(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (!channel.isOpen()) {
                // チャンネルが閉じられている場合、書き込みを中止して正常終了させる
                System.err.println("channel is closed. cancel writting.");
                mDisconnectable.disconnect(channel, key);
                return;
            }

            Sender sender = mRemote.sender();
            if (sender == null) {
                mDisconnectable.disconnect(channel, key);
                return;
            }
            sender.send(channel);

            if (!mRemote.restore(sender)) {
                System.out.println("!sender.isWrittenFinished()");
                return;
            }

            if (mIsClient || mRemote.isContinue()) {
                key.interestOps(SelectionKey.OP_READ);
                key.attach(new ReadingHandler(mDisconnectable, mRemote, mIsClient));
            } else {
                mDisconnectable.disconnect(channel, key);
            }

        } catch (IOException e) {
            mDisconnectable.disconnect(channel, key);
            e.printStackTrace();
        }

    }

}
