package com.example.ymiyauchi.mylibrary.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TabHost;

/**
 * Created by M.yusuke on 2017/01/08.
 * <p>
 * ViewPagerと連動して動くTabHostです
 * <p>
 * bindViewPager,setAdapter, buildメソッドを呼び出すことで設定できます
 */

public class PagerTabHost extends TabHost {
    private PagerAdapter mAdapter = null;
    private ViewPager mViewPager = null;

    public PagerTabHost(Context context) {
        super(context);
    }

    public PagerTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    public void bindViewPager(@IdRes int viewPagerId) {
        Activity activity = (Activity) getContext();
        ViewPager viewPager = (ViewPager) activity.findViewById(viewPagerId);
        bindViewPager(viewPager);
    }

    private ViewPager getViewPager() {
        if (mViewPager != null) {
            return mViewPager;
        }

        ViewGroup parent = (ViewGroup) getParent();
        ViewPager viewPager = new ViewPager(getContext());

        // TODO: ViewPagerの基礎設定
        parent.addView(viewPager);
        return viewPager;
    }

    public void setAdapter(PagerAdapter adapter) {
        mAdapter = adapter;
    }

    public void build(@IdRes int tabContentId) {
        setup();
        PagerAdapter adapter = mAdapter;
        ViewPager viewPager = getViewPager();
        viewPager.setAdapter(adapter);

        for (int i = 0, cnt = adapter.getCount(); i < cnt; i++) {
            addTab(newTabSpec(String.valueOf(i))
                    .setIndicator(adapter.getPageTitle(i))
                    .setContent(tabContentId));
        }

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentTab(position);
            }
        });

        setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                ViewPager viewPager = getViewPager();
                viewPager.setCurrentItem(Integer.parseInt(tabId), true);
            }
        });
        viewPager.setCurrentItem(1);
        setCurrentTab(1);
    }
}
