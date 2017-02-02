package com.example.ymiyauchi.mylibrary.remote.receiver;


import com.example.ymiyauchi.mylibrary.IntRange;
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
 */

public class MultiDataReceiver implements Receiver {
    private final Deque<ByteBuffer> mReceivedData = new ArrayDeque<>();
    private Entry mNonFinishedEntry = null;
    private OnReceiveListener mListener = null;

    public MultiDataReceiver() {
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        mListener = listener;
    }

    @Override
    public Result receive(SocketChannel channel) throws IOException {
        // もし送信側がByteBufferの配列を使って送信してきても、
        //  受け取り側ではその内容がすべて連結されて送られてくる

        Header header;
        Entry entry;
        if (mNonFinishedEntry == null) {
            try {
                header = Header.parse(channel);
                System.out.println("header size:" + header.size());
            } catch (IOException e) {
                return Result.ERROR;
            }
            entry = new Entry(header);
        } else {
            header = mNonFinishedEntry.mHeader;
            entry = mNonFinishedEntry;
        }

        int tmp = entry.read(channel);
        System.out.println("data read:" + tmp);
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
     * @return 保持している受信データがあればそのデータ。なければnull
     */
    @Override
    public ByteBuffer get() {
        return mReceivedData.poll();
    }

    @Override
    public ByteBuffer[] getAll() {
        ByteBuffer[] ret = mReceivedData.toArray(new ByteBuffer[0]);
        mReceivedData.clear();
        return ret;
    }

    @Override
    public int dataCount() {
        return mReceivedData.size();
    }

    @Override
    public void clear() {
        mReceivedData.clear();
    }

    @Override
    public String getString() {
        ByteBuffer buf = get();
        if (buf == null)
            return null;
        return StandardCharsets.UTF_8.decode(buf).toString();
    }

    /**
     * @return 保持データがなければ0
     */
    @Override
    public int getInt() {
        ByteBuffer buf = get();
        if (buf == null)
            return 0;
        return buf.getInt();
    }

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

        private void init() {
            IntBuffer sizeBuf = mHeader.dataSizeBuffer();
            while (sizeBuf.hasRemaining()) {
                ByteBuffer buf = ByteBuffer.allocate(sizeBuf.get());
                mItemData.add(buf);
            }
        }

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

        private void add(Deque<ByteBuffer> dst) {
            if (!isFinished()) {
                return;
            }
            for (ByteBuffer item : mItemData) {
                item.flip();
                dst.add(item);
            }
        }

        private boolean isFinished() {
            return mRemain == 0;
        }
    }
}
