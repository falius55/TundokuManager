package com.example.ymiyauchi.tundokumanager.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.ymiyauchi.mylibrary.remote.client.Client;
import com.example.ymiyauchi.mylibrary.remote.client.NonBlockingClient;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.mylibrary.remote.sender.MultiDataSender;
import com.example.ymiyauchi.mylibrary.remote.sender.Sender;
import com.example.ymiyauchi.mylibrary.remote.swapper.OnceSwapper;
import com.example.ymiyauchi.mylibrary.remote.swapper.Swapper;
import com.example.ymiyauchi.tundokumanager.tree.TreeFragment;
import com.example.ymiyauchi.tundokumanager.tree.filetree.DirectoryElement;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ymiyauchi on 2017/02/05.
 */

public class DirectoryLoadTask extends AsyncTask<DirectoryElement, Integer, Receiver> {
    private static final String TAG = "Directory append task";
    private final Context mContext;
    private DirectoryElement mDirectoryElement = null;
    private final TreeFragment mFragment;

    public DirectoryLoadTask(TreeFragment fragment) {
        mContext = fragment.getActivity();
        mFragment = fragment;
    }

    @Override
    protected Receiver doInBackground(final DirectoryElement... elements) {
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(mContext);
        String serverHost = sharedPreferences.getString("ip_address", "localhost");
        int port = Integer.parseInt(sharedPreferences.getString("port", "0"));
        mDirectoryElement = elements[0];


        Log.d(TAG, "element:" + elements[0].getAbsoluteName());
        Client client = new NonBlockingClient(serverHost, port);
        Swapper swapper = new OnceSwapper() {
            @Override
            public Sender swap(String remoteAddress, Receiver receiver) {
                Sender sender = new MultiDataSender();
                sender.put(RequestHandler.DIRECTORY_ASK.getCode());
                String dirName = elements[0].getAbsoluteName();
                Log.d(TAG, "send:" + dirName);
                sender.put(dirName);
                return sender;
            }
        };
        Receiver ret;
        try {
            ret = client.start(swapper);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Receiver receiver) {
        super.onPostExecute(receiver);
        if (receiver == null) {
            Log.d(TAG, "directory ask missing");
            mFragment.createList(null);
            return;
        }
        try {
            String dirsString = receiver.getString();
            String filesString = receiver.getString();

            Log.d(TAG, "dirsString:" + dirsString);
            Log.d(TAG, "filesString:" + filesString);

            JSONArray dirs = new JSONArray(dirsString);
            JSONArray files = new JSONArray(filesString);

            Log.d(TAG, "dirs:" + dirs);
            Log.d(TAG, "files:" + files);

            DirectoryElement element = mDirectoryElement;

            for (int i = 0; i < dirs.length(); i++) {
                JSONArray dirPath = dirs.getJSONArray(i);
                String[] paths = getStringArray(dirPath);

                element.addDirectory(paths);
            }
            for (int i = 0; i < files.length(); i++) {
                JSONArray filePath = files.getJSONArray(i);
                String[] paths = getStringArray(filePath);
                element.addFile(paths);
            }
        } catch (JSONException | FileNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("FILE_TREE_PRINT", "size:" + mDirectoryElement.root().getChildCount());
        Log.d("FILE_TREE_PRINT", "\n" + mDirectoryElement.root().toTreeString("|"));

        mFragment.createList(mDirectoryElement);
    }

    private String[] getStringArray(JSONArray filePath) throws JSONException {
        String[] paths = new String[filePath.length()];
        for (int i = 0; i < filePath.length(); i++) {
            paths[i] = filePath.getString(i);
        }
        return paths;
    }
}
