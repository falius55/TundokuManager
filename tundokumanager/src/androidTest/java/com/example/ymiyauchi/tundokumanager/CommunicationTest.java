package com.example.ymiyauchi.tundokumanager;

import android.support.test.runner.AndroidJUnit4;

import com.example.ymiyauchi.tundokumanager.remote.RequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import jp.gr.java_conf.falius.communication.client.Client;
import jp.gr.java_conf.falius.communication.client.NonBlockingClient;
import jp.gr.java_conf.falius.communication.receiver.Receiver;
import jp.gr.java_conf.falius.communication.sender.MultiDataSender;
import jp.gr.java_conf.falius.communication.sender.Sender;
import jp.gr.java_conf.falius.communication.swapper.RepeatSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

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
