package com.example.ymiyauchi.tundokumanager.database;

import android.provider.BaseColumns;
import android.text.TextUtils;

import com.example.ymiyauchi.tundokumanager.Type;

/**
 * Created by ymiyauchi on 2016/11/25.
 * <p>
 * データベースの列を表す列挙
 */

public enum ItemColumns {
    ID(BaseColumns._ID, "integer", "primary key"),
    NAME("name", "text", "not null"),
    PRICE("price", "integer", "not null default 0 check(price >= 0)"),
    DATE("date", "date", "not null"),
    PLAYED("played", "text", null) {
        @Override
        public String getOptions(Type type) {
            return TextUtils.concat(
                    "check(played == '", type.playedText(true), "' or played == '",
                    type.playedText(false), "') not null default '", type.playedText(false), "'"
            ).toString();
        }
    },
    CURRENT("current", "integer", "not null default 0 check(current <= capacity)"),
    CAPACITY("capacity", "integer", "not null default 0"),
    MEMO("memo", "text", "");

    private final String name;
    private final String dataType;
    private final String options;

    ItemColumns(String name, String dataType, String options) {
        this.name = name;
        this.dataType = dataType;
        this.options = options;
    }

    public String getName() {
        return name;
    }

    private String getDataType() {
        return dataType;
    }

    protected String getOptions(Type type) {
        if (options == null)
            throw new UnsupportedOperationException("don't use at " + type);
        return options;
    }

    public String toColumnString(Type type) {
        return TextUtils.join(" ", new Object[]{getName(), getDataType(), getOptions(type)});
    }
}
