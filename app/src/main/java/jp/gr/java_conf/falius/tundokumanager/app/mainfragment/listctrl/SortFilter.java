package jp.gr.java_conf.falius.tundokumanager.app.mainfragment.listctrl;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.example.ymiyauchi.app.R;

import jp.gr.java_conf.falius.tundokumanager.app.Type;
import jp.gr.java_conf.falius.tundokumanager.lib.view.manager.TextViewManager;

/**
 * Created by ymiyauchi on 2017/01/20.
 * <p>
 * リストコントロール
 * ソートボタンとフィルターボタン及び再構築
 */

public class SortFilter {
    private static final String ARG_FILTER = "filter";
    private static final String ARG_SORT = "sort";

    private final Fragment mFragment;
    private final ListBuilder mBuilder;
    private final TextViewManager mTextViewManager;
    private final Type mType;

    public SortFilter(Fragment fragment, Type type, View layout, ListBuilder builder) {
        mFragment = fragment;
        mType = type;
        mBuilder = builder;
        mTextViewManager = init(layout);
    }

    private TextViewManager init(View layout) {
        TextViewManager textViewManager = new TextViewManager(layout,
                new int[]{R.id.txt_filter, R.id.txt_sort, R.id.btn_filter, R.id.btn_sort});
        textViewManager.setOnClickListener(R.id.btn_filter, new FilterButtonClickListener(textViewManager));
        textViewManager.setOnClickListener(R.id.btn_sort, new SortButtonClickListener(textViewManager));

        return textViewManager;
    }

    public boolean isDefaultFilter() {
        return getFilter() == Filter.DEFAULT;
    }

    public Filter getFilter() {
        Bundle bundle = mFragment.getArguments();
        String filterKey = bundle.getString(ARG_FILTER, Filter.DEFAULT.toString());
        return Filter.fromString(filterKey);
    }

    public Sort getSort() {
        Bundle bundle = mFragment.getArguments();
        String sortKey = bundle.getString(ARG_SORT, Sort.DEFAULT.toString());
        return Sort.fromString(sortKey);
    }

    private void changeFilter(Filter filter) {
        Bundle bundle = mFragment.getArguments();
        bundle.putString(ARG_FILTER, filter.toString());
        Sort sort = getSort();
        mBuilder.build(filter, sort);
    }

    private void changeSort(Sort sort) {
        Bundle bundle = mFragment.getArguments();
        bundle.putString(ARG_SORT, sort.toString());
        Filter filter = getFilter();
        mBuilder.build(filter, sort);
    }

    private class FilterButtonClickListener implements View.OnClickListener {
        private int state = 0;

        private FilterButtonClickListener(TextViewManager textViewManager) {
            textViewManager.setText(R.id.txt_filter, Filter.DEFAULT.label(mType));
        }

        @Override
        public void onClick(View view) {
            state = (++state) % Filter.values().length;
            Filter newFilter = Filter.values()[state];
            mTextViewManager.setText(R.id.txt_filter, newFilter.label(mType));
            changeFilter(newFilter);
        }
    }

    private class SortButtonClickListener implements View.OnClickListener {
        private int state = 0;

        private SortButtonClickListener(TextViewManager textViewManager) {
            textViewManager.setText(R.id.txt_sort, Sort.DEFAULT.label());
        }

        @Override
        public void onClick(View view) {
            state = (++state) % Sort.values().length;
            Sort newSort = Sort.values()[state];
            mTextViewManager.setText(R.id.txt_sort, newSort.label());
            changeSort(newSort);
        }
    }
}
