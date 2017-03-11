package com.example.ymiyauchi.mylibrary.remote.swapper;


/**
 * Created by ymiyauchi on 2017/02/02.
 *
 * 一度の送受信で通信を終える際に利用するSwapperです。
 *
 * クライアントに限り、swapメソッドのreceiverにはnullが入っていますので注意してください。
 *
 */
public abstract class OnceSwapper implements Swapper {

    @Override
    public boolean isContinue() {
        return false;
    }
}
