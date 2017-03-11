package com.example.ymiyauchi.mylibrary.remote.handler;

import android.util.Log;

import com.example.ymiyauchi.mylibrary.remote.Disconnectable;
import com.example.ymiyauchi.mylibrary.remote.Remote;
import com.example.ymiyauchi.mylibrary.remote.sender.Sender;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by ymiyauchi on 2017/02/02.
 *
 * 送信処理を行うハンドラです。
 */

public class WritingHandler implements Handler {
    private static final String TAG = "WRITING_HANDLER";
    private final Disconnectable mDisconnectable;
    private final Remote mRemote;
    private final boolean mIsClient;
    private Sender mSender = null;

    public WritingHandler(Disconnectable disconnectable, Remote remote, boolean isClient) {
        mDisconnectable = disconnectable;
        mRemote = remote;
        mIsClient = isClient;
    }

    @Override
    public void handle(SelectionKey key) {
        System.out.println("writing handle");
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (!channel.isOpen()) {
                // チャンネルが閉じられている場合、書き込みを中止して正常終了させる
                System.err.println("channel is closed. cancel writing.");
                mDisconnectable.disconnect(channel, key,
                        new IllegalStateException("channel is not open@writing handler"));
                return;
            }

            Sender sender;
            if (mSender == null) {
                sender = mSender = mRemote.sender();
            } else {
                sender = mSender;
            }

            if (sender == null) {
                mDisconnectable.disconnect(channel, key, null);
                return;
            }

            Sender.Result result = sender.send(channel);

            if (result == Sender.Result.UNFINISHED) {
                System.out.println("sender unfinished");
                return;
            }

            if (mIsClient || mRemote.isContinue()) {
                key.interestOps(SelectionKey.OP_READ);
                key.attach(new ReadingHandler(mDisconnectable, mRemote, mIsClient));
            } else {
                mDisconnectable.disconnect(channel, key, null);
            }

        } catch (Exception e) {
            mDisconnectable.disconnect(channel, key, new IOException("writing handler exception", e));
            e.printStackTrace();
        }
    }
}
