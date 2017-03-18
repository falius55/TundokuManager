package jp.gr.java_conf.falius.tundokumanager.app.database;

import android.provider.BaseColumns;
import android.text.TextUtils;

import jp.gr.java_conf.falius.tundokumanager.lib.DatabaseColumns;

/**
 * Created by ymiyauchi on 2017/01/22.
 */

public enum HistoryColumns implements DatabaseColumns {
    ID(BaseColumns._ID, "integer", "primary key"),
    DATE("date", "date", "not null"),
    TODAY_PAGE("today_page", "integer", "not null default 0"),
    BASIC_ID("basic_id", "integer", "not null");

    private final String name;
    private final String dataType;
    private final String options;

    HistoryColumns(String name, String dataType, String options) {
        this.name = name;
        this.dataType = dataType;
        this.options = options;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public String getOptions() {
        return options;
    }

    public String toColumnString() {
        return TextUtils.join(" ", new Object[]{getName(), getDataType(), getOptions()});
    }
}
