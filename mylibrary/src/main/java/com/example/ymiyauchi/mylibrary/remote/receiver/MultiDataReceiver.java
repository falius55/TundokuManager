package com.example.ymiyauchi.mylibrary.remote.receiver;


import android.util.Log;

import com.example.ymiyauchi.mylibrary.remote.Header;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by ymiyauchi on 2017/02/02.
 *
 * <p>
 * 複数データの受信を管理するクラスです
 * MultiDataSenderによって送信された際に利用します
 *
 * <p>
 * 詳細はReceiver interfaceを参照してください。
 *
 * @see com.example.ymiyauchi.mylibrary.remote.receiver.Receiver
 */
public class MultiDataReceiver implements Receiver {
    private static final String TAG = "MULTI_DATA RECEIVER";
    private final Deque<ByteBuffer> mReceivedData = new ArrayDeque<>();
    private Entry mNonFinishedEntry = null;
    private OnReceiveListener mListener = null;

    public MultiDataReceiver() {
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        mListener = listener;
    }

    /**
     * {@inheritDoc}
     *
     * @param channel 受信用ソケット・チャネル
     * @return
     * @throws IOException
     */
    @Override
    public Result receive(SocketChannel channel) throws IOException {
        // もし送信側がByteBufferの配列を使って送信してきても、
        //  受け取り側ではその内容がすべて連結されて送られてくる

        Header header;
        Entry entry;
        if (mNonFinishedEntry == null) {
            try {
                header = Header.from(channel);
            } catch (IOException e) {
                return Result.ERROR;
            }
            entry = new Entry(header);
        } else {
            header = mNonFinishedEntry.mHeader;
            entry = mNonFinishedEntry;
        }

        int tmp = entry.read(channel);
        Log.d(TAG, "data read:" + tmp);
        if (tmp < 0) {
            return Result.ERROR;
        }

        if (entry.isFinished()) {
            System.out.println("reading finish");
            entry.add(mReceivedData);
            if (mListener != null) {
                String remoteAddress = channel.socket().getRemoteSocketAddress().toString();
                mListener.onReceive(remoteAddress, header.allDataSize(), this);
            }
            mNonFinishedEntry = null;
            return Result.FINISHED;
        } else {
            mNonFinishedEntry = entry;
            return Result.UNFINISHED;
        }

    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public ByteBuffer get() {
        return mReceivedData.poll();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public ByteBuffer[] getAll() {
        ByteBuffer[] ret = mReceivedData.toArray(new ByteBuffer[0]);
        mReceivedData.clear();
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public int dataCount() {
        return mReceivedData.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        mReceivedData.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getString() {
        ByteBuffer buf = get();
        if (buf == null)
            return null;
        return StandardCharsets.UTF_8.decode(buf).toString();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public int getInt() {
        ByteBuffer buf = get();
        if (buf == null)
            return 0;
        return buf.getInt();
    }

    /**
     * {@inheritDoc}
     *
     * @param os
     * @throws IOException
     */
    @Override
    public void getAndOutput(OutputStream os) throws IOException {
        ByteBuffer buf = get();
        try (OutputStream out = os) {
            if (buf.hasArray()) {
                byte[] bytes = buf.array();
                out.write(bytes);
            } else {
                for (int i = buf.position(), len = buf.limit(); i < len; i++) {
                    out.write(buf.get());
                }
            }
        }
    }

    /**
     * 一度の受信単位
     *
     * @author "ymiyauchi"
     */
    private static class Entry {
        private final Header mHeader;
        private int mRemain;
        private final Deque<ByteBuffer> mItemData;

        private Entry(Header header) {
            mHeader = header;
            mRemain = header.allDataSize() - header.size();
            mItemData = new ArrayDeque<>();
            System.out.println("all data size:" + header.allDataSize());
            init();
        }

        /**
         * ヘッダーの情報を元に、読み取り予定データ分のバッファを用意します。
         */
        private void init() {
            IntBuffer sizeBuf = mHeader.dataSizeBuffer();
            while (sizeBuf.hasRemaining()) {
                ByteBuffer buf = ByteBuffer.allocate(sizeBuf.get());
                mItemData.add(buf);
            }
        }

        /**
         * データを受信します。あらかじめ決定した容量を超えるデータは何度呼び出しても読み取りません。
         * @param channel
         * @return
         * @throws IOException
         */
        private int read(SocketChannel channel) throws IOException {
            int readed = 0;
            for (ByteBuffer itemBuf : mItemData) {
                int tmp = channel.read(itemBuf);
                if (tmp < 0) {
                    return -1;
                }
                readed += tmp;
            }
            mRemain -= readed;
            return readed;
        }

        /**
         * 保持データをdstに追加します。
         * @param dst
         */
        private void add(Deque<ByteBuffer> dst) {
            if (!isFinished()) {
                return;
            }
            for (ByteBuffer item : mItemData) {
                item.flip();
                dst.add(item);
            }
        }

        /**
         *
         * @return 読み取り予定のデータをすべて受信したかどうか
         */
        private boolean isFinished() {
            return mRemain == 0;
        }
    }
}
