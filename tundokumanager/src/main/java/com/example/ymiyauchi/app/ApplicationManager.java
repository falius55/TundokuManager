package com.example.ymiyauchi.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by ymiyauchi on 2017/01/11.
 * <p>
 * アプリケーション全体のContextを保持するクラス
 */
public class ApplicationManager extends Application {
    private static ApplicationManager ourInstance;

    public ApplicationManager() {
        super();
        // フィールドで初期化するとgetResources()でnullが返ってきてしまう(アプリが使用するApplicationとは別のインスタンスが作成されて保持されてしまう？)
        ourInstance = this;
    }

    public static ApplicationManager getInstance() {
        return ourInstance;
    }

    public static Context getContext() {
        return getInstance();
    }

}
