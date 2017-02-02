package com.example.ymiyauchi.mylibrary.remote;

import com.example.ymiyauchi.mylibrary.remote.receiver.OnReceiveListener;
import com.example.ymiyauchi.mylibrary.remote.sender.OnSendListener;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public interface Client extends Callable<Receiver> {

    Receiver start(Swapper sender) throws IOException, TimeoutException;

    void addOnSendListener(OnSendListener listener);

    void addOnReceiveListener(OnReceiveListener listener);
}
