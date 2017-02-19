package com.example.ymiyauchi.tundokumanager.log;

import android.content.Context;

import com.example.ymiyauchi.mylibrary.DateTime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by ymiyauchi on 2017/02/19.
 */

public class LogFile implements Thread.UncaughtExceptionHandler {
    private static final String FILE_NAME = "Tundoku.log";

    private final Context mContext;
    private final boolean mIsAppendMode;
    private final Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    public LogFile(Context context, boolean isAppendMode) {
        mContext = context;
        mIsAppendMode = isAppendMode;
        mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public LogFile(Context context) {
        this(context, false);
    }

    public void writeException(Exception e) {
        try (FileOutputStream fos = mContext.openFileOutput(FILE_NAME, mIsAppendMode ? Context.MODE_APPEND : Context.MODE_PRIVATE);
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
            DateTime dateTime = DateTime.now();
            String formattedDateTime = dateTime.formatTo(DateTime.DEFAULT_DATETIME_FORMAT);

            bw.write("date: ");
            bw.write(formattedDateTime);
            bw.write(System.lineSeparator());
            bw.write(System.lineSeparator());

            bw.write(e.getMessage());
            bw.write(System.lineSeparator());
            bw.write(System.lineSeparator());

            StackTraceElement[] elements = e.getStackTrace();
            int cnt = 0;
            for (StackTraceElement element : elements) {
                bw.write(++cnt + " : ");
                bw.write(element.toString());
                bw.write(System.lineSeparator());
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String readFile() {
        try (FileInputStream fis = mContext.openFileInput(FILE_NAME);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line)
                        .append(System.lineSeparator());
            }
            return sb.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof Exception) {
            writeException((Exception) throwable);
        }

        mDefaultUncaughtExceptionHandler.uncaughtException(thread, throwable);
    }
}
