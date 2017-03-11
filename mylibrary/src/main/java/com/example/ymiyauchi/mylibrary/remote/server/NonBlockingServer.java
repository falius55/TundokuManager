package com.example.ymiyauchi.mylibrary.remote.server;

import com.android.annotations.Nullable;
import com.example.ymiyauchi.mylibrary.remote.Disconnectable;
import com.example.ymiyauchi.mylibrary.remote.swapper.Swapper;
import com.example.ymiyauchi.mylibrary.remote.handler.Handler;
import com.example.ymiyauchi.mylibrary.remote.receiver.OnReceiveListener;
import com.example.ymiyauchi.mylibrary.remote.sender.OnSendListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by ymiyauchi on 2017/02/02.
 *
 * {@inheritDoc}
 *
 * <p/>
 * startOnNewThreadメソッドの呼び出し一回につき、ひとつのスレッドで起動します。
 *
 * <p/>
 * Timeoutの設定はなく、別のスレッドからshutdownメソッドあるいはcloseメソッドが実行されるまで起動を
 * 続けます。
 */

public class NonBlockingServer implements Server, Disconnectable {
    private final int mServerPort;
    private ServerSocketChannel mServerSocketChannel = null;
    private Selector mSelector = null;

    private ExecutorService mExecutor = null;

    private final RemoteStarter mRemoteStarter;

    private Server.OnShutdownCallback mOnShutdownCallback = null;
    private volatile boolean mIsShutdowned = false;

    public NonBlockingServer(int serverPort, Swapper.SwapperFactory swapperFactory) {
        mServerPort = serverPort;
        mRemoteStarter = new RemoteStarter(this, swapperFactory);
    }

    @Override
    public void addOnReceiveListener(OnReceiveListener listener) {
        mRemoteStarter.addOnReceiveListener(listener);
    }

    @Override
    public void addOnSendListener(OnSendListener listener) {
        mRemoteStarter.addOnSendListener(listener);
    }

    @Override
    public void addOnAcceptListener(Server.OnAcceptListener listener) {
        mRemoteStarter.addOnAcceptListener(listener);
    }

    @Override
    public void addOnShutdownCallback(Server.OnShutdownCallback callback) {
        mOnShutdownCallback = callback;
    }

    @Override
    public Future<?> startOnNewThread() {
        // Handler内で発生したIOExceptionは個別の接続先との問題であると判断し、
        // 内部でcatchした後発生先との接続を切断して続行する。
        // それ以外の箇所で発生したIOExceptionはサーバー全体に問題が発生していると判断し、
        // スレッドを停止して例外を外に伝播させている
        if (mExecutor == null) {
            mExecutor = Executors.newCachedThreadPool();
        }
        return mExecutor.submit(this);
    }

    /**
     * @return null
     */
    @Override
    public Throwable call() throws IOException {
        exec();
        return null;
    }

    private void exec() throws IOException {
        try (Selector selector = Selector.open();
             ServerSocketChannel channel = ServerSocketChannel.open()) {
            mSelector = selector;
            mServerSocketChannel = channel;
            bind(channel);

            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_ACCEPT, mRemoteStarter);

            while (!mIsShutdowned) {
                // select()メソッドの戻り値は新しく通知(OP_ACCEPT)のあったキーの数
                // selectedKeys(Setオブジェクト)から明示的に削除しない限り、
                // キーはselectedKeysに格納されたままになる
                // 削除しないと、次回も再び同じキーで通知される
                if (selector.select() > 0 || selector.selectedKeys().size() > 0) {

                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        Handler handler = (Handler) key.attachment();
                        handler.handle(key);
                        iter.remove();
                    }

                }
            }

        }
    }

    private void bind(ServerSocketChannel channel) throws IOException {
        InetSocketAddress address = new InetSocketAddress(mServerPort);
        System.out.println("bind to " + getIPAddress() + ":" + address.getPort());
        channel.socket().bind(address);
    }

    private String getIPAddress() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void disconnect(SocketChannel channel, SelectionKey key, @Nullable Throwable cause) {
        try {
            if (channel != null) {
                channel.close();
            }
            key.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }

    @Override
    public void shutdown() throws IOException {
        if (mIsShutdowned) {
            return;
        }
        if (mServerSocketChannel == null) {
            return;
        }

        mIsShutdowned = true;

        mSelector.wakeup();
        mServerSocketChannel.close();
        mExecutor.shutdown();

        if (mOnShutdownCallback != null) {
            mOnShutdownCallback.onShutdown();
        }
    }
}