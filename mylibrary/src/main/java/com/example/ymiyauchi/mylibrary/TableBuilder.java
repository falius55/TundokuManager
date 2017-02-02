package com.example.ymiyauchi.mylibrary;

import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by ymiyauchi on 2017/01/28.
 * <p>
 * データを格納し、表を作成するクラス
 * {@code
 * // コンストラクタに列名を渡してインスタンスを作成
 * TableBuilder tb = new TableBuilder("名前","性別","年齢");
 * // insert(String)で挿入行を指定して内部クラスのインスタンスを取得
 * // さらに続けてadd(String, Object)で挿入行とデータを渡す
 * tb.insert("Anna")
 * .add("性別", "女")
 * .add("年齢", 16);
 * tb.insert("Alex")
 * .add("性別", "男")
 * .add("年齢", 21);
 * tb.insert("Kai")
 * .add(1, "男")
 * .add(2, 12)
 * .add(0, "カイ"); // 最初の行の値を変更すると、識別名とは別に変更される
 * <p>
 * Object alex = new Object() {
 * String name = "Alex";
 * <p>
 * public String toString() {
 * return name;
 * }
 * };
 * tb.insert("Anna").add(3,'A');
 * // オブジェクトを行の識別名として使用することもできる。特に一行目の名前を指定していなければtoString()の戻り値が行名として使われる
 * tb.insert(alex).add("イニシャル", "A");
 * tb.insert("Kai").add(3, 'K');
 * tb.print();
 * }
 * 上の例で作成される表
 * +-----+-----+-----+-----------+
 * |名前 |性別 |年齢 |イニシャル |
 * |Anna |女   |16   |A          |
 * |Alex |男   |21   |A          |
 * |カイ |男   |12   |K          |
 * +-----+-----+-----+-----------+
 */

public class TableBuilder {
    // 行の名前(１列目のデータ)からの、列名からデータへのマップ、へのマップ
    private final Map<String, Map<String, Object>> mDataMap;
    private final String mFirstColumn;
    private final List<String> mColumns; // 列名のリスト

    /**
     * @param firstColumn 一列目の名前。各行のタイトルになる
     * @param column      二列目以降各列の名前
     */
    public TableBuilder(String firstColumn, String... column) {
        mDataMap = new ArrayMap<>();
        this.mFirstColumn = firstColumn;
        mColumns = new ArrayList<>();
        mColumns.add(firstColumn);
        mColumns.addAll(Arrays.asList(column));
    }

    /**
     * 指定行へのデータ追加を開始します
     * 識別名を引数で指定し、以降は同じ識別名なら同一行として扱います
     * 初めて指定される識別名は自動的に一行目のデータとして扱われますが、add()の第一引数に一列目の名称または0を指定することで変更することも可能です
     * 一列目のデータを変更したとしても、識別名として使用されるオブジェクトが変わることはありません
     *
     * @param rowTitleObj 追加行の識別名を文字列表現として持つオブジェクト。初めて挿入する識別名なら自動的に一列目のデータとして挿入される
     * @return 指定された行へのデータ挿入を請け負う内部クラスのインスタンス
     */
    public InsertAgency insert(Object rowTitleObj) {
        String rowTitle = rowTitleObj.toString();
        if (mDataMap.containsKey(rowTitle)) {
            return new InsertAgency(mColumns, mDataMap.get(rowTitle));
        }

        Map<String, Object> newData = new ArrayMap<>();
        newData.put(mFirstColumn, rowTitle);
        mDataMap.put(rowTitle, newData);
        return new InsertAgency(mColumns, newData);
    }

    /**
     * 指定行の識別名で指定された列にデータを追加します
     * 一列目のデータを変更しても、insert(String)に使用する識別名は変わりません
     *
     * @param rowTitleObj 追加行の識別名を文字列表現として持つオブジェクト。初めて挿入する識別名なら自動的に一列目のデータとして挿入される
     * @param column      追加する列の名前
     * @param data        追加するデータ
     * @return 指定された行へのデータ挿入を請け負う内部クラスのインスタンス
     */
    public InsertAgency add(Object rowTitleObj, String column, Object data) {
        return insert(rowTitleObj).add(column, data);
    }

    /**
     * 指定行のインデックスで指定された列にデータを追加します
     * 一列目のデータを変更しても、insert(String)に使用するキーは変わりません
     *
     * @param rowTitleObj 追加行の識別名を文字列表現として持つオブジェクト。初めて挿入する識別名なら自動的に一列目のデータとして挿入される
     * @param columnIndex 追加する列のインデックス(０なら一列目)
     * @param data        追加するデータ
     * @return 指定された行へのデータ挿入を請け負う内部クラスのインスタンス
     */
    public InsertAgency add(Object rowTitleObj, int columnIndex, Object data) {
        return insert(rowTitleObj).add(columnIndex, data);
    }

    /**
     * テーブルを作成します
     *
     * @return 作成されたテーブルを一行ずつ格納したリスト
     */
    public List<String> build() {
        Map<String, Integer> lenMap = columnLenMap();

        // 区切り文字列及びヘッダ文字列を作成する。
        StringBuilder separatorBuf = new StringBuilder("+");
        StringBuilder header = new StringBuilder("|");
        for (String column : mColumns) {
            separatorBuf.append(padding("", lenMap.get(column), '-')).append('+');
            header.append(padding(column, lenMap.get(column))).append('|');
        }
        String separator = separatorBuf.toString();

        List<String> table = new ArrayList<>(); // 結果を格納するリスト
        table.add(separator);
        table.add(header.toString());

        // 各行の文字列を作成する
        for (Map<String, Object> row : mDataMap.values())
            table.add(toRowString(lenMap, row));
        table.add(separator);
        return table;
    }

