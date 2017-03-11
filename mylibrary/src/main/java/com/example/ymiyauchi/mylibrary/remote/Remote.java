package com.example.ymiyauchi.mylibrary.remote;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.example.ymiyauchi.mylibrary.remote.receiver.OnReceiveListener;
import com.example.ymiyauchi.mylibrary.remote.sender.OnSendListener;
import com.example.ymiyauchi.mylibrary.remote.receiver.MultiDataReceiver;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.mylibrary.remote.sender.Sender;
import com.example.ymiyauchi.mylibrary.remote.server.Server;
import com.example.ymiyauchi.mylibrary.remote.swapper.Swapper;

/**
 * Created by ymiyauchi on 2017/02/02.
 *
 * 接続先に関する情報を管理するクラスです。
 * 一度の接続を通して共有されます。
 */

public class Remote {
    private final String mRemoteAddress;
    private final Swapper mSwapper;
    private Receiver mReceiver = null;  // receiver()が一度でも呼ばれるとnullではなくなる

    private Server.OnAcceptListener mOnAcceptListener = null;
    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;

    public Remote(String remoteAddress, Swapper.SwapperFactory swapperFactory) {
        mRemoteAddress = remoteAddress;
        mSwapper = swapperFactory.get();
    }

    public void addOnAcceptListener(Server.OnAcceptListener listener) {
        mOnAcceptListener = listener;
    }

    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    @NonNull
    public Receiver receiver() {
        if (mReceiver == null) {
            mReceiver = new MultiDataReceiver();
        }
        mReceiver.addOnReceiveListener(mOnReceiveListener);
        return mReceiver;
    }

    @Nullable
    public Sender sender() {
        Sender sender = mSwapper.swap(mRemoteAddress, mReceiver);
        if (sender == null) {
            return null;
        }
        sender.addOnSendListener(mOnSendListener);
        return sender;

    }

    public boolean isContinue() {
        return mSwapper.isContinue();
    }


    public void onAccept() {
        if (mOnAcceptListener != null) {
            mOnAcceptListener.onAccept(mRemoteAddress);
        }
    }
}
