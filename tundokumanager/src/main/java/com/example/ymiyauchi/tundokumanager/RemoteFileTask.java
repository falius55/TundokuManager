package com.example.ymiyauchi.tundokumanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.ymiyauchi.mylibrary.remote.Client;
import com.example.ymiyauchi.mylibrary.remote.NonBlockingClient;
import com.example.ymiyauchi.mylibrary.remote.receiver.OnReceiveListener;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.mylibrary.remote.sender.MultiDataSender;
import com.example.ymiyauchi.mylibrary.remote.sender.OnSendListener;
import com.example.ymiyauchi.mylibrary.remote.sender.OnceSwapper;
import com.example.ymiyauchi.mylibrary.remote.sender.Sender;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class RemoteFileTask extends AsyncTask<String, String, Receiver> {
    private final Context mContext;

    public RemoteFileTask(Context context) {
        mContext = context;
    }

    /**
     * @param strings ファイル名、ファイルの場所。ファイル名が不適切(/や空白が含まれているなど)だと、
     *                サーバー側でファイル保存に失敗する
     * @return
     */
    @Override
    protected Receiver doInBackground(final String... strings) {
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(mContext);
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
            return client.start(new OnceSwapper() {
                @Override
                public Sender swap(String remoteAddress, Receiver receiver) {
                    Sender sender = new MultiDataSender();
                    sender.put(strings[0]);
                    try {
                        sender.put(new File(strings[1]));
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
            Toast.makeText(mContext, "failed send file", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, "send file:" + receiver.getString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        StringBuilder sb = new StringBuilder();
        for (String msg : values) {
            sb.append(msg);
        }
        Toast.makeText(mContext, sb.toString(), Toast.LENGTH_SHORT).show();
    }
}
