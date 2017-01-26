package com.example.ymiyauchi.tundokumanager.mainfragment.listctrl;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ymiyauchi.tundokumanager.database.BasicDatabase;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.data.ListItemDataConverter;
import com.example.ymiyauchi.tundokumanager.data.MutableDataConverter;
import com.example.ymiyauchi.tundokumanager.database.ItemColumns;
import com.example.ymiyauchi.tundokumanager.mainfragment.ItemEntryManager;
import com.example.ymiyauchi.mylibrary.AndroidDatabase;
import com.example.ymiyauchi.mylibrary.view.manager.ContainerManager;

import java.util.Locale;

/**
 * Created by ymiyauchi on 2017/01/19.
 * <p>
 * リストの初期化と構築
 */

public class ListBuilder {
    private final Fragment mFragment;
    private final Type mType;
    private final ContainerManager mContainerManager;

    public ListBuilder(Fragment fragment, Type type, ContainerManager containerManager) {
        mFragment = fragment;
        mType = type;
        mContainerManager = containerManager;
    }

    public void setupList(SortFilter sortFilter, ItemEntryManager itemEntryManager, ListView listView) {

        listView.setAdapter(mContainerManager.getAdapter());
        mFragment.registerForContextMenu(listView);
        listView.setOnItemClickListener(new ListViewClickListener(listView, itemEntryManager));

        build(sortFilter.getFilter(), sortFilter.getSort());
    }

    public void build(Filter filter, Sort sort) {

        ContainerManager containerManager = mContainerManager;
        containerManager.clear();

        Type type = mType;
        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity().getApplicationContext())) {
            String table = type.table();
//            String sql = mFragment.getString(R.string.sql_build, table, filter.where(type), sort.orderBy());
            String dateColumn = String.format(Locale.JAPAN, "strftime('%%Y/%%m/%%d', %s) as date", ItemColumns.DATE.getName());
            String daysColumn = String.format("\'購入から\' || cast(julianday(\'now\') - julianday(%s) as int) || \'日経過\' as days", ItemColumns.DATE.getName());
            Cursor cursor = db.selectWithOrder(table,
                    new String[]{
                            ItemColumns.ID.getName(), dateColumn, ItemColumns.NAME.getName(), ItemColumns.PLAYED.getName(),
                            ItemColumns.CURRENT.getName(), ItemColumns.CAPACITY.getName(), daysColumn,
                            ItemColumns.PRICE.getName(), ItemColumns.MEMO.getName()}, sort.orderBy(), filter.where(type));
            // select _id, strftime(\'%%Y/%%m/%%d\', date) as date, name, played, current, capacity, \'購入から\' || cast(julianday(\'now\') - julianday(date) as int) || \'日経過\' as days, price, memo from %1$s %2$s %3$s
            containerManager.addAllItem(cursor, 1, 2, 3, 4, 5, 6, 7, 8);
        }
    }


    private class ListViewClickListener implements AdapterView.OnItemClickListener {
        private final ListView mListView;
        private final ItemEntryManager mItemEntryManager;

        private ListViewClickListener(ListView listView, ItemEntryManager itemEntryManager) {
            mListView = listView;
            mItemEntryManager = itemEntryManager;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int headerCount = mListView.getHeaderViewsCount();
            position -= headerCount;
            if (position < 0) return;

            Type type = mType;
            if (type.hasProgress()) {
                ClickDialogFragment newFragment = ClickDialogFragment.newInstance(type, position, mContainerManager);
                newFragment.show(mFragment.getActivity().getSupportFragmentManager(), "Test");
            } else {
                DataConverter curData = new ListItemDataConverter(type, mContainerManager, position);
                DataConverter newData = new MutableDataConverter(curData).putPlayed(!curData.isPlayed());
                mItemEntryManager.updateItem(newData);
            }
        }
    }
}
