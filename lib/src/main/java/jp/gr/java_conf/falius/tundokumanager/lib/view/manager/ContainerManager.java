package com.example.ymiyauchi.lib.view.manager;

import android.database.Cursor;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.widget.BaseAdapter;

/**
 * Created by ymiyauchi on 2016/11/27.
 *
 * <p>
 * Adapterを内包し、データの受入を主に配列とCursorオブジェクトに規定するインターフェースです
 * ListViewを支援します。
 *
 * <p/>
 * インスタンスを作成してから、getAdapter()で返されるadapterをListViewのsetAdapterに渡します<br>
 * 渡すデータは各リストのビューがもつidをキーにしたMapであり、データを変更する度にリストビューに通知して変更を自動的に反映させます<br>
 * もしデータを渡さない場合には空のデータが作成され、各種メソッドでデータを操作できます</p>
 *
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
 *     // ContainerManager manager = new CustomContainerManager(this, R.id.list, new int[]{R.id.name, R.id.age, R.id.place, R.id.is_student};
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

public interface ContainerManager {

    /**
     * Adapterを取得します
     *
     * @return Adapter
     */
    BaseAdapter getAdapter();

    /**
     * 指定された位置のコンテナアイテムにつけられたIDを取得します
     *
     * @param position IDを取得するアイテムの位置を示すインデックス
     * @return アイテムに対応づけられたID
     */
    long getItemId(int position);

    /**
     * コンテナに表示箇所を指定してアイテムを新規追加します
     *
     * @param to   追加先のウィジェットのID
     * @param data 先に渡したtoの順番に対応する並びで、そのウィジェットに表示するデータを表すオブジェクト
     * @throws IllegalArgumentException to.length < data.lengthの場合
     */
    void addItem(@IdRes int[] to, Object[] data);

    void addItem(@IdRes int[] to, int[] data);

    /**
     * コンテナに表示箇所を指定してアイテムを新規追加し、このアイテムにIDをつけます(オプションの操作)
     *
     * @param to   追加先のウィジェットのID
     * @param data 先に渡したtoの順番に対応する並びで、そのウィジェットに表示するデータを表すオブジェクト
     * @param ID   追加するアイテムにつけるID
     * @throws IllegalArgumentException to.length < data.lengthの場合
     */
    void addItemWithID(@IdRes int[] to, Object[] data, long ID);

    /**
     * コンテナにアイテムを新規追加します
     *
     * @param data 先に渡したtoの順番に対応する並びで、そのウィジェットに表示するデータを表すオブジェクト
     * @throws IllegalArgumentException to.length < data.lengthの場合
     */
    void addItem(Object[] data);

    void addItem(int[] data);

    /**
     * コンテナにアイテムを新規追加し、このアイテムにIDをつけます(オプションの操作)
     *
     * @param data 先に渡したtoの順番に対応する並びで、そのウィジェットに表示するデータを表すオブジェクト
     * @throws IllegalArgumentException to.length < data.lengthの場合
     */
    void addItemWithID(Object[] data, long ID);

    /**
     * Cursorから取得できる値を、利用する列のインデックスを指定して全行追加します。列の値がnullの場合は空文字を表示します。
     *
     * @param cursor        データベース等から取得したカーソル
     * @param to            データの表示箇所を表すIdRes
     * @param columnIndexes カーソルから得られる値のうち、ビューに表示する列のインデックス。
     *                      引数に与えた順番の値がtoと同じインデックスのリソースIDを持つウィジェットに表示されます
     * @throws IllegalArgumentException 列のインデックスの数が先に渡したリソースIDの総数よりも多い場合、
     *                                  指定されたインデックスの列が存在しない場合
     * @throws RuntimeException         Cursorから取得した値の型がblobその他予期しないものであった場合
     */
    void addAllItem(Cursor cursor, @IdRes int[] to, int... columnIndexes);

    /**
     * Cursorから取得できる値を、利用する列のインデックスを指定して全行追加します。列の値がnullの場合は空文字を表示します。
     *
     * @param cursor        データベース等から取得したカーソル
     * @param columnIndexes カーソルから得られる値のうち、ビューに表示する列のインデックス。
     *                      引数に与えた順番の値が先に渡したtoと同じインデックスのリソースIDを持つウィジェットに表示されます
     * @throws IllegalArgumentException 列のインデックスの数が先に渡したリソースIDの総数よりも多い場合、
     *                                  指定されたインデックスの列が存在しない場合
     * @throws RuntimeException         Cursorから取得した値の型がblobその他予期しないものであった場合
     */
    void addAllItem(Cursor cursor, int... columnIndexes);

    /**
     * Cursorから取得できる値を、利用する列名を指定して全行追加します。列の値がnullの場合は空文字を表示します。
     *
     * @param cursor  データベース等から取得したカーソル
     * @param to      データの表示箇所を表すIdRes
     * @param columns カーソルの列名
     * @throws IllegalArgumentException 存在しない列名が指定された場合、列のインデックス賀の数が先に渡したリソースIDの総数よりも多い場合
     */
    void addAllItem(Cursor cursor, @IdRes int[] to, String... columns);

    /**
     * Cursorから取得できる値を、利用する列名を指定して全行追加します。列の値がnullの場合は空文字を表示します。
     *
     * @param cursor  データベース等から取得したカーソル
     * @param columns カーソルの列名
     * @throws IllegalArgumentException 存在しない列名が指定された場合、列のインデックス賀の数が先に渡したリソースIDの総数よりも多い場合
     */
    void addAllItem(Cursor cursor, String... columns);

    /**
     * 既存のアイテムの内容を変更します
     *
     * @param position アイテムのインデックス(０始まり)
     * @param to       書き換えるウィジェットのID。必ずしもすべてのウィジェットを網羅する必要はなく、ひとつだけでも構いません
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     * @throws IndexOutOfBoundsException positionが範囲外の場合、to.length < data.lengthの場合
     */
    void updateItem(int position, @IdRes int[] to, Object[] data);

    /**
     * 既存のアイテムの内容を指定されたint値のデータに変更します
     *
     * @param position アイテムのインデックス(０始まり)
     * @param to       書き換えるウィジェットのID。必ずしもすべてのウィジェットを網羅する必要はなく、ひとつだけでも構いません
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     * @throws IndexOutOfBoundsException positionが範囲外の場合、to.length < data.lengthの場合
     */
    void updateItem(int position, @IdRes int[] to, int[] data);

    /**
     * 既存のアイテムの内容を変更します
     *
     * @param position アイテムのインデックス(０始まり)
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     * @throws IndexOutOfBoundsException positionが範囲外の場合、to.length < data.lengthの場合
     */
    void updateItem(int position, Object[] data);

    /**
     * あらかじめ指定されたtoの順序に従って、既存のアイテムの内容を変更します。dataの内容をtoのひとつめから詰めていくようにして表示します。
     * dataの数がtoの数より少なかった場合は、dataの内容を使い切った残りのIdResから得られるウィジェットには何もしません。
     *
     * @param position アイテムのインデックス(０始まり)
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     * @throws IndexOutOfBoundsException positionが範囲外の場合、to.length < data.lengthの場合
     */
    void updateItem(int position, int[] data);

    /**
     * 指定された位置にあるコンテナアイテムの指定箇所の内容を変更します
     *
     * @param position アイテムのインデックス(０始まり)
     * @param data     新しいデータ。toのインデックスとdataのインデックスは対応しています
     */
    void updateItem(int position, @IdRes int to, Object data);

    void updateItem(int position, @IdRes int to, @StringRes int resId, Object... formatArgs);

    // TODO: void changeAllItemContent(Cursor cursor, @IdRes int[] to, int... columnIndexes);

    /**
     * コンテナアイテムの総数を取得します
     *
     * @return アイテム総数
     */
    int getCount();

    /**
     * コンテナから指定されたアイテムを消去します
     *
     * @param position 消去するアイテム０始まりのインデックス
     */
    void removeItem(int position);

    /**
     * コンテナの中にあるアイテムを空にします
     */
    void clear();

    /**
     * 指定されたデータを文字列として取得します
     *
     * @param position  コンテナアイテムの位置を示すインデックス
     * @param widgetRes 指定されたアイテムビューに含まれている、取得するデータの場所を示すIdRes
     * @return 取得できた文字列。取得に失敗するとnull
     * @throws IndexOutOfBoundsException 指定されたpositionにアイテムが存在しない場合(positionが負の数である場合も含む)
     */
    String getString(int position, @IdRes int widgetRes);

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
    String getString(int position, @IdRes int widgetRes, String defaultIfNotFound);

    /**
     * 指定されたデータをint値として取得します
     *
     * @param position  コンテナアイテムの位置を示すインデックス
     * @param widgetRes 指定されたアイテムビューに含まれている、取得するデータの場所を示すIdRes
     * @return 取得できた数値
     * @throws IndexOutOfBoundsException 指定されたpositionにアイテムが存在しない場合(positionが負の数である場合も含む)
     * @throws NumberFormatException     取得に失敗して正当な数値を得られなかった場合
     */
    int getInt(int position, @IdRes int widgetRes);

    /**
     * 指定されたデータをint値として取得し、もし指定のViewがコンテナアイテム内に存在しなかった場合、および取得できたデータが数値データではなかった場合には指定されたデフォルト値を返します。
     * なお、デフォルト値にかかわらず範囲外のpositionを指定した場合にはIndexOutOfExceptionが投げられます
     *
     * @param position          コンテナアイテムの位置を示すインデックス
     * @param widgetRes         指定されたアイテムビューに含まれている、取得するデータの場所を示すIdRes
     * @param defaultIfNotFound 指定されたwidgetResを使ってViewを取得できなかった場合、およびViewが取得できても表示データが数字のみではなかった場合に戻り値となるデフォルト値
     * @return 取得できた数値、あるいは指定されたデフォルト値
     * @throws IndexOutOfBoundsException 指定されたpositionにアイテムが存在しない場合(positionが負の数である場合も含む)
     */
    int getInt(int position, @IdRes int widgetRes, int defaultIfNotFound);
}
