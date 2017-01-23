package com.example.ymiyauchi.mylibrary.view.manager;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.ymiyauchi.mylibrary.listener.ObservableArrayList;
import com.example.ymiyauchi.mylibrary.listener.ObservableArrayMap;
import com.example.ymiyauchi.mylibrary.listener.ObservableList;
import com.example.ymiyauchi.mylibrary.listener.ObservableMap;

/**
 * Created by ymiyauchi on 2016/11/10.
 * <p>
 * <p>ListViewの支援クラスです<br>
 * インスタンスを作成してから、getAdapter()で返されるadapterをListViewのsetAdapterに渡します<br>
 * 渡すデータは各リストのビューがもつidをキーにしたMapであり、データを変更する度にリストビューに通知して変更を自動的に反映させます<br>
 * もしデータを渡さない場合には空のデータが作成され、各種メソッドでデータを操作できます</p>
 * <p>
 * <p>SimpleAdapterを直接扱う方法から、ListのMapにデータを入れる煩わしさをなくしてデータの受入方法を多様化しました</p>
 * <p>
 * {@code
 * <pre>
 *     // 基本的な使い方
 *     // 以下のように、MapにputしてListにaddするといったことを一切行わずに、一気にデータを作成してListViewに表示することができる
 *
 *     ListView listView = (ListView)findById(R.id.listView);
 *
 *     // コンストラクタはデータを渡さず空のListViewにする場合にだけ用意している。なお、この場合でも後からアイテムを追加できる
 *     // ContainerManager manager = new ContainerManager(this, R.id.list, new int[){R.id.name, R.id.age, R.id.place, R.id.is_student};
 *
 *     // 複数データを最初に用意してリストを表示させる場合にはstatic factoryメソッドを用いる
 *     ContainerManager manager = ContainerManager.create(
 *          this,  // Activityなどのコンテキスト
 *          R.id.list,  // 各リストアイテムのレイアウトをしたレイアウトID
 *          new int[]{R.id.name, R.id.age, R.id.place, R.id.is_student},  // R.id.list内にある各TextViewなどのID
 *          new Object[][]{  // ２元配列であればデータの型は何でも良いが(プリミティブはintのみ)、複数の型を混在させる場合にはObject型にする(表示はtoStringメソッドにより文字列化)
 *               {"taro", 12, "北海道", true},  // ひとつひとつが各リストに収まる。直前に渡した順番通り、R.id.name、R.id.age、R.id.placeに表示
 *               {"hanako", 20, "東京", false},
 *               {"jiro", 35, "福岡", false}
 *           }
 *     );
 *
 *     listView.setAdapter(manager.getAdapter());  // このクラス自体はadapterを継承していないので、getAdapterメソッドでadapterを取得してListViewに渡す
 *
 *     manager.addItem(new String{"saburo", "42", "名古屋", false});
 *     manager.updateItem(2, R.id.age, 32);  // jiroの年齢を32に変更。直接変更箇所を指定する場合はリソースIDを使う
 *     manager.updateItem(0, new Object[]{"taro", 13, "東京"});  // dataの数が足りないのは問題ない
 *     manager.updateItem(1, new int[]{R.id.name, R.id.place}, new String[]{"yamada hanako", "広島"});
 *
 *     // Cursorを渡すパターンでもあくまで追加。データベースの内容で総入れ替えしたいならclearメソッドでリストを空にしてから
 *     // manager.clear();
 *     try (AndroidDatabase db = new MyDatabase(this, DB_NAME)) {
 *          Cursor cursor = db.selectAll(TABLE_NAME);
 *          // 列名で使用する値を特定し、指定した順序で表示位置が決まる。
 *          // 例えばR.id.placeにはデータベースDB_NAMEのテーブルTABLE_NAMEにあるaddressという列名の値が表示されることになる
 *          // また、真偽値をintegerを使ってデータベースに保存していると文字列が０と１になってしまうので、データベースには"true"と"false"をtextで保存するのが望ましい
 *           manager.addItem(cursor, "name", "age", "address", "is_student");
 *     }
 *
 *     String hanakoName = manager.get(1, R.id.name);
 *     int jiroAge = Integer.parseInt(manager.get(2, R.id.age));  // リストの値は文字列で取得。適宜パースすること
 * </pre>
 * }
 */
/*
 * このクラスではListViewListenersを保持していないが、originalDataのObservableがListenerを保持している(ListViewListener内で自らをObservableにaddしている)
 * このクラスではまずadapterに渡すためのデータを新たに作成し、その後は主にoriginalDataの操作を行う(つまり、データの受入を担当)
 * adapterの操作はコールバックを通じてListenersで行う
 */
