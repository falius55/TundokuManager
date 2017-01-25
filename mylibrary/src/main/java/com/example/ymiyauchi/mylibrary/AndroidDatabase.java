package com.example.ymiyauchi.mylibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Created by ymiyauchi on 2016/11/09.
 * <p>
 * <p>SQLiteデータベースを操作するためのクラスです</p>
 * <p>まずはこのクラスを継承し、onCreateメソッドを実装してください。<br />
 * その後、インスタンスを作成し各種メソッドでデータベースの操作を行います。<br />
 * 最後にcloseメソッドを実行することを忘れないでください。
 * 必要であれば、try-with-resources文を利用することも可能です。<br />
 * また、このクラスによって作成されたCursorインスタンスはクローズする必要はありません。それらはすべて、このクラスにて請け負います。</p>
 * <p>queryメソッドは個別のCursorインスタンスを返しますが、
 * 　　　　最新の結果であればこのクラスのサブタイプのインスタンスを通して取得することもできます。</p>
 */
abstract public class AndroidDatabase extends SQLiteOpenHelper implements AutoCloseable {
    private static final String TAG_SQL = "SQLite";

    private static final int DEFAULT_VERSION = 1;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final Deque<Cursor> mCursorStack = new ArrayDeque<>();

    /**
     * @param context      データベースを利用するアプリのコンテキスト
     * @param databaseName データベース名
     * @param version      バージョン数。以前より小さなバージョン数を指定するとエラーになりますので注意してください
     */
    public AndroidDatabase(Context context, String databaseName, int version) {
        super(context, databaseName, null, version);
    }

    public AndroidDatabase(Context context, String databaseName) {
        this(context, databaseName, DEFAULT_VERSION);
    }

    /**
     * 接続するデータベース名が初めて使用される場合に、データベースへのアクセスがあったときに実行されるメソッドです。<br />
     * queryメソッドやexecuteメソッドなどによって実際にアクセスされたときであり、インスタンス作成時ではないことに注意してください。<br />
     * このメソッドでは、主にテーブル作成などの初期化処理を行います。具体的なテーブルの内容はアプリによって異なるため、抽象メソッドにしてあります
     *
     * @param db データベースのインスタンス
     */
    @Override
    abstract public void onCreate(SQLiteDatabase db);

    /**
     * テーブルを新しく作成します
     *
     * @param table           テーブル名
     * @param columnData      一列目の定義文 ex. "id integer primary key autoincrement"
     * @param otherColumnData 二列目以降の定義文
     */
    public final void createTable(String table, String columnData, String... otherColumnData) {
        SQLiteDatabase db = getWritableDatabase();
        createTable(db, table, columnData, otherColumnData);
    }

    /**
     * 初期化用にテーブルを新しく作成します
     *
     * @param db              onCreateメソッドなどで渡されたデータベースインスタンス
     * @param table           テーブル名
     * @param columnData      一列目の定義文 ex. "id integer primary key autoincrement"
     * @param otherColumnData 二列目以降の定義文
     */
    protected final void createTable(SQLiteDatabase db, String table, String columnData, String... otherColumnData) {
        StringBuilder sql = new StringBuilder("create table ");
        sql.append(table)
                .append("(")
                .append(columnData);
        for (String column : otherColumnData) {
            sql.append(",")
                    .append(column);
        }
        sql.append(")");
        db.execSQL(sql.toString());
    }

