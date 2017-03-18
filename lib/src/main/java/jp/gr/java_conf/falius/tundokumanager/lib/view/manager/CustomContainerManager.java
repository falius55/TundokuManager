package jp.gr.java_conf.falius.tundokumanager.lib.view.manager;

import android.app.Activity;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.falius.tundokumanager.lib.view.containeradapter.CustomAdapter;

/**
 * Created by ymiyauchi on 2016/11/27.
 *
 * <p>
 * CustomAdapterのインターフェースを拡張するためのクラスです。
 * データの受入方法をCustomAdapterのSparseArray方式から、配列とCursorオブジェクトを使う方法に変更しています。
 * getAdapterメソッドにて取得したAdapterをViewGroupに登録してください。
 * また、データ変更メソッドを実行すると自動で登録しているViewGroupに変更が通知されて反映されます。
 *
 * <p/>
 * {@code
 * <pre>
 *     // 基本的な使い方
 *     ListView listView = (ListView)findById(R.id.listView);
 *
 *     // コンストラクタはデータを渡さず空のListViewにする場合にだけ用意している。なお、この場合でも後からアイテムを追加できる
 *     // ContainerManager manager = new CustomContainerManager(this, R.id.list, new int[]{R.id.name, R.id.age, R.id.place, R.id.is_student});
 *
 *     // 複数データを最初に用意してリストを表示させる場合にはstatic factoryメソッドを用いる
 *     ContainerManager manager = CustomContainerManager.create(
 *          this,  // Activityなどのコンテキスト
 *          R.id.list,  // 各リストアイテムのレイアウトを定義したレイアウトID
 *          new int[]{ R.id.name, R.id.age, R.id.place, R.id.is_student },  // R.id.list内にある各TextViewなどのID
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
 *     manager.clear();
 *     try (AndroidDatabase db = new MyDatabase(this, DB_NAME)) {
 *          Cursor cursor = db.selectAll(TABLE_NAME);
 *          // 列名で使用する値を特定し、指定した順序で表示位置が決まる。
 *          // 例えばR.id.placeにはデータベースDB_NAMEのテーブルTABLE_NAMEにあるaddressという列名の値が表示されることになる
 *          // また、真偽値をintegerを使ってデータベースに保存していると文字列が０と１になってしまうので、データベースには"true"と"false"をtextで保存するのが望ましい
 *           manager.addItem(cursor, "name", "age", "address", "is_student");
 *     }
 *
 *     // アイテムのインデックスと、値が表示されているウィジェットのIDでデータを特定して取得
 *     String hanakoName = manager.getString(1, R.id.name);
 *     int jiroAge = manager.getInt(2, R.id.age);
 * </pre>
 * }
 */

public final class CustomContainerManager implements ContainerManager {
    private final static String ID_COLUMN = BaseColumns._ID;

    private final Activity mActivity;
    private final List<SparseArray<Object>> mData;
    private final
    @IdRes
    int[] mTo;
    private final CustomAdapter mAdapter;

