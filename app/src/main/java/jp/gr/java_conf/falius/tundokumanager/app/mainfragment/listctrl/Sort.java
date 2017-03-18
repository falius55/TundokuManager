package jp.gr.java_conf.falius.tundokumanager.app.mainfragment.listctrl;

import android.util.ArrayMap;

import java.util.Map;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * リストビューのソート
 */

enum Sort {
    DEFAULT("登録順") {
        @Override
        String orderBy() {
            return "_id desc";
        }
    },
    DATE("購入日順") {
        @Override
        String orderBy() {
            return "date desc";
        }
    },
    PRICE("価格順") {
        @Override
        String orderBy() {
            return "price desc";
        }
    };

    private static final Map<String, Sort> stringToEnum = new ArrayMap<>();
    private final String label;

    static {
        for (Sort sort : values())
            stringToEnum.put(sort.toString(), sort);
    }

    Sort(String label) {
        this.label = label;
    }

    public static Sort fromString(String symbol) {
        return stringToEnum.get(symbol);
    }

    abstract String orderBy();

    public String label() {
        return this.label;
    }
}
