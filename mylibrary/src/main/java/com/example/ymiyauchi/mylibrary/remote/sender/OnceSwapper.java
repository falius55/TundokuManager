package com.example.ymiyauchi.mylibrary.remote.sender;


import com.example.ymiyauchi.mylibrary.remote.Swapper;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public abstract class OnceSwapper implements Swapper {

    @Override
    public boolean isContinue() {
        return false;
    }

}
