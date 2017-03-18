package com.example.ymiyauchi.app.mainfragment;

import android.content.Intent;
import android.util.Log;

import com.example.ymiyauchi.app.data.DataConverter;
import com.example.ymiyauchi.app.data.ReceiveDataConverter;
import com.example.ymiyauchi.app.input.Result;

/**
 * Created by ymiyauchi on 2017/01/20.
 * <p>
 * ダイアログ及びInputActivityからの情報をMainActivityから他のオブジェクトに伝える
 */

class CallbackTransmitter {
    private final ItemEntryManager mItemEntryManager;

    CallbackTransmitter(ItemEntryManager itemEntryManager) {
        mItemEntryManager = itemEntryManager;
    }

    // ダイアログからの値受け取り
    // ダイアログでは直接Fragmentのインスタンスを取得できないので、一度ActivityのonReturnValueメソッドを呼び出し、
    // onReturnValue内でfromActivityを呼び出している
    void onDialogLeaved(DataConverter newData) {
        mItemEntryManager.updateItem(newData);
    }

    /**
     * InputActivityから、登録ボタンおよび削除ボタンでActivityが破棄されたときに呼ばれる
     * 戻るボタンでアクティビティを破棄した場合でも呼ばれ、dataがセットされていないためdataはnullとなる
     * <p>
     * 登録ボタン : 新規データ句の作成
     * 削除ボタン : データの削除
     * 戻るボタン : 何もしない
     *
     * @param resultCode 結果の種類を表すコード。この値を使って結果Enumを取得する
     * @param data       InputActivity内で入力されていたデータ。戻るボタン等で破棄されていた場合にはnull
     */
    void onInputActivityLeaved(int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        Log.d("RESULT", "onActivityResult: result" + data.toString());

        DataConverter newData = new ReceiveDataConverter(data);
        Result result = Result.fromCode(resultCode);
        switch (result) {
            case NON:
                return;
            case DELETE:
                mItemEntryManager.deleteItem(newData);
                break;
            case REGISTER:
                mItemEntryManager.registerItem(newData);
                break;
            case UPDATE:
                mItemEntryManager.updateItem(newData);
                break;
        }

    }
}