    /**
     * 表のデータ部の一行を表す文字列を作成します
     *
     * @param lenMap 各列の最大の長さを格納したマップ
     * @param row    行の各列のデータを格納したマップ
     */
    private String toRowString(Map<String, Integer> lenMap, Map<String, Object> row) {
        StringBuilder sbRow = new StringBuilder("|");
        for (String column : mColumns) {
            String data = Objects.toString(row.get(column), "");
            sbRow.append(padding(data, lenMap.get(column))).append('|');
        }
        return sbRow.toString();
    }

    /**
     * 標準出力に表を出力します
     *
     * @return このオブジェクトの参照
     */
    public TableBuilder print() {
        for (String line : build())
            System.out.println(line);
        return this;
    }

    /**
     * 各列から、その列の最大の長さへのマップを作成します
     *
     * @return 列名をkeyにして、valueにその列の最大の長さが格納されたマップ
     */
    private Map<String, Integer> columnLenMap() {
        Map<String, Integer> lenMap = new ArrayMap<>();
        for (String column : mColumns) {
            final Set<Integer> dataLenSet = new HashSet<>();
            columnEach(column, new Consumer<Object>() {
                @Override
                public void accept(Object arg) {
                    int len = length(Objects.toString(arg, ""));
                    dataLenSet.add(len);
                }
            }, true);
            int maxLen = 0;
            for (int len : dataLenSet) {
                maxLen = Math.max(len, maxLen);
            }
            lenMap.put(column, maxLen);
        }
        return lenMap;
    }

    /**
     * 指定した列のデータをイテレートして処理を適用します
     *
     * @param column       イテレートする列名
     * @param func         適用する処理を定義した関数型インターフェース
     * @param includeTitle 処理対象に列名自体まで含めるかどうかを指定する(trueなら含める)
     */
    private void columnEach(String column, Consumer<Object> func, boolean includeTitle) {
        if (includeTitle) func.accept(column);
        for (Map<String, Object> rowData : mDataMap.values()) {
            func.accept(rowData.get(column));
        }
    }

    private void columnEach(String column, Consumer<Object> func) {
        columnEach(column, func, false);
    }

    private interface Consumer<T> {
        void accept(T arg);
    }

    /**
     * 文字列の長さを取得します(半角文字の何文字分か)
     */
    private int length(String str) {
        int length = 0;
        for (int i = 0; i < str.length(); i++) {
            String s = str.substring(i, i + 1);
            // 全角文字は３バイトとして出てくるが、長さは半角２文字分
            length += s.getBytes().length == 1 ? 1 : 2;
        }
        return length;
    }

    /**
     * 空白でパディングします
     */
    private String padding(String str, int length) {
        return padding(str, length, ' ');
    }

    /**
     * 文字列の後ろに指定された文字を連結して、指定された長さになるまでパディングします
     *
     * @param str     パディングする文字列
     * @param length  パディング後の長さ
     * @param padChar パディングに使用する文字
     */
    private String padding(String str, int length, char padChar) {
        StringBuilder ret = new StringBuilder(str);
        int padCount = length - length(str) + 1;
        for (int i = 0; i < padCount; i++) ret.append(padChar);
        return ret.toString();
    }

    public static void main(String[] arg) {
        Object alex = new Object() {
            String name = "Alex";

            @Override
            public String toString() {
                return name;
            }
        };
        TableBuilder tb = new TableBuilder("名前", "性別", "年齢", "イニシャル");
        tb.insert("Anna")
                .add("性別", "女")
                .add("年齢", 16);
        tb.insert("Alex")
                .add("性別", "男")
                .add("年齢", 21);
        tb.insert("Kai")
                .add(1, "男")
                .add(2, 12)
                .add(0, "カイ");

        tb.insert("Anna").add(3, 'A');
        tb.insert(alex).add("イニシャル", "A");
        tb.insert("Kai").add(3, 'K');
        tb.print();
    }

    /**
     * データ挿入を請け負うクラス
     */
    public static class InsertAgency {
        private List<String> columnTitles;
        private Map<String, Object> row;

        /**
         * @param columnTitles 列名のリスト
         * @param row          追加行の各データのマップ
         */
        private InsertAgency(List<String> columnTitles, Map<String, Object> row) {
            this.columnTitles = columnTitles;
            this.row = row;
        }

        /**
         * 指定された列に、データを挿入します。nullが渡された場合は、その列のデータは空であるものとします
         *
         * @param column 挿入する列の名前
         * @param data   挿入するデータ。nullが渡されるとデータを空にする
         * @return このオブジェクトの参照
         * @throws IllegalArgumentException インスタンス作成時に設定した列名以外の名前の列に挿入しようとした場合
         */
        public InsertAgency add(String column, Object data) {
            if (!columnTitles.contains(column))
                throw new IllegalArgumentException(String.format("列名'%s'はこの表に設定されていません", column));
            row.put(column, data);
            return this;
        }

        /**
         * 指定されたインデックスの列に、データを挿入します
         *
         * @param columnIndex 挿入する列のインデックス
         * @param data        挿入するデータ。nullが渡されるとデータを空にする
         * @return このオブジェクトの参照
         * @throws IndexOutOfBoundsException 指定されたインデックスの列が設定されていない場合
         */
        public InsertAgency add(int columnIndex, Object data) {
            return add(columnTitles.get(columnIndex), data);
        }
    }
}
