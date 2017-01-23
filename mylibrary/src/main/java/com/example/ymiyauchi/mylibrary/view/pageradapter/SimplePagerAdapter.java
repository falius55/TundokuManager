package com.example.ymiyauchi.mylibrary.view.pageradapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by M.yusuke on 2017/01/08.
 * <p>
 * 各ページの情報をPagerTypeを実装したファクトリから受け取る、シンプルなPagerAdapterです
 */

public class SimplePagerAdapter extends FragmentStatePagerAdapter {
    private static final String ARG_PAGER_POSITION = "pager position";

    private final List<Pair<PagerType, Fragment>> mPages;

    public SimplePagerAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
        mPages = new ArrayList<>();
    }

    public Fragment findFragmentByPosition(ViewPager viewPager, int position) {
        return (Fragment) instantiateItem(viewPager, position);
    }

    public void add(PagerType type) {
        mPages.add(new Pair<>(type, type.getFragment()));
    }

    public void addAll(PagerType[] types) {
        for (PagerType type : types) {
            add(type);
        }
    }

    @Override
    public Fragment getItem(int position) {
        Fragment item = mPages.get(position).second;
        Bundle bundle = item.getArguments();
        if (bundle == null)
            bundle = new Bundle();
        bundle.putInt(ARG_PAGER_POSITION, position);
        item.setArguments(bundle);

        return item;
    }

    @Override
    public int getCount() {
        return mPages.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPages.get(position).first.getPageTitle();
    }
}
