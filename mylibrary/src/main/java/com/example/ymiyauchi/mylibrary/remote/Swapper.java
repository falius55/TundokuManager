package com.example.ymiyauchi.mylibrary.remote;

import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.mylibrary.remote.sender.Sender;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public interface Swapper {

    /**
     * データの受信と送信を繋ぐ処理を行うクラスのインターフェースです。
     * 受信データが格納されたReceiverからデータを取得し、
     * 送信するデータを格納したSenderオブジェクトを作成して戻り値としてください
     * <p>
     * swapメソッドは送信の直前に実行されます。そのため、受信直後に実行され
     * るOnReceiveListener#onReceiveメソッドにて消費された受信データは
     * このメソッドに渡されるReceiverオブジェクトには入っていません。
     * <p>
     * nullを返すと通信を強制的に終了します。
     * この場合、クライアントかサーバーかに関係なく送信直前に接続を切断します。
     * ただし通信相手は受信に失敗したことを検知して接続を切ることになるため、正常に終了したとは言えないかもしれません。
     * <p>
     * クライアントに限り、最初の一度だけreceiverにnullが渡されますので注意してください。
     */
    Sender swap(String remoteAddress, Receiver receiver);

    /**
     * 通信を続けるかどうかを返すメソッドです。
     * このメソッドはクライアントでは受信直後、サーバーでは送信直後に呼ばれ、falseとなった時点で接続を切断します。
     *
     * @return 通信を続けるかどうか
     */
    boolean isContinue();

    interface SwapperFactory {

        Swapper get();
    }

}
