package com.example.ymiyauchi.mylibrary.remote.receiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ymiyauchi on 2017/02/03.
 *
 * ファイルの受信が可能となるよう、Receiverオブジェクトを拡張します。
 */

public class FileReceiver extends ExtendableReceiver {

    public FileReceiver(Receiver receiver) {
        super(receiver);
    }

    public void getAndSave(File file) throws IOException {
        getAndOutput(new FileOutputStream(file));
    }

    public void getAndSave(File file, boolean append) throws IOException {
        getAndOutput(new FileOutputStream(file, append));
    }

}