public final class SimpleContainerManager implements ContainerManager {
    /*
     * ViewIDから値へのマップのリスト
     * リストひとつが１行を表す（１行のデータをマップで表している）
     * mOriginalDataに操作を行うことでListenerに通知がいき、その先でadapterを操作する
     */
    private final Context mContext;
    private final ObservableList<ObservableMap<Integer, String>> mOriginalData;
    private final SimpleAdapter adapter;
    @IdRes
    private final int[] mTo;

    /**
     * @param context      リストビューを配下に持つアクティビティ
     * @param listResource リストビューの各アイテムの配置をレイアウトしたレイアウトID
     * @param data         各リストのビューが持つidをキーにしたMapのリスト。Mapひとつがリストアイテム一つに対応します。また、リストとマップはすべてObservableである必要があります
     * @param to           リストにある各widgetのid
     */
    private SimpleContainerManager(Context context, @LayoutRes int listResource, ObservableList<ObservableMap<Integer, String>> data, @IdRes int[] to) {
        mContext = context;
        mOriginalData = data;
        mTo = to;
        /*
         * adapterに渡すデータは、キーがデータを表示するテキストビューのリソースIDの文字列、値が表示するデータの文字列
         */
        String[] from = from(to);
        List<Map<String, String>> adapterData = toStringKey(data);
        this.adapter = new SimpleAdapter(context, adapterData, listResource, from, to);

        new SimpleContainerManagerHelper(data, adapterData, adapter);
    }

    /**
     * リストアイテムが空の状態でリストビューをセットします
     *
     * @param context
     * @param listResource
     * @param to
     */
    public SimpleContainerManager(Context context, @LayoutRes int listResource, @IdRes int[] to) {
        this(context, listResource, new ObservableArrayList<ObservableMap<Integer, String>>(), to);
    }


    /*
     * データはすべてtoStringメソッドによって文字列化して表示される
     * ContainerManager.create(this, R.id.list, new int[}{R.id.name, R.id.age, R.id.place},
     *  new Object[][]{
     *      {"taro", 12, "北海道"},  // リストアイテム１行分のデータ。toのインデックスと対応する
     *      {"hanako", 20, "東京"}
     *  }
     * );
     */
    public static <T> ContainerManager create(Context context, @LayoutRes int listResource, @IdRes int[] to, T[][] data) {
        return new SimpleContainerManager(context, listResource, createDataFrom(data, to), to);
    }

    public static ContainerManager create(Context context, @LayoutRes int listResource, @IdRes int[] to, int[][] data) {
        Integer[][] objData = new Integer[data.length][];
        for (int i = 0, iLength = data.length; i < iLength; i++) {
            Integer[] rowObjData = new Integer[data[i].length];
            for (int j = 0, jLength = data[i].length; j < jLength; j++) {
                rowObjData[j] = data[i][j];
            }
            objData[i] = rowObjData;
        }
        return create(context, listResource, to, objData);
    }

    @Override
    public SimpleAdapter getAdapter() {
        return this.adapter;
    }

    @Override
    public long getItemId(int position) {
        return adapter.getItemId(position);
    }

    private static <T> ObservableList<ObservableMap<Integer, String>> createDataFrom(T[][] data, int[] to) {
        ObservableList<ObservableMap<Integer, String>> ret = new ObservableArrayList<>();
        for (T[] row : data) {
            ret.add(createDataFrom(row, to));
        }
        return ret;
    }

    private static <T> ObservableMap<Integer, String> createDataFrom(T[] data, int[] to) {
        ObservableMap<Integer, String> ret = new ObservableArrayMap<>();
        for (int i = 0; i < to.length; i++) {
            ret.put(to[i], data[i].toString());
        }
        return ret;
    }


    // アダプタに渡すデータの、マップのキーの一覧
    // int配列をString配列に変換しているだけ
    private static String[] from(int[] to) {
        String[] ret = new String[to.length];
        for (int i = 0; i < to.length; i++) {
            ret[i] = Integer.toString(to[i]);
        }
        return ret;
    }

    /**
     * リスト内にあるマップのキーをすべて文字列に変換します
     *
     * @param list
     * @return
     */
    private static List<Map<String, String>> toStringKey(List<? extends Map<?, ?>> list) {
        List<Map<String, String>> ret = new ArrayList<>();
        for (Map<?, ?> map : list) {
            ret.add(toStringKeyMap(map));
        }
        return ret;
    }

