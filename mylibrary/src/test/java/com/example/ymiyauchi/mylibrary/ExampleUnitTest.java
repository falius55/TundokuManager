package com.example.ymiyauchi.mylibrary;

import org.junit.Test;

import jp.gr.java_conf.falius.util.range.IntRange;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void intRange_test() {
        int start = -5;
        int end = 29;
        int step = 2;
        int k = start;
        for (int i : new IntRange(k, end, step).toArray()) {
            System.out.println("i : " + i);
            assertEquals(k, i);
            k += step;
        }
        System.out.println("k : " + k);
        assertTrue(k >= end);
        assertTrue(k < end + step);
    }
}