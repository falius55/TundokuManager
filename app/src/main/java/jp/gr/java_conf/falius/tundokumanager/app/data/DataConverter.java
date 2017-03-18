package jp.gr.java_conf.falius.tundokumanager.app.data;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;

import jp.gr.java_conf.falius.tundokumanager.app.Type;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * IntentとConvertValuesへのデータ変換および各ソースからの特定データ取得を抽象化し、
 * アクティビティ間のデータのやりとりやデータベースの更新を容易にするためのインターフェースです
 * <p>
 * 与えられたデータソースから指定のデータが見つからなかった場合、
 * 数値は0、自由に入力される文字列は空文字列、真偽値はfalse、
 * 日付は今日の日付、getPlayedText()は偽の場合の文字列、getPosition()とgetId()は-1が返ります。
 */

public interface DataConverter {
    String POSITION = "position";
    String TYPE = "type";

    Intent toIntent();

    Intent stuffInto(Intent intent);

    Bundle toBundle();

    Bundle stuffInto(Bundle bundle);

    ContentValues toContentValuesForDB();

    /**
     * @return 名前。データがなければ空文字列
     */
    String getName();

    /**
     * @return yyyy/MM/dd。有効な値が存在しなければ現在の日付
     */
    String getDate();

    /**
     * @return yyyy-MM-dd
     */
    String getDateForDB();

    String getDays();

    int getPrice();

    String getPlayedText();

    boolean isPlayed();

    int getCurrent();

    int getCapacity();

    String getMemo();

    Type getType();

    int getPosition();

    long getId();
}
