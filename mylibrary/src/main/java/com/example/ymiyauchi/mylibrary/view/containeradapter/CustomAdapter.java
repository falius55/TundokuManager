package com.example.ymiyauchi.mylibrary.view.containeradapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.ymiyauchi.mylibrary.IntRange;

import java.util.List;

import static com.example.ymiyauchi.mylibrary.view.containeradapter.ViewType.IMAGE;
import static com.example.ymiyauchi.mylibrary.view.containeradapter.ViewType.TEXT;


/**
 * Created by ymiyauchi on 2016/11/26.
 * <p>
 * <p>
 * 汎用的なViewに対応できるアダプターです
 * TextViewおよびImageViewに対応しています
 * <p>
 * <p>
 * SparseArrayを用いてデータを登録します。キーは各アイテムのレイアウトリソースに配置されているウィジェットのIdResです。
 * 基本はすべてのウィジェットをテキストビューとして扱い、SparseArrayの値をtoStringメソッドにて文字列化して表示しますが、
 * specifyViewSetterメソッドで個別のビューの表示状態を指定することもできます。
 * また、その際のデータの形式はそれぞれ異なります。
 * なお、表示対象のViewがTextViewおよびそのサブタイプでなかった場合、必ずspecifyViewSetterメソッドなどでViewの表示方法を指定する必要があります。
 * もし指定されなければ例外がスローされます。
 */

public class CustomAdapter extends BaseAdapter {
    public static final int ID_KEY = -1021;
    private static final ViewSetter DEFAULT_SETTER = TEXT;

    private final Context mContext;
    private final LayoutInflater mInflater;
    @LayoutRes
    private final int mItemRes;
    @IdRes
    private final int[] mTo;
    private final List<SparseArray<Object>> mData;  // item list stuffed SparseArray, from IdRes to data object
    private final SparseArray<ViewSetter> mViewSetters;  // from @IdRes or @LayoutLes to setter