    private final Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.notifyDataSetChanged();
        }
    };

    private CustomContainerManager(Activity activity, @LayoutRes int itemResource, List<SparseArray<Object>> data, @IdRes int[] to) {
        mActivity = activity;
        mData = data;
        mTo = to;
        mAdapter = new CustomAdapter(activity, itemResource, to, data);
    }

    /**
     * アイテムが空の状態でセットします
     *
     * @param activity     コンテキスト
     * @param itemResource 各アイテムのレイアウトリソースID
     * @param to           itemResource内の各表示箇所のViewID
     */
    public CustomContainerManager(Activity activity, @LayoutRes int itemResource, @IdRes int[] to) {
        this(activity, itemResource, new ArrayList<SparseArray<Object>>(), to);
    }

    public static ContainerManager create(Activity activity, @LayoutRes int listResource, @IdRes int[] to, int[][] data) {
        Integer[][] objData = new Integer[data.length][];
        for (int i = 0, iLength = data.length; i < iLength; i++) {
            objData[i] = toIntegerArray(data[i]);
        }
        return create(activity, listResource, to, objData);
    }

    public static <T> CustomContainerManager create(Activity activity, @LayoutRes int listResource, @IdRes int[] to, T[][] data) {
        return new CustomContainerManager(activity, listResource, createDataFrom(data, to), to);
    }

    private static <T> List<SparseArray<Object>> createDataFrom(T[][] data, @IdRes int[] to) {
        List<SparseArray<Object>> ret = new ArrayList<>();
        for (T[] itemSource : data) {
            SparseArray<Object> item = new SparseArray<>(to.length + 1);
            for (int i = 0, len = itemSource.length; i < len; i++) {
                item.put(to[i], itemSource[i]);
            }
            ret.add(item);
        }
        return ret;
    }

    /**
     * Adapterを取得します
     *
     * @return Adapter
     */
    @Override
    public CustomAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 指定された位置のコンテナアイテムにつけられたIDを取得します
     *
     * @param position IDを取得するアイテムの位置を示すインデックス
     * @return アイテムに対応づけられたID
     */
    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    public CustomContainerManager specifyViewSetter(@IdRes int[] viewIDs, CustomAdapter.ViewSetter[] viewTypes) {
        CustomAdapter adapter = mAdapter;
        for (int i = 0, len = viewTypes.length; i < len; i++) {
            adapter.specifyViewSetter(viewIDs[i], viewTypes[i]);
        }
        return this;
    }

    /**
     * コンテナに表示箇所を指定してアイテムを新規追加します
     *
     * @param to   追加先のウィジェットのID
     * @param data 先に渡したtoの順番に対応する並びで、そのウィジェットに表示するデータ
     * @throws IllegalArgumentException to.length < data.lengthの場合
     */
    @Override
    public void addItem(@IdRes int[] to, Object[] data) {
        if (to.length < data.length)
            throw new IllegalArgumentException("many data. 'to' max length is " + to.length + ". but data length is " + data.length);

        SparseArray<Object> newData = new SparseArray<>(mTo.length + 1);
        for (int i = 0, len = data.length; i < len; i++) {
            newData.put(to[i], data[i]);
        }
        mData.add(newData);
        notifyDataSetChanged();
    }

    @Override
    public void addItem(@IdRes int[] to, int[] data) {
        addItem(to, toIntegerArray(data));
    }

    /**
     * コンテナにアイテムを新規追加します
     *
     * @param data 先に渡したtoの順番に対応する並びで、そのウィジェットに表示するデータ
     * @throws IllegalArgumentException to.length < data.lengthの場合
     */
    @Override
    public void addItem(Object[] data) {
        addItem(mTo, data);
    }

    @Override
    public void addItem(int[] data) {
        addItem(toIntegerArray(data));
    }

    /**
     * コンテナに表示箇所を指定してアイテムを新規追加し、このアイテムにIDをつけます(オプションの操作)
     *
     * @param to   追加先のウィジェットのID
     * @param data 先に渡したtoの順番に対応する並びで、そのウィジェットに表示するデータを表すオブジェクト
     * @param ID   追加するアイテムにつけるID
     * @throws IllegalArgumentException to.length < data.lengthの場合
     */
    @Override
    public void addItemWithID(@IdRes int[] to, Object[] data, long ID) {
        addItem(to, data);
        SparseArray<Object> item = mData.get(mData.size() - 1);
        item.put(CustomAdapter.ID_KEY, ID);
    }

    /**
     * コンテナにアイテムを新規追加し、このアイテムにIDをつけます(オプションの操作)
     *
     * @param data 先に渡したtoの順番に対応する並びで、そのウィジェットに表示するデータを表すオブジェクト
     * @param ID   追加するアイテムにつけるID
     * @throws IllegalArgumentException to.length < data.lengthの場合
     */
    @Override
    public void addItemWithID(Object[] data, long ID) {
        addItem(data);
        SparseArray<Object> item = mData.get(mData.size() - 1);
        item.put(CustomAdapter.ID_KEY, ID);
    }

    /**
     * {@inheritDoc}
     * <p>
     * カーソルにidという名前の列名で型がIntegerである列が存在する場合には、その値をIDとして扱います
     *
     * @param cursor        データベース等から取得したカーソル
     * @param to            データの表示箇所を表すIdRes
     * @param columnIndexes カーソルから得られる値のうち、ビューに表示する列のインデックス。引数に与えた順番の値がtoと同じインデックスのリソースIDを持つウィジェットに表示されます
     * @throws IllegalArgumentException 列のインデックスの数が先に渡したリソースIDの総数よりも多い場合、指定されたインデックスの列が存在しない場合
     * @throws RuntimeException         Cursorから取得した値の型がblobその他予期しないものであった場合
     */
    @Override
    public void addAllItem(Cursor cursor, @IdRes int[] to, int... columnIndexes) {
        if (columnIndexes.length > to.length)
            throw new IllegalArgumentException("columnIndex(" + columnIndexes.length + ") more than Resources(" + to.length + ")");
        for (int columnIndex : columnIndexes) {
            if (columnIndex < 0 || columnIndex >= cursor.getColumnCount())
                throw new IllegalArgumentException("not exit columnIndex(" + columnIndex + "), max column index is " + (cursor.getColumnCount() - 1));
        }

        List<SparseArray<Object>> newData = new ArrayList<>();
        while (cursor.moveToNext()) {
            SparseArray<Object> item = new SparseArray<>(to.length + 1);  // +1はIDの分

            int idColumnIndex = cursor.getColumnIndex(ID_COLUMN);
            if (idColumnIndex >= 0 && cursor.getType(idColumnIndex) == Cursor.FIELD_TYPE_INTEGER) {
                item.put(CustomAdapter.ID_KEY, cursor.getLong(idColumnIndex));
            }

            for (int i = 0; i < columnIndexes.length; i++) {
                Object data = getObjectFrom(cursor, columnIndexes[i]);
                item.put(to[i], data);
            }
            newData.add(item);
        }
        mData.addAll(newData);
        notifyDataSetChanged();
    }

    /**
     * カーソルから値をオブジェクト型で取り出します
     *
     * @param cursor
     * @param columnIndex
     * @return
     */
    private Object getObjectFrom(Cursor cursor, int columnIndex) {
        switch (cursor.getType(columnIndex)) {
            case Cursor.FIELD_TYPE_STRING:
                return cursor.getString(columnIndex);
            case Cursor.FIELD_TYPE_INTEGER:
                return cursor.getInt(columnIndex);
            case Cursor.FIELD_TYPE_FLOAT:
                return cursor.getFloat(columnIndex);
            case Cursor.FIELD_TYPE_NULL:
                return "";
            case Cursor.FIELD_TYPE_BLOB:
            default:
                throw new RuntimeException("type blob or other cannot be displayed : columnIndex(" + columnIndex + ")");
        }
    }

    /**
     * Cursorから取得できる値を、利用する列のインデックスを指定して全行追加します。列の値がnullの場合は空文字を表示します。
     *
     * @param cursor        データベース等から取得したカーソル
     * @param columnIndexes カーソルから得られる値のうち、ビューに表示する列のインデックス。引数に与えた順番の値が先に渡したtoと同じインデックスのリソースIDを持つウィジェットに表示されます
     * @throws IllegalArgumentException 列のインデックスの数が先に渡したリソースIDの総数よりも多い場合、指定されたインデックスの列が存在しない場合
     * @throws RuntimeException         Cursorから取得した値の型がblobその他予期しないものであった場合
     */
    @Override
    public void addAllItem(Cursor cursor, int... columnIndexes) {
        addAllItem(cursor, mTo, columnIndexes);
    }

    /**
     * {@inheritDoc}
     *
     * @param cursor  データベース等から取得したカーソル
     * @param to      データの表示箇所を表すIdRes
     * @param columns カーソルの列名
     * @throws IllegalArgumentException 列のインデックスの数が先に渡したリソースIDの総数よりも多い場合、存在しない列名が指定された場合
     * @throws RuntimeException         Cursorから取得した値の型がblobその他予期しないものであった場合
     */
    @Override
    public void addAllItem(Cursor cursor, @IdRes int[] to, String... columns) {
        int[] columnIndexes = toColumnIndexes(cursor, columns);
        addAllItem(cursor, to, columnIndexes);
    }

    /**
     * Cursorから取得できる値を、利用する列名を指定して全行追加します。列の値がnullの場合は空文字を表示します。
     *
     * @param cursor  データベース等から取得したカーソル
     * @param columns カーソルの列名
     * @throws IllegalArgumentException 列のインデックスの数が先に渡したリソースIDの総数よりも多い場合、存在しない列名が指定された場合
     * @throws RuntimeException         Cursorから取得した値の型がblobその他予期しないものであった場合
     */
    @Override
    public void addAllItem(Cursor cursor, String... columns) {
        int[] columnIndexes = toColumnIndexes(cursor, columns);
        addAllItem(cursor, columnIndexes);
    }

    /**
     * 列名の集合を列のインデックスの配列に変換します
     *
     * @param cursor  カーソル
     * @param columns 　列名
     * @return 列のインデックスの配列
     * @throws IllegalArgumentException 存在しない列名が指定された場合
     */
    private int[] toColumnIndexes(Cursor cursor, String... columns) {
        int[] columnIndexes = new int[columns.length];
        for (int i = 0, len = columns.length; i < len; i++) {
            columnIndexes[i] = cursor.getColumnIndexOrThrow(columns[i]);
        }
        return columnIndexes;
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
        if (to.length < data.length)
            throw new IndexOutOfBoundsException("max 'to' length is " + to.length + ", but data length is " + data.length);

        SparseArray<Object> item = mData.get(position);
        for (int i = 0, len = data.length; i < len; i++) {
            item.put(to[i], data[i]);
        }
        notifyDataSetChanged();
    }

    /**
     * 既存のアイテムの内容を指定されたint値のデータに変更します
     *
     * @param position アイテムのインデックス(０始まり)
     * @param to       書き換えるウィジェットのID。必ずしもすべてのウィジェットを網羅する必要はなく、ひとつだけでも構いません
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     * @throws IndexOutOfBoundsException positionが範囲外の場合、to.length < data.lengthの場合
     */
    @Override
    public void updateItem(int position, @IdRes int[] to, int[] data) {
        updateItem(position, to, toIntegerArray(data));
    }

    private static Integer[] toIntegerArray(int[] array) {
        Integer[] ret = new Integer[array.length];
        for (int i = 0, len = array.length; i < len; i++) {
            ret[i] = array[i];
        }
        return ret;
    }

    /**
     * 既存のアイテムの内容を変更します
     *
     * @param position アイテムのインデックス(０始まり)
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     * @throws IndexOutOfBoundsException positionが範囲外の場合、to.length < data.lengthの場合
     */
    @Override
    public void updateItem(int position, Object[] data) {
        updateItem(position, mTo, data);
    }

    /**
     * あらかじめ指定されたtoの順序に従って、既存のアイテムの内容を変更します。dataの内容をtoのひとつめから詰めていくようにして表示します。
     * dataの数がtoの数より少なかった場合は、dataの内容を使い切った残りのIdResから得られるウィジェットには何もしません。
     *
     * @param position アイテムのインデックス(０始まり)
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     * @throws IndexOutOfBoundsException positionが範囲外の場合、to.length < data.lengthの場合
     */
    @Override
    public void updateItem(int position, int[] data) {
        updateItem(position, mTo, data);
    }

    /**
     * 指定された位置にあるコンテナアイテムの指定箇所の内容を変更します
     *
     * @param position アイテムのインデックス(０始まり)
     * @param to       データの表示箇所のIdRes
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     */
    @Override
    public void updateItem(int position, @IdRes int to, Object data) {
        SparseArray<Object> item = mData.get(position);
        item.put(to, data);
        notifyDataSetChanged();
    }

    @Override
    public void updateItem(int position, @IdRes int to, @StringRes int resId, Object... formatArgs) {
        String data = mActivity.getString(resId, formatArgs);
        updateItem(position, to, data);
    }

    /**
     * {@inheritDoc}
     *
     * @return アイテム総数
     */
    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    /**
     * コンテナから指定されたアイテムを消去します
     *
     * @param position 消去するアイテム０始まりのインデックス
     */
    @Override
    public void removeItem(int position) {
        mData.remove(position);
        notifyDataSetChanged();
    }

    /**
     * コンテナの中にあるアイテムを空にします
     */
    @Override
    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    /**
     * 指定されたデータを文字列として取得します
     *
     * @param position  コンテナアイテムの位置を示すインデックス
     * @param widgetRes 指定されたアイテムビューに含まれている、取得するデータの場所を示すIdRes
     * @return 取得できた文字列。取得に失敗するとnull
     * @throws IndexOutOfBoundsException 指定されたpositionにアイテムが存在しない場合(positionが負の数である場合も含む)
     */
    @Override
    public String getString(int position, @IdRes int widgetRes) {
        SparseArray<Object> itemData = mData.get(position);
        Object targetData = itemData.get(widgetRes, null);
        if (targetData == null) {
            return null;
        } else {
            return targetData.toString();
        }
    }

    /**
     * 指定されたデータを文字列として取得し、もし指定のViewがコンテナアイテム内に存在しなかった場合には指定されたデフォルト値を返します。
     * なお、デフォルト値にかかわらず範囲外のpositionを指定した場合にはIndexOutOfExceptionが投げられます
     *
     * @param position          コンテナアイテムの位置を示すインデックス
     * @param widgetRes         指定されたアイテムビューに含まれている、取得するデータの場所を示すIdRes
     * @param defaultIfNotFound 指定されたwidgetResを使ってViewを取得できなかった場合に戻り値となるデフォルト値
     * @return 取得できた文字列、あるいは指定されたデフォルト値
     * @throws IndexOutOfBoundsException 指定されたpositionにアイテムが存在しない場合(positionが負の数である場合も含む)
     */
    @Override
    public String getString(int position, @IdRes int widgetRes, String defaultIfNotFound) {
        String result = getString(position, widgetRes);
        if (result == null)
            return defaultIfNotFound;
        return result;
    }

    /**
     * 指定されたデータをint値として取得します
     *
     * @param position  コンテナアイテムの位置を示すインデックス
     * @param widgetRes 指定されたアイテムビューに含まれている、取得するデータの場所を示すIdRes
     * @return 取得できた数値
     * @throws IndexOutOfBoundsException 指定されたpositionにアイテムが存在しない場合(positionが負の数である場合も含む)
     * @throws NumberFormatException     取得に失敗して正当な数値を得られなかった場合
     */
    @Override
    public int getInt(int position, @IdRes int widgetRes) {
        return Integer.parseInt(getString(position, widgetRes));
    }

    /**
     * 指定されたデータをint値として取得し、もし指定のViewがコンテナアイテム内に存在しなかった場合、および取得できたデータが数値データではなかった場合には指定されたデフォルト値を返します。
     * なお、デフォルト値にかかわらず範囲外のpositionを指定した場合にはIndexOutOfExceptionが投げられます
     *
     * @param position          コンテナアイテムの位置を示すインデックス
     * @param widgetRes         指定されたアイテムビューに含まれている、取得するデータの場所を示すIdRes
     * @param defaultIfNotFound 指定されたwidgetResを使ってViewを取得できなかった場合、
     *                          およびViewが取得できても表示データが数字のみではなかった場合に戻り値となるデフォルト値
     * @return 取得できた数値、あるいは指定されたデフォルト値
     * @throws IndexOutOfBoundsException 指定されたpositionにアイテムが存在しない場合(positionが負の数である場合も含む)
     */
    @Override
    public int getInt(int position, @IdRes int widgetRes, int defaultIfNotFound) {
        String value = getString(position, widgetRes);
        if (TextUtils.isDigitsOnly(value))
            return Integer.parseInt(value);
        return defaultIfNotFound;
    }

    private void notifyDataSetChanged() {
        mActivity.runOnUiThread(mNotifyRunnable);
    }
}
