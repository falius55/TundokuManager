package com.example.ymiyauchi.mylibrary.remote.sender;

import android.graphics.Path;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by ymiyauchi on 2017/02/03.
 */

public class FileSender extends ExtendableSender {

    public FileSender(Sender sender) {
        super(sender);
    }

    public Sender put(File file) throws FileNotFoundException, IOException {
        return put(new FileInputStream(file));
    }

    public Sender putFile(String fileName) throws IOException {
        return put(new File(fileName));
    }
}
