package com.example.ymiyauchi.mylibrary;

import java.util.Iterator;

/**
 * Created by ymiyauchi on 2017/01/13.
 * <p>
 * startを含み、endを含まない連続した数値のイテレータです。
 * toArrayメソッドを利用することで拡張for文におけるIntegerへのオートボクシング変換を防ぐことができます。
 * <p>
 * <p>
 * stepを省略すると１として扱われ、さらにstartを省略すると０から始まります。
 * </p>
 */

public class IntRange implements Iterable<Integer>, Iterator<Integer> {
    private static final int DEFAULT_START = 0;
    private static final int DEFAULT_STEP = 1;

    private final int mStart;
    private final int mEnd;
    private final int mStep;
    private int mCount;

    public IntRange(int start, int end, int step) {
        mStart = start;
        mEnd = end;
        mStep = step;
        mCount = start - step;
    }

    public IntRange(int start, int end) {
        this(start, end, DEFAULT_STEP);
    }

    public IntRange(int end) {
        this(DEFAULT_START, end);
    }

    @Override
    public boolean hasNext() {
        return mCount + mStep < mEnd;
    }

    @Override
    public Integer next() {
        return mCount = mCount + mStep;
    }

    @Override
    public Iterator<Integer> iterator() {
        return this;
    }

    public int[] toArray() {
        int start = mStart;
        int end = mEnd;
        int step = mStep;

        boolean isDivisible = (end - start) % step == 0;
        int len = (end - start) / step;
        len = isDivisible ? len : len + 1;
        int[] ret = new int[len];

        for (int i = 0, k = start; k < end; i++, k += step) {
            ret[i] = k;
        }
        return ret;
    }
}
