package com.example.ymiyauchi.mylibrary.remote;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;

/**
 * Created by ymiyauchi on 2017/02/02.
 *
 * ヘッダー情報を管理するクラスです。
 */

public class Header {
    private static final String TAG = "HEADER";
    private final int mHeaderSize;
    private final int mAllDataSize;
    private final IntBuffer mItemDataSizes;

    private Header(int headerSize, int allDataSize, IntBuffer itemDataSizes) {
        mHeaderSize = headerSize;
        mAllDataSize = allDataSize;
        mItemDataSizes = itemDataSizes.asReadOnlyBuffer();
        Log.d(TAG, "header size:" + headerSize);
        Log.d(TAG, "all data size:" + allDataSize);
    }

    /**
     * 渡されたデータを元にヘッダーを構築します。
     *
     * @param data
     * @return
     */
    public static Header from(Collection<ByteBuffer> data) {
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

    /**
     * 受信チャネルからヘッダー情報を読み取ります。
     * @param channel
     * @return
     * @throws IOException
     */
    public static Header from(SocketChannel channel) throws IOException {
        int read = 0;

        ByteBuffer headerSizeBuf = ByteBuffer.allocate(8);
        int tmp = channel.read(headerSizeBuf);
        headerSizeBuf.flip();
        read += tmp;
        if (tmp < 0) {
            throw new IOException();
        }
        int headerSize = headerSizeBuf.getInt();
        Log.d(TAG, "size:" + headerSize);
        int allDataSize = headerSizeBuf.getInt();
        Log.d(TAG, "all data size:" + allDataSize);

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
            int dataSize = headerBuf.getInt();
            dataSizes.put(dataSize);
            Log.d(TAG, "data size:" + dataSize);

        }
        dataSizes.flip();
        return new Header(read, allDataSize, dataSizes);
    }

    public int size() {
        return mHeaderSize;
    }

    /**
     *
     * @return ヘッダーも含めた送信データの総サイズ
     */
    public int allDataSize() {
        return mAllDataSize;
    }

    public int dataSize(int index) {
        return mItemDataSizes.get(index);
    }

    /**
     * 各データのサイズを順に格納したバッファを返します。
     * @return 各データサイズを格納した読み取り専用バッファ
     */
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
            int size = dataSizes.get();
            ret.putInt(size);
            Log.d(TAG, "data size:" + size);
        }
        ret.flip();
        return ret;
    }
}
