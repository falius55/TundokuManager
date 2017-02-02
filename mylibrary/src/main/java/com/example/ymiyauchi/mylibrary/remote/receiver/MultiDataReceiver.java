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

    private OnReceiveListener mListener = null;

    public MultiDataReceiver() {
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        mListener = listener;
    }

    @Override
    public int receive(SocketChannel channel) throws IOException {
        // もし送信側がByteBufferの配列を使って送信してきても、
        //  受け取り側ではその内容がすべて連結されて送られてくる

        Header header;
        try {
            header = Header.parse(channel);
        } catch (IOException e) {
            return -1;
        }
        int readSize = header.size();

        Deque<ByteBuffer> data = mReceivedData;

        IntBuffer dataSizeBuf = header.dataSizeBuffer();
        while (dataSizeBuf.hasRemaining()) {
            int dataSize = dataSizeBuf.get();
            ByteBuffer buf = ByteBuffer.allocate(dataSize);
            int tmp = channel.read(buf);
            if (tmp < 0) {
                return -1;
            }
            readSize += tmp;
            buf.flip();
            data.add(buf);
        }

        if (mListener != null) {
            String remoteAddress = channel.socket().getRemoteSocketAddress().toString();
            mListener.onReceive(remoteAddress, readSize, this);
        }

        return readSize;

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
        if (buf == null) {
            System.err.println("no data null");
            return null;
        }
        String ret = StandardCharsets.UTF_8.decode(buf).toString();
        return ret;
    }

    @Override
    public int getInt() {
        ByteBuffer buf = get();
        int ret = buf.getInt();
        return ret;
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

}