    static Map<String, String> toStringKeyMap(Map<?, ?> map) {
        Map<String, String> ret = new ArrayMap<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            ret.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return ret;
    }


    /**
     * リストビューにアイテムを新規追加します
     *
     * @param to   追加先のウィジェットのID
     * @param data toの順番に対応する並びで、そのウィジェットに表示する文字列
     * @throws IllegalArgumentException to.length < data.lengthの場合
     */
    @Override
    public void addItem(@IdRes int[] to, Object[] data) {
        if (to.length < data.length)
            throw new IllegalArgumentException("max 'to' length is " + to.length + ". but data length is " + data.length);
        ObservableMap<Integer, String> newMap = new ObservableArrayMap<>();
        for (int i = 0; i < data.length; i++) {
            newMap.put(to[i], data[i].toString());
        }
        mOriginalData.add(newMap);
    }

    /**
     * サポートしていません
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void addItemWithID(@IdRes int[] to, Object[] data, long ID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addItem(@IdRes int[] to, int[] data) {
        addItem(to, toIntegerArray(data));
    }

    @Override
    public void addItem(Object[] data) {
        addItem(mTo, data);
    }

    @Override
    public void addItem(int[] data) {
        addItem(toIntegerArray(data));
    }

    /**
     * サポートしていません
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void addItemWithID(Object[] data, long ID) {
        throw new UnsupportedOperationException();
    }

    private Integer[] toIntegerArray(int[] array) {
        Integer[] objArray = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            objArray[i] = array[i];
        }
        return objArray;
    }

    /**
     * Cursorから取得できる値を全行追加します。列の値がnullの場合は空文字を表示します。
     *
     * @param cursor        データベース等から取得したカーソル
     * @param columnIndexes カーソルから得られる値のうち、ビューに表示する列のインデックス。引数に与えた順番の値が先に渡したtoと同じインデックスのリソースIDを持つウィジェットに表示されます
     * @throws IllegalArgumentException 列のインデックスの数が先に渡したリソースIDの総数よりも多い場合、指定されたインデックスの列が存在しない場合
     * @throws RuntimeException         Cursorから取得した値の型がblobその他予期しないものであった場合
     */
    @Override
    public void addAllItem(Cursor cursor, @IdRes int[] to, int... columnIndexes) {
        if (columnIndexes.length > to.length)
            throw new IllegalArgumentException("columnIndex(" + columnIndexes.length + ") more than Resources(" + to.length + ")");
        for (int columnIndex : columnIndexes) {
            if (columnIndex < 0 || columnIndex >= cursor.getColumnCount())
                throw new IllegalArgumentException("not exit columnIndex(" + columnIndex +
                        "), max column index is " + (cursor.getColumnCount() - 1));
        }

        List<ObservableMap<Integer, String>> addList = new ArrayList<>();
        while (cursor.moveToNext()) {
            ObservableMap<Integer, String> map = new ObservableArrayMap<>();
            for (int i = 0; i < columnIndexes.length; i++) {
                Object data;
                switch (cursor.getType(columnIndexes[i])) {
                    case Cursor.FIELD_TYPE_STRING:
                        data = cursor.getString(columnIndexes[i]);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        data = cursor.getInt(columnIndexes[i]);
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        data = cursor.getFloat(columnIndexes[i]);
                        break;
                    case Cursor.FIELD_TYPE_NULL:
                        data = "";
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                    default:
                        throw new RuntimeException("type blob or other cannot be displayed : columnIndex(" + columnIndexes[i] + ")");
                }
                map.put(to[i], data.toString());
            }
            addList.add(map);
        }
        mOriginalData.addAll(addList);
    }

    /**
     * {@inheritDoc}
     *
     * @param cursor
     * @param columns カーソルの列名
     * @throws IllegalArgumentException 存在しない列名が指定された場合
     */
    @Override
    public void addAllItem(Cursor cursor, String... columns) {
        addAllItem(cursor, toColumnIndexes(cursor, columns));
    }

