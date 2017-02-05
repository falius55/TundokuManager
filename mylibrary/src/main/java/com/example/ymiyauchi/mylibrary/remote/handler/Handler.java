package com.example.ymiyauchi.mylibrary.remote.handler;

import java.nio.channels.SelectionKey;

/**
 * Created by ymiyauchi on 2017/02/02.
 *
 * サーバー及びクライアントの送受信などの具体的な処理を行うクラスのインターフェースです。
 * 一度処理が開始されると自身で次のハンドラを生成し、接続が切断されるまで連鎖していきます。
 */

public interface Handler {

    void handle(SelectionKey key);
}
