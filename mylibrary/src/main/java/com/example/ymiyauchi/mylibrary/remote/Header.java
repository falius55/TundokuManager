package com.example.ymiyauchi.mylibrary.remote;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class Header {
    private final int mHeaderSize;
    private final IntBuffer mDataSizes;

    private Header(int headerSize, IntBuffer dataSizes) {
        mHeaderSize = headerSize;
        mDataSizes = dataSizes.asReadOnlyBuffer();
    }

    public static Header parse(Collection<ByteBuffer> data) {
        IntBuffer buf = IntBuffer.allocate(data.size());
        for (ByteBuffer elem : data) {
            buf.put(elem.limit());
        }
        buf.flip();
        int headerSize = 4 + data.size() * 4;
        return new Header(headerSize, buf);
    }

    public static Header parse(SocketChannel channel) throws IOException {
        int read = 0;

        ByteBuffer headerSizeBuf = ByteBuffer.allocate(4);
        int tmp = channel.read(headerSizeBuf);
        headerSizeBuf.flip();
        read += tmp;
        if (tmp < 0) {
            throw new IOException();
        }
        int headerSize = headerSizeBuf.getInt();

        ByteBuffer headerBuf = ByteBuffer.allocate(headerSize - 4);
        tmp = channel.read(headerBuf);
        headerBuf.flip();
        read += tmp;
        if (tmp < 0) {
            throw new IOException();
        }

        int dataCount = headerSize / 4 - 1;
        IntBuffer dataSizes = IntBuffer.allocate(dataCount);
        while (headerBuf.hasRemaining()) {
            dataSizes.put(headerBuf.getInt());
        }
        dataSizes.flip();
        return new Header(read, dataSizes);
    }

    public int size() {
        return mHeaderSize;
    }

    public int dataSize() {
        IntBuffer dataSizes = mDataSizes;
        int size = mHeaderSize;
        dataSizes.rewind();
        while (dataSizes.hasRemaining()) {
            size += dataSizes.get();
        }
        return size;
    }

    public int dataSize(int index) {
        return mDataSizes.get(index);
    }

    public IntBuffer dataSizeBuffer() {
        mDataSizes.rewind();
        return mDataSizes;
    }

    public ByteBuffer toByteBuffer() {
        // ヘッダーのサイズ(自身を含む), データ１のサイズ, データ２のサイズ...
        IntBuffer dataSizes = mDataSizes;
        dataSizes.rewind();
        ByteBuffer ret = ByteBuffer.allocate(mHeaderSize);
        ret.putInt(mHeaderSize);
        while (dataSizes.hasRemaining()) {
            ret.putInt(dataSizes.get());
        }
        ret.flip();
        return ret;
    }


}