    private int[] toColumnIndexes(Cursor cursor, String... columns) {
        int[] columnIndexes = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnIndexes[i] = cursor.getColumnIndexOrThrow(columns[i]);
        }
        return columnIndexes;
    }


    /**
     * リストビューからアイテムを消去します
     *
     * @param index 消去するアイテム０始まりのインデックス
     */
    @Override
    public void removeItem(int index) {
        mOriginalData.remove(index);
    }

    /**
     * 既存のアイテムの内容を変更します
     *
     * @param position アイテムのインデックス(０始まり)
     * @param to       書き換えるウィジェットのID。必ずしもすべてのウィジェットを網羅する必要はなく、ひとつだけでも構いません
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     * @throws IndexOutOfBoundsException positionが範囲外の場合、to.length < data.lengthの場合
     */
    @Override
    public void updateItem(int position, @IdRes int[] to, Object[] data) {
        if (position < 0 || position >= adapter.getCount())
            throw new IndexOutOfBoundsException("list item count is " + adapter.getCount() + ", but " + position);

        Map<Integer, String> putMap = new ArrayMap<>();
        for (int i = 0; i < data.length; i++) {
            putMap.put(to[i], data[i].toString());
        }
        mOriginalData.get(position).putAll(putMap);
    }

    @Override
    public void updateItem(int position, @IdRes int[] to, int[] data) {
        updateItem(position, to, toIntegerArray(data));
    }

    @Override
    public void updateItem(int position, Object[] data) {
        updateItem(position, mTo, data);
    }

    @Override
    public void updateItem(int position, int[] data) {
        updateItem(position, toIntegerArray(data));
    }

    /**
     * Cursorから取得できる値を、利用する列のインデックスを指定して全行追加します。列の値がnullの場合は空文字を表示します。
     *
     * @param cursor        データベース等から取得したカーソル
     * @param columnIndexes カーソルから得られる値のうち、ビューに表示する列のインデックス。引数に与えた順番の値がtoと同じインデックスのリソースIDを持つウィジェットに表示されます
     * @throws IllegalArgumentException 列のインデックスの数が先に渡したリソースIDの総数よりも多い場合、指定されたインデックスの列が存在しない場合
     * @throws RuntimeException         Cursorから取得した値の型がblobその他予期しないものであった場合
     */
    @Override
    public void addAllItem(Cursor cursor, int... columnIndexes) {
        addAllItem(cursor, mTo, columnIndexes);
    }

    /**
     * Cursorから取得できる値を、利用する列名を指定して全行追加します。列の値がnullの場合は空文字を表示します。
     *
     * @param cursor  データベース等から取得したカーソル
     * @param to      データの表示箇所を表すIdRes
     * @param columns カーソルの列名
     * @throws IllegalArgumentException 存在しない列名が指定された場合、列のインデックス賀の数が先に渡したリソースIDの総数よりも多い場合
     */
    @Override
    public void addAllItem(Cursor cursor, @IdRes int[] to, String... columns) {
        addAllItem(cursor, to, toColumnIndexes(cursor, columns));
    }

    @Override
    public void updateItem(int position, @IdRes int to, Object data) {
        mOriginalData.get(position).put(to, data.toString());
    }

    @Override
    public void updateItem(int position, @IdRes int to, @StringRes int resId, Object... formatArgs) {
        String data = mContext.getString(resId, formatArgs);
        updateItem(position, to, data);
    }

    @Override
    public int getCount() {
        return adapter.getCount();
    }

    public void clear() {
        mOriginalData.clear();
    }


    /**
     * {@inheritDoc}
     *
     * @param position  コンテナアイテムの位置を示すインデックス
     * @param widgetRes 指定されたアイテムビューに含まれている、取得するデータの場所を示すIdRes
     * @return
     */
    @Override
    public String getString(int position, @IdRes int widgetRes) {
        return mOriginalData.get(position).get(widgetRes);
    }

    /**
     * {@inheritDoc}
     *
     * @param position          コンテナアイテムの位置を示すインデックス
     * @param widgetRes         指定されたアイテムビューに含まれている、取得するデータの場所を示すIdRes
     * @param defaultIfNotFound 指定されたwidgetResを使ってViewを取得できなかった場合に戻り値となるデフォルト値
     * @return
     */
    @Override
    public String getString(int position, @IdRes int widgetRes, String defaultIfNotFound) {
        String result = getString(position, widgetRes);
        if (result == null)
            return defaultIfNotFound;
        return result;
    }

    @Override
    public int getInt(int position, @IdRes int widgetRes) {
        return Integer.parseInt(getString(position, widgetRes));
    }

    @Override
    public int getInt(int position, @IdRes int widgetRes, int defaultIfNotFound) {
        String value = getString(position, widgetRes);
        if (TextUtils.isDigitsOnly(value))
            return Integer.parseInt(value);
        return defaultIfNotFound;
    }
}