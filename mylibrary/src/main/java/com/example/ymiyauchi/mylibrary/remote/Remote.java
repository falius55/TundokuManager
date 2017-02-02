package com.example.ymiyauchi.mylibrary.remote;

import com.android.annotations.NonNull;
import com.example.ymiyauchi.mylibrary.remote.receiver.OnReceiveListener;
import com.example.ymiyauchi.mylibrary.remote.sender.OnSendListener;
import com.example.ymiyauchi.mylibrary.remote.receiver.MultiDataReceiver;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.mylibrary.remote.sender.Sender;
import com.example.ymiyauchi.mylibrary.remote.server.Server;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class Remote {
    private final String mRemoteAddress;
    private final Swapper mSwapper;
    private Receiver mReceiver = null;  // receiver()が一度でも呼ばれるとnullではなくなる
    private Sender mNextSender = null;

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

    public String getAddress() {
        return mRemoteAddress;
    }

    @NonNull
    public Receiver receiver() {
        if (mReceiver == null) {
            mReceiver = new MultiDataReceiver();
        }
        mReceiver.addOnReceiveListener(mOnReceiveListener);
        return mReceiver;
    }

    public Sender sender() {
        if (mNextSender == null) {
            Sender sender = mSwapper.swap(mRemoteAddress, mReceiver);
            sender.addOnSendListener(mOnSendListener);
            return sender;
        } else {
            mNextSender.addOnSendListener(mOnSendListener);
            return mNextSender;
        }
    }

    /**
     * @param sender
     * @return 書き込みが終了したらtrue
     */
    public boolean restore(Sender sender) {
        if (sender.isSendFinished()) {
            mNextSender = null;
            return true;
        } else {
            mNextSender = sender;
            return false;
        }
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
