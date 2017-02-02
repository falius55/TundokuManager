package com.example.ymiyauchi.mylibrary.remote;


import com.example.ymiyauchi.mylibrary.remote.handler.Handler;
import com.example.ymiyauchi.mylibrary.remote.handler.WritingHandler;
import com.example.ymiyauchi.mylibrary.remote.receiver.OnReceiveListener;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.mylibrary.remote.sender.OnSendListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class NonBlockingClient implements Client, Disconnectable {
    private static final long POLL_TIMEOUT = 5000L;

    private final String mServerHost;
    private final int mServerPort;

    private OnSendListener mOnSendListener = null;
    private OnReceiveListener mOnReceiveListener = null;

    private Swapper.SwapperFactory mSwapperFactory;

    private boolean mIsExit = false;
    private Selector mSelector = null;

    public NonBlockingClient(String serverHost, int serverPort) {
        this(serverHost, serverPort, null);
    }

    /**
     * このオブジェクトをRunnableとして扱う際のコンストラクター
     *
     * @param serverHost
     * @param serverPort
     */
    public NonBlockingClient(String serverHost, int serverPort,
                             Swapper.SwapperFactory swapperFactory) {
        mServerHost = serverHost;
        mServerPort = serverPort;
        mSwapperFactory = swapperFactory;
    }

    @Override
    public void addOnSendListener(OnSendListener listener) {
        mOnSendListener = listener;
    }

    @Override
    public void addOnReceiveListener(OnReceiveListener listener) {
        mOnReceiveListener = listener;
    }

    /**
     * @throws IOException
     */
    @Override
    public Receiver call() throws IOException, TimeoutException {
        return start(mSwapperFactory.get());
    }

    @Override
    public void disconnect(SocketChannel channel, SelectionKey key) {
        mIsExit = true;
        if (mSelector != null) {
            mSelector.wakeup();
        }
    }

    /**
     * @throws IOException その他入出力エラーが発生した場合。接続がタイムアウトした場合も含まれます。
     */
    @Override
    public Receiver start(Swapper swapper) throws IOException, TimeoutException {
        Objects.requireNonNull(swapper, "sender is null");
        try (Selector selector = Selector.open(); SocketChannel channel = SocketChannel.open()) {
            mSelector = selector;
            Remote remote = connect(channel, swapper); // 接続はブロッキングモード
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE,
                    new WritingHandler(this, remote, true));

            while (!mIsExit) {
                if (selector.select(POLL_TIMEOUT) > 0 || selector.selectedKeys().size() > 0) {

                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        Handler handler = (Handler) key.attachment();
                        handler.handle(key);
                        iter.remove();
                    }

                } else {
                    throw new TimeoutException("could not get selected operation during " +
                            ((int) (double) POLL_TIMEOUT / 1000) + " sec.");
                }
            }
            return remote.receiver();
        }
    }

    private Remote connect(SocketChannel channel, final Swapper swapper) throws IOException {
        InetSocketAddress address = new InetSocketAddress(mServerHost, mServerPort);
        channel.connect(address);

        String remoteAddress = address.toString();
        Swapper.SwapperFactory swapperFactory = new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return swapper;
            }
        };
        Remote remote = new Remote(remoteAddress, swapperFactory);
        remote.addOnSendListener(mOnSendListener);
        remote.addOnReceiveListener(mOnReceiveListener);
        return remote;
    }

}
