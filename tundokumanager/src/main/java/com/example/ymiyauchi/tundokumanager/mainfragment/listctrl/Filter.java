package com.example.ymiyauchi.tundokumanager.mainfragment.listctrl;

import android.util.ArrayMap;

import com.example.ymiyauchi.tundokumanager.Type;

import java.util.Map;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * リストのフィルター
 */

enum Filter {
    DEFAULT() {
        String where(Type type) {
            return "";
        }

        String label(Type type) {
            return "すべて表示";
        }
    },
    ONLY_NON_PLAY() {
        String where(Type type) {
            return "where played == '" + type.playedText(false) + "'";
        }

        String label(Type type) {
            return type.playedText(false) + "のみ表示";
        }
    },
    ONLY_PLAYED() {
        String where(Type type) {
            return "where played == '" + type.playedText(true) + "'";
        }

        String label(Type type) {
            return type.playedText(true) + "のみ表示";
        }
    };

    private static final Map<String, Filter> stringToEnum = new ArrayMap<>();

    static {
        for (Filter filter : values())
            stringToEnum.put(filter.toString(), filter);
    }

    public static Filter fromString(String symbol) {
        return stringToEnum.get(symbol);
    }

    abstract String where(Type type);

    abstract String label(Type type);
}
