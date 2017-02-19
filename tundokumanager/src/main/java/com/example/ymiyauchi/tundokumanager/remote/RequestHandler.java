package com.example.ymiyauchi.tundokumanager.remote;

import android.util.SparseArray;

import com.example.ymiyauchi.mylibrary.remote.receiver.FileReceiver;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.mylibrary.remote.sender.MultiDataSender;
import com.example.ymiyauchi.mylibrary.remote.sender.Sender;

import java.io.File;
import java.io.IOException;

/**
 * Created by ymiyauchi on 2017/02/05.
 */

public enum RequestHandler {
    FILE_SAVE(0) {
        @Override
        public Sender handle(Receiver receiver) {
            // 受け取り
            // ファイル名(文字列)
            // 保存パス
            // ファイルデータ
            // 送信
            //  保存成功： "save file"
            //　保存失敗： "failed save file"
            Sender sender = new MultiDataSender();

            String fileName = receiver.getString();
            String savePath = receiver.getString();
            System.out.println("filename:" + fileName);
            try {
                FileReceiver fileReceiver = new FileReceiver(receiver);
                fileReceiver.getAndSave(new File(savePath + "\\" + fileName));
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IOException in swap()");
                sender.put("failed save file");
                return sender;
            }

            sender.put("save file");
            System.out.println("send");
            return sender;

        }
    },

    DIRECTORY_ASK(1) {
        @Override
        public Sender handle(Receiver receiver) {
            // 受け取り
            // 検索ディレクトリの絶対名
            // 送信
            // ディレクトリ一覧の配列のＪｓｏｎ文字列
            // ファイル一覧の配列のＪｓｏｎ文字列
            String directory = receiver.getString();
            StringBuilder sbDirs = new StringBuilder("[");
            StringBuilder sbFiles = new StringBuilder("[");
            File dir = new File(directory);

            File[] files = dir.listFiles();
            for (int i = 0, len = files.length; i < len; i++) {
                File file = files[i];
                if (!file.canRead() || !file.canWrite()
                        || file.getAbsolutePath().equals("C:\\Documents and Settings")
                        || file.getAbsolutePath().equals("C:\\$Recycle.Bin")
                        || file.getAbsolutePath().equals("C:\\System Volume Information")
                        || file.getAbsolutePath().equals("C:\\Recovery")) {
                    continue;
                }

                if (file.isFile()) {
                    continue;
                }

                if (i != 0) {
                    sbDirs.append(",");
                }

                String[] paths = file.getAbsolutePath().split("\\\\");
                StringBuilder sbDir = new StringBuilder("[");
                for (int j = 0; j < paths.length; j++) {
                    if (j != 0) {
                        sbDir.append(",");
                    }
                    String path = paths[j];
                    sbDir.append("\"").append(path).append("\"");
                }
                sbDir.append("]");


                sbDirs.append(sbDir.toString());
            }
            sbDirs.append("]");

            for (int i = 0, len = files.length; i < len; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    continue;
                }
                if (i != 0) {
                    sbFiles.append(",");
                }

                String[] paths = file.getAbsolutePath().split("\\\\");
                StringBuilder sbFile = new StringBuilder("[");
                for (int j = 0; j < paths.length; j++) {
                    if (j != 0) {
                        sbFile.append(",");
                    }
                    String path = paths[j];
                    sbFile.append("\"").append(path).append("\"");
                }
                sbFile.append("]");

                sbFiles.append(sbFile.toString());
            }
            sbFiles.append("]");

            Sender sender = new MultiDataSender();
            sender.put(sbDirs.toString());
            sender.put(sbFiles.toString());
            return sender;
        }
    };

    private static final SparseArray<RequestHandler> mFromCodeToEnum;

    static {
        mFromCodeToEnum = new SparseArray<>();
        for (RequestHandler handler : values()) {
            mFromCodeToEnum.put(handler.getCode(), handler);
        }
    }

    public static RequestHandler fromCode(int code) {
        return mFromCodeToEnum.get(code);
    }

    private final int mCode;

    RequestHandler(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    public abstract Sender handle(Receiver receiver);

}
