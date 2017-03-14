package com.example.ymiyauchi.tundokumanager.remote;

import android.util.SparseArray;

import communication.receiver.FileReceiver;
import communication.receiver.Receiver;
import communication.sender.MultiDataSender;
import communication.sender.Sender;

import org.json.JSONArray;

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
            try {
                FileReceiver fileReceiver = new FileReceiver(receiver);
                fileReceiver.getAndSave(new File(savePath + "\\" + fileName));
            } catch (IOException e) {
                e.printStackTrace();
                sender.put("failed save file");
                return sender;
            }

            sender.put("save file");
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
            File dir = new File(directory);

            JSONArray dirsArray = new JSONArray();
            JSONArray filesArray = new JSONArray();

            for (File file : dir.listFiles()) {
                if (!file.canRead() || !file.canWrite()
                        || file.getAbsolutePath().equals("C:\\Documents and Settings")
                        || file.getAbsolutePath().equals("C:\\$Recycle.Bin")
                        || file.getAbsolutePath().equals("C:\\System Volume Information")
                        || file.getAbsolutePath().equals("C:\\Recovery")) {
                    continue;
                }

                String[] paths = file.getAbsolutePath().split("\\\\");
                JSONArray pathArray = new JSONArray();
                for (String path : paths) {
                    pathArray.put(path);
                }

                if (file.isDirectory()) {
                    dirsArray.put(pathArray);
                } else {
                    filesArray.put(pathArray);
                }
            }

            Sender sender = new MultiDataSender();
            sender.put(dirsArray.toString());
            sender.put(filesArray.toString());
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
