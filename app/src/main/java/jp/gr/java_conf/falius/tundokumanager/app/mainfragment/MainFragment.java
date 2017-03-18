package jp.gr.java_conf.falius.tundokumanager.app.mainfragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.ymiyauchi.app.R;

import jp.gr.java_conf.falius.tundokumanager.app.Type;
import jp.gr.java_conf.falius.tundokumanager.app.data.DataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.mainfragment.listctrl.ListContextMenu;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * メイン画面のフラグメント
 */

public class MainFragment extends Fragment {
    private static final String ARG_TYPE_CODE = "type code";

    private Type mType;

    private ListContextMenu mContextMenu;
    private CallbackTransmitter mCallbackTransmitter;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(Type type) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE_CODE, type.getCode());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        if (getArguments() != null) {
            mType = Type.fromCode(getArguments().getInt(ARG_TYPE_CODE));
        }

        Initializer initializer = new Initializer(this, mType, view);
        mContextMenu = initializer.getListContextMenu();
        mCallbackTransmitter = initializer.getCallbackTransmitter();
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        mContextMenu.onCreateContextMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        mContextMenu.onContextItemSelected(item);
        return false;  // trueにするとこれ以上イベントを伝播させない
    }

    /**
     * InputActivityから、登録ボタンおよび削除ボタンでActivityが破棄されたときに呼ばれる
     * 戻るボタンでアクティビティを破棄した場合でも呼ばれ、dataがセットされていないためdataはnullとなる
     * <p>
     * 登録ボタン : 新規データの作成
     * 削除ボタン : データの削除
     * 戻るボタン : 何もしない
     *
     * @param requestCode このフラグメントのポジション
     * @param resultCode  結果の種類を表すコード。この値を使って結果Enumを取得する
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackTransmitter.onInputActivityLeaved(resultCode, data);
    }

    // ダイアログからの値受け取り
    // ダイアログでは直接Fragmentのインスタンスを取得できないので、一度ActivityのonReturnValueメソッドを呼び出し、
    // onReturnValue内でfromActivityを呼び出している
    public void onDialogLeaved(DataConverter newData) {
        mCallbackTransmitter.onDialogLeaved(newData);
    }

    public Type type() {
        return mType;
    }
}
