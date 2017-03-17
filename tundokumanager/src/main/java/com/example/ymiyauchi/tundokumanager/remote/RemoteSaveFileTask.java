package com.example.ymiyauchi.tundokumanager.remote;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import jp.gr.java_conf.falius.communication.client.Client;
import jp.gr.java_conf.falius.communication.client.NonBlockingClient;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.FileSender;
import jp.gr.java_conf.falius.communication.sender.MultiDataSender;
import jp.gr.java_conf.falius.communication.sender.OnSendListener;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class RemoteSaveFileTask extends AsyncTask<String, String, Receiver> {
    private static final String TAG = "REMOTE_SAVE_FILE_TASK";
    private final Activity mActivity;

    public RemoteSaveFileTask(Activity activity) {
        mActivity = activity;
    }

    /**
     * @param strings ファイル名、サーバー側での保存ディレクトリ、送るファイルの場所。
     *                ファイル名が不適切(/や空白が含まれているなど)だと、サーバー側でファイル保存に失敗する
     * @return
     */
    @Override
    protected Receiver doInBackground(final String... strings) {
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String serverHost = sharedPreferences.getString("ip_address", "localhost");
        int port = Integer.parseInt(sharedPreferences.getString("port", "0"));

        Client client = new NonBlockingClient(serverHost, port);

        client.addOnSendListener(new OnSendListener() {
            @Override
            public void onSend(int writeSize) {
                publishProgress("send(", Integer.toString(writeSize), "byte)");
            }
        });

        client.addOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(String fromAddress, int readByte, Receiver receiver) {
                publishProgress("received data");
            }
        });

        try {
            final String fileName = strings[0];
            final String savePath = strings[1];
            final String filePath = strings[2];
            Log.d(TAG, "fileName:" + fileName);
            Log.d(TAG, "savePath:" + savePath);
            Log.d(TAG, "filePath:" + filePath);
            return client.start(new OnceSwapper() {
                @Override
                public Sender swap(String remoteAddress, Receiver receiver) {
                    Sender sender = new MultiDataSender();
                    sender.put(RequestHandler.FILE_SAVE.getCode());
                    FileSender fileSender = new FileSender(sender);
                    fileSender.put(fileName);
                    fileSender.put(savePath);
                    try {
                        fileSender.put(new File(filePath));
                    } catch (IOException e) {
                        return null;
                    }
                    return sender;
                }
            });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Receiver receiver) {
        super.onPostExecute(receiver);
        if (receiver == null) {
            Toast.makeText(mActivity, "failed send file", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mActivity, "send file:" + receiver.getString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        StringBuilder sb = new StringBuilder();
        for (String msg : values) {
            sb.append(msg);
        }
        Toast.makeText(mActivity, sb.toString(), Toast.LENGTH_SHORT).show();
        mActivity.finish();
    }
}
