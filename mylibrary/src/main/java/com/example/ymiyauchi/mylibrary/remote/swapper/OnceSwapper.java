package com.example.ymiyauchi.mylibrary.remote.swapper;


/**
 * Created by ymiyauchi on 2017/02/02.
 */

public abstract class OnceSwapper implements Swapper {

    @Override
    public boolean isContinue() {
        return false;
    }
}
