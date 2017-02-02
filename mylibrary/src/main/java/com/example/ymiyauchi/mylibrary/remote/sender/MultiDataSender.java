package com.example.ymiyauchi.mylibrary.remote.sender;


import com.example.ymiyauchi.mylibrary.remote.Header;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class MultiDataSender implements Sender {
    private final Deque<ByteBuffer> mData = new ArrayDeque<>();

    private OnSendListener mListener = null;
    private ByteBuffer mSendData = null;
    private State mState = null;

    @Override
    public Sender addOnSendListener(OnSendListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public final Result send(SocketChannel channel) throws IOException {
        State state;
        if (mState == null) {
            state = mState = new State();
            state.header = Header.parse(mData);
            state.headerBuffer = state.header.toByteBuffer();
        } else {
            state = mState;
        }

        state.writeSize += channel.write(state.headerBuffer);
        for (ByteBuffer item : mData) {
            state.writeSize += channel.write(item);
        }

        if (state.writeSize == state.header.allDataSize()) {
            if (mListener != null) {
                mListener.onSend(state.writeSize);
            }
            return Result.FINISHED;
        } else {
            System.out.println("written is not finished");
            return Result.UNFINISHED;
        }
    }

    private static class State {
        private Header header;
        private ByteBuffer headerBuffer;
        private int writeSize = 0;
    }

    @Override
    public final Sender put(ByteBuffer buf) {
        mData.add(buf);
        return this;
    }

    @Override
    public final Sender put(ByteBuffer[] bufs) {
        mData.addAll(Arrays.asList(bufs));
        return this;
    }

    private Sender put(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.allocate(bytes.length);
        buf.put(bytes);
        buf.flip();
        return put(buf);
    }

    @Override
    public Sender put(String str) {
        return put(str.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Sender put(int num) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(num);
        buf.flip();
        return put(buf);
    }

    @Override
    public Sender put(InputStream is) throws IOException {
        final int READ_SIZE = 4096;
        int size = READ_SIZE;
        ByteBuffer result = ByteBuffer.allocate(size);
        byte[] bytes = new byte[READ_SIZE];
        int len;
        while ((len = is.read(bytes)) != -1) {
            int rest = size - result.position();
            if (rest < len) {
                size += READ_SIZE;
                ByteBuffer newBuf = ByteBuffer.allocate(size);
                result.flip();
                newBuf.put(result);
                result = newBuf;
            }

            result.put(bytes, 0, len);
        }

        result.flip();
        return put(result);
    }

    @Override
    public Sender put(File file) throws IOException {
        return put(new FileInputStream(file));
    }
}
