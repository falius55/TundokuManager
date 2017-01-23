package com.example.ymiyauchi.mylibrary.view.pageradapter;

import android.support.v4.app.Fragment;

/**
 * Created by M.yusuke on 2017/01/08.
 * <p>
 * PagerViewの各ページのFragmentを提供するファクトリのインターフェースです
 */

public interface PagerType {

    Fragment getFragment();

    String getPageTitle();
}
