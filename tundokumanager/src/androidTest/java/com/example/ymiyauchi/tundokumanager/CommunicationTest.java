package com.example.ymiyauchi.tundokumanager;

import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.AndroidJUnitRunner;

import com.example.ymiyauchi.mylibrary.remote.client.Client;
import com.example.ymiyauchi.mylibrary.remote.client.NonBlockingClient;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.mylibrary.remote.sender.MultiDataSender;
import com.example.ymiyauchi.mylibrary.remote.sender.Sender;
import com.example.ymiyauchi.mylibrary.remote.swapper.RepeatSwapper;
import com.example.ymiyauchi.mylibrary.remote.swapper.Swapper;
import com.example.ymiyauchi.tundokumanager.remote.RequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by ymiyauchi on 2017/03/01.
 */

@RunWith(AndroidJUnit4.class)
public class CommunicationTest {

    @Test
    public void fileTree() {
        String host = "localhost";
        int port = 7100;
        Client client = new NonBlockingClient(host, port);

        Swapper swapper = new RepeatSwapper() {
            @Override
            public Sender swap(String remoteAddress, Receiver receiver) {
                Sender sender = new MultiDataSender();
                sender.put(RequestHandler.DIRECTORY_ASK.getCode());
                sender.put("root");

                return sender;
            }
        };

        try {
            Receiver receiver = client.start(swapper);

            JSONArray dirs = new JSONArray(receiver.getString());
            JSONArray files = new JSONArray(receiver.getString());

            String c = dirs.getString(0);
            String d = dirs.getString(1);
            String f = dirs.getString(2);

            assertThat(c, is("C:"));
            assertThat(d, is("D:"));
            assertThat(f, is("F:"));

            assertThat(files.length(), is(0));
        } catch (IOException | TimeoutException | JSONException e) {
            e.printStackTrace();
        }
    }
}
