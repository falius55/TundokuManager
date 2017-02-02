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
    private final int mAllDataSize;
    private final IntBuffer mItemDataSizes;

    private Header(int headerSize, int allDataSize, IntBuffer itemDataSizes) {
        mHeaderSize = headerSize;
        mAllDataSize = allDataSize;
        mItemDataSizes = itemDataSizes.asReadOnlyBuffer();
    }

    public static Header parse(Collection<ByteBuffer> data) {
        int headerSize = 4 + 4 + data.size() * 4;
        int dataSize = headerSize;

        IntBuffer buf = IntBuffer.allocate(data.size());
        for (ByteBuffer elem : data) {
            int size = elem.limit();
            dataSize += size;
            buf.put(size);
        }
        buf.flip();
        return new Header(headerSize, dataSize, buf);
    }

    public static Header parse(SocketChannel channel) throws IOException {
        int read = 0;

        ByteBuffer headerSizeBuf = ByteBuffer.allocate(8);
        int tmp = channel.read(headerSizeBuf);
        headerSizeBuf.flip();
        read += tmp;
        if (tmp < 0) {
            throw new IOException();
        }
        int headerSize = headerSizeBuf.getInt();
        int dataSize = headerSizeBuf.getInt();

        ByteBuffer headerBuf = ByteBuffer.allocate(headerSize - 8);
        tmp = channel.read(headerBuf);
        headerBuf.flip();
        read += tmp;
        if (tmp < 0) {
            throw new IOException();
        }

        int dataCount = headerSize / 4 - 2;
        IntBuffer dataSizes = IntBuffer.allocate(dataCount);
        while (headerBuf.hasRemaining()) {
            dataSizes.put(headerBuf.getInt());
        }
        dataSizes.flip();
        return new Header(read, dataSize, dataSizes);
    }

    public int size() {
        return mHeaderSize;
    }

    public int allDataSize() {
        return mAllDataSize;
    }

    public int dataSize(int index) {
        return mItemDataSizes.get(index);
    }

    public IntBuffer dataSizeBuffer() {
        mItemDataSizes.rewind();
        return mItemDataSizes;
    }

    public ByteBuffer toByteBuffer() {
        // ヘッダーのサイズ(自身を含む), データ全体のサイズ(ヘッダー含む), データ１のサイズ, データ２のサイズ...
        IntBuffer dataSizes = mItemDataSizes;
        dataSizes.rewind();
        ByteBuffer ret = ByteBuffer.allocate(mHeaderSize);
        ret.putInt(mHeaderSize);
        ret.putInt(mAllDataSize);
        while (dataSizes.hasRemaining()) {
            ret.putInt(dataSizes.get());
        }
        ret.flip();
        return ret;
    }
}