    /**
     * データベース名は以前に使われたものと同一だがバージョン数が以前と異なるという場合に、データベースへのアクセスがあったときに実行されるメソッドです。<br />
     * queryメソッドやexecuteメソッドなどによって実際にアクセスされたときであり、インスタンス作成時ではないことに注意してください。<br />
     * このメソッドでは単にonCreateメソッドを呼び出すのみとなっています。
     * 古いバージョンにあるテーブルの削除及びテーブルデータの移動は行いませんので、必要であればオーバーライドして実装してください。
     *
     * @param db         データベースのインスタンス
     * @param oldVersion 古いバージョン数
     * @param newVersion 新しいバージョン数
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    /**
     * このクラスによって作成されたすべてのCursorをクローズします
     */
    @Override
    public void close() {
        for (Cursor cursor : mCursorStack) {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public void deleteDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        File file = new File(db.getPath());
        SQLiteDatabase.deleteDatabase(file);
    }

    /**
     * 結果を返さないデータベース操作を行います
     *
     * @param sql SQL文
     * @throws SQLiteException 書き込み用データベースが開けなかった場合、SQL文が不正の場合
     */
    public final void execute(String sql) {
        Log.d(TAG_SQL, sql);

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
    }

    /**
     * 条件に合致したレコードの値を更新します
     *
     * @param table       テーブル名
     * @param values      列の名前から列の新しい値へのマップ。nullは正当な値であり、NULLに変換されます
     * @param whereClause 条件句
     * @param whereArgs   条件句に？が含まれているなら、？を置き換える文字列
     * @return 変更された行数
     * @throws SQLiteException 書き込み用データベースが開けなかった場合
     */
    public final int update(String table, ContentValues values, String whereClause, String... whereArgs) {
        Log.d(TAG_SQL, "table : " + table + " -> " + values.toString());

        SQLiteDatabase db = getWritableDatabase();
        return db.update(table, values, whereClause, whereArgs);
    }

    /**
     * 新しく行を追加します
     *
     * @param table  テーブル名
     * @param values 列の名前から列の新しい値へのマップ
     * @return 新しく挿入された行のID。エラーが起こった場合は-1
     */
    public final long insert(String table, ContentValues values) {
        Log.d(TAG_SQL, values.toString());

        SQLiteDatabase db = getWritableDatabase();
        return db.insert(table, null, values);
    }

    /**
     * 条件に合致したレコードを削除します
     *
     * @param table       テーブル名
     * @param whereClause 条件句
     * @param whereArgs   条件句に？が含まれているなら、？を置き換える文字列
     * @return 削除した行数？
     */
    public final int delete(String table, String whereClause, String... whereArgs) {
        // TODO: @returnが正しいのか確認
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(table, whereClause, whereArgs);
    }

    /**
     * 指定されたテーブルを削除します
     *
     * @param table テーブル名
     * @throws SQLiteException 書き込み用データベースが開けなかった場合
     */
    public final void deleteTable(String table) {
        SQLiteDatabase db = getWritableDatabase();
        deleteTable(db, table);
    }

    protected final void deleteTable(SQLiteDatabase db, String table) {
        db.execSQL("drop table if exists " + table);
    }

    /**
     * 指定されたテーブル内のすべてのレコードを削除します
     *
     * @param table テーブル名
     * @return 1が返る?
     * @throws SQLiteException 書き込み用データベースが開けなかった場合
     */
    public final int clear(String table) {
        // TODO: 戻り値がどうなるのか確認
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(table, null, EMPTY_STRING_ARRAY);
    }

    /**
     * 結果を受け取る必要のあるSQL文を実行します
     *
     * @param sql           SQL文
     * @param selectionArgs もしSQL文が？を含むなら、その？を置き換える文字列
     * @return 結果を取得するためのカーソルインスタンス
     * @throws SQLiteException データベースを開けなかった場合
     */
    public final Cursor query(String sql, String... selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        mCursorStack.push(cursor);
        return cursor;
    }

    /**
     * 条件に合致した行の全列を取得するカーソルを返します
     *
     * @param table       テーブル名
     * @param whereClause 条件句
     * @param whereArgs   条件句に？が含まれているなら、？を置き換える文字列
     * @return 結果を取得するためのカーソルインスタンス
     */
    public final Cursor selectAllColumn(String table, String whereClause, String... whereArgs) {
        StringBuilder sql = new StringBuilder("select * from ")
                .append(table)
                .append(" where ")
                .append(whereClause);
        return query(sql.toString(), whereArgs);
    }

    /**
     * すべての行のすべての列を取得するカーソルを返します
     *
     * @param table テーブル名
     * @return 結果を取得するためのカーソルインスタンス
     */
    public final Cursor selectAll(String table) {
        return query("select * from " + table);
    }

    /**
     * @param table
     * @param targetColumn
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public final int min(String table, String targetColumn, String whereClause, String... whereArgs) {
        String sql = TextUtils.concat("select min(", targetColumn, ") from ", table, " where ", whereClause).toString();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, whereArgs)) {
            if (!cursor.moveToNext()) {
                throw new SQLiteException("no data min from " + table + " where " + whereClause + " with " + Arrays.toString(whereArgs));
            }
            return cursor.getInt(0);
        }
    }

    public final int max(String table, String targetColumn, String whereClause, String... whereArgs) {
        String sql = TextUtils.concat("select max(", targetColumn, ") from ", table, " where ", whereClause).toString();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, whereArgs)) {
            if (!cursor.moveToNext()) {
                throw new SQLiteException("no data max from " + table + " where " + whereClause + " with " + Arrays.toString(whereArgs));
            }
            return cursor.getInt(0);
        }
    }

    public final boolean isExist(String table, String whereClause, String... whereArgs) {
        String sql = TextUtils.concat("select * from ", table, " where ", whereClause).toString();
        try (Cursor cursor = query(sql, whereArgs)) {
            return cursor.moveToFirst();
        }
    }

    /**
     * 最新のカーソルを次に進めます
     *
     * @return カーソルを次に進めるのに成功したかどうか
     */
    public final boolean next() {
        Cursor cursor = mCursorStack.peek();
        return cursor.moveToNext();
    }

    /**
     * 最新のカーソルの現在位置から、指定した列名の値を文字列で取り出します
     *
     * @param column 列名
     * @return 結果
     * @throws IllegalArgumentException                         存在しない列名が渡された場合
     * @throws android.database.CursorIndexOutOfBoundsException カーソルが正当な位置にない場合(next()を呼び出していないなど)
     */
    public final String getString(String column) {
        Cursor cursor = mCursorStack.peek();
        int index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(index);
    }

    /**
     * 最新のカーソルの現在位置から、指定した列名の値を整数で取り出します
     *
     * @param column 列名
     * @return 結果
     * @throws IllegalArgumentException                         存在しない列名が渡された場合
     * @throws android.database.CursorIndexOutOfBoundsException カーソルが正当な位置にない場合(next()を呼び出していないなど)
     */
    public final int getInt(String column) {
        Cursor cursor = mCursorStack.peek();
        int index = cursor.getColumnIndexOrThrow(column);
//        if (index == -1) {
//            throw new IllegalArgumentException("no columns " + column + " in " + Arrays.format(cursor.getColumnNames()));
//        }
//        Log.d("DATABASE", "getInt("+ column + ") index:"+ index + " " + Arrays.format(cursor.getColumnNames()) + " type: " + cursor.getType(0));
        return cursor.getInt(index);
    }

    public final long getLong(String column) {
        Cursor cursor = mCursorStack.peek();
        int index = cursor.getColumnIndexOrThrow(column);
        return cursor.getLong(index);
    }

    public final int getCount() {
        Cursor cursor = mCursorStack.peek();
        return cursor.getCount();
    }

    public SQLiteDatabase beginTransaction() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        return db;
    }
}