    /**
     * ImageViewへのデータはDrawableIDのIntegerオブジェクトかBitmapオブジェクトで渡します
     * 行データを表すSparseArrayのキーにID_KEYがあった場合、その値をIDとして扱います。
     * ID_KEYのキーの値にIntegerオブジェクトでないオブジェクトを保持させないように注意してください
     *
     * @param context コンテキスト
     * @param itemRes リストアイテムのレイアウトID
     * @param to      リストアイテムのレイアウトに含まれるIDのうち、操作するIDの配列
     * @param data    Listの要素一つが表示するリストアイテムのデータを表す。
     *                リストアイテムの行データはSparseArrayで表され、キーが表示先のIdResを表し、
     *                値に表示するデータを格納する。
     *                テキストビューに表示するデータはtoStringメソッドで文字列化されて表示される。
     *                ImageViewに表示するデータは表示するDrawableIDかBitmapオブジェクトを格納する。
     *                キーにKEY_NUMを指定して値にIntegerオブジェクトを格納すると、その値が行のIDとして扱われる。
     */
    public CustomAdapter(Context context,
                         @LayoutRes int itemRes, @IdRes int[] to, List<SparseArray<Object>> data) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItemRes = itemRes;
        // toの値はSparseArrayのキーで得られる値と同一であるためコンストラクタ引数からtoをなくそうとも考えたが、
        // SparseArrayのキーが必ずしも操作対象ビューすべてに対して存在する保証もなく(データの種類の増減があり得る)、
        // そうなるとViewHolderの作成に支障が出る(配列のサイズをいくつにすればいいのかがわからない)ために断念した
        mTo = to;
        mData = data;
        mViewSetters = new SparseArray<>();
    }

    /**
     * <p>
     * アイテムビュー内の特定の表示箇所の操作を指定します。
     * アイテム内ビューのIdResを指定することで、すべてのアイテムに対してsetterが適用されます。
     * setterではアイテムの位置情報、指定されたアイテム内Viewのオブジェクトなどが渡されるので、
     * 各種情報を用いて渡されたViewオブジェクトを操作してください。
     * 指定されていないビューはすべてTextViewであるとして扱われ、そのデータはtoStringメソッドにて文字列化して表示されます。
     * 基本的な操作に対しては、Enum型ViewTypeの列挙定数をsetterに指定することができます。
     * ViewTypeにない操作を行いたい、あるいはアイテムごとに処理を変えたいという場合にViewSetterを独自に作成してください。
     * 登録できるViewSetterは各リソースに対して一つだけです
     * <p>
     * <p>
     * 渡したViewSetter内で何もしなければ、該当のViewはデフォルトの状態(レイアウトファイルの状態)になります。
     * 単純に文字列を表示させた上で操作したい場合でも、ViewType.TEXT.setView()メソッドを先に適用するなどして対処してください。
     * 動的に各アイテムの状態によって変化をつける場合、一度変化させたら自動では元に戻りませんので
     * デフォルト状態に戻す処理もViewSetter内に記述してください。(各アイテムのViewはコンテナにて使い回されるため)
     * <p>
     * <p>
     * なお、resに先に渡されたアイテムのレイアウトリソースを指定すると、アイテムレイアウト全体を表すViewに対してsetterが登録されます。
     * この場合setterの第三引数に渡されるオブジェクトはアイテム全体を表すViewとなり、
     * リストのポジションごとにアイテム背景色を変えるなどといった操作も可能になります。
     *
     * @param res    指定する表示箇所のViewID、あるいは先に渡したアイテムのレイアウトリソース
     * @param setter Viewに行う処理を定義したオブジェクト
     * @return このインスタンス
     * @see ViewType
     */
    // クライアント側でメソッドの引数でViewの種類を指定するときにいちいちCustomAdapter.ViewType.IMAGE_VIEWなどと
    // 記述するのは面倒なので(配列で指定するにもnew CustomAdapter.ViewType[]となってしまう)、
    // int値で指定できるようにする  --> static importで対応する形にする
    public CustomAdapter specifyViewSetter(int res, ViewSetter setter) {
        mViewSetters.put(res, setter);
        return this;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    /**
     * 指定されたインデックスのSparseArray&lt;Object&gt;
     *
     * @param i インデックス
     * @return 指定されたインデックスのデータを表すオブジェクト。
     * SparseArray&lt:Object&gt;が返されるので、必要に応じてキャストしてください
     * @throws IndexOutOfBoundsException 指定されたインデックスがアイテム総数を超えていた場合
     */
    @Override
    public SparseArray<Object> getItem(int i) {
        return mData.get(i);
    }

    /**
     * 行データにキーがID_KEY、値がLongオブジェクトのマッピングがあればそれをIDとしてLongオブジェクトをアンボクシングした値を返します。
     * キーにID_KEYがなければ引数に渡されたインデックスをそのまま返します。
     *
     * @param i 取得するIDのインデックス
     * @return ID_KEYをキーとした値があればその値、なければ指定されたインデックス
     * @throws IllegalStateException     行データのキーがID_KEYである値にLongオブジェクト以外のオブジェクトが保持されている場合
     * @throws IndexOutOfBoundsException 指定されたインデックスがアイテム総数を超えていた場合
     */
    @Override
    public long getItemId(int i) {
        try {
            return (Long) mData.get(i).get(ID_KEY, i);
        } catch (ClassCastException e) {
            throw new IllegalStateException("ID value is not Long object, from ID_KEY(" + ID_KEY + ") to " +
                    mData.get(i).get(ID_KEY) + "(" + mData.get(i).get(ID_KEY).getClass().getName() + ")", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * あらかじめ指定されたIdResからViewが取得できなかった場合は、そのIdResを無視して続行します
     *
     * @param pos         アイテムの位置を表すインデックス
     * @param convertView 各アイテムを表すViewオブジェクト
     * @param viewGroup
     * @return 各リストアイテムに表示するView
     * @throws IllegalStateException あらかじめ指定されたViewのタイプとIdResから得られた実際のViewのタイプが異なっていた場合、
     *                               あらかじめ指定されたViewのタイプと与えられたデータのタイプに不整合があった場合
     */
    @Override
    public View getView(int pos, View convertView, ViewGroup viewGroup) {
        SparseArray<Object> itemData = mData.get(pos);
        int[] to = mTo;
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(mItemRes, viewGroup, false);
            holder = new ViewHolder(to.length);
            for (int i : new IntRange(to.length).toArray()) {
                int res = to[i];
                holder.views[i] = convertView.findViewById(res);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Context context = mContext;
        SparseArray<ViewSetter> setters = mViewSetters;
        ViewSetter setLayoutSetter = setters.get(mItemRes);
        if (setLayoutSetter != null) {
            setLayoutSetter.setView(context, pos, convertView, itemData);
        }

        try {
            for (int i : new IntRange(to.length).toArray()) {
                int res = to[i];
                View view = holder.views[i];
                Object data = itemData.get(res);
                // ここで'if (data == null) continue;'とはできない
                // ViewTypeにHIDEを指定している場合、itemDataにそのViewを対象としたマッピングがあるとは限らないため
                // もしここでcontinueするとsetViewでそのViewを隠すことができず、デフォルトの状態で表示されたままになってしまう
                if (view == null)  // 存在しないViewのIDがtoに指定されていれば、nullが入っている
                    continue;

                ViewSetter setter = setters.get(res, DEFAULT_SETTER);
                // 次の場合は、dataにnullが入った状態でsetViewに渡される
                //  存在するViewのIDをtoに指定しているものの、itemDataにそのViewのリソースIDをキーとしたマッピングがない
                //  存在するViewのIDをtoに指定しており、itemDataにそのViewのリソースIDをキーとしたマッピングはあるものの、その値にnullが入っている
                setter.setView(context, pos, view, data);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e.getMessage());
        }
        return convertView;
    }

    private static class ViewHolder {
        private View[] views;

        private ViewHolder(int len) {
            views = new View[len];
        }
    }

    public interface ViewSetter {
        /**
         * 各アイテムの各ビューに対して行う処理を定義します。
         *
         * @param position アイテムの位置を示すインデックス。
         * @param view     処理対象のビュー
         * @param data     viewに対応付けられたデータ。アイテムの@LayoutResに登録したsetterの場合は、アイテムのデータすべてを含むSparseArray&lt;Object&gt;
         */
        void setView(Context context, int position, @NonNull View view, @Nullable Object data);
    }
}