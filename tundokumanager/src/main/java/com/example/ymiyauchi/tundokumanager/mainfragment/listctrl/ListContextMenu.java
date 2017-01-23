package com.example.ymiyauchi.tundokumanager.mainfragment.listctrl;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ymiyauchi.tundokumanager.ChartDialogFragment;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.data.ListItemDataConverter;
import com.example.ymiyauchi.tundokumanager.input.InputActivity;
import com.example.ymiyauchi.tundokumanager.MainActivity;
import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.tundokumanager.mainfragment.ItemEntryManager;
import com.example.ymiyauchi.tundokumanager.mainfragment.MainFragment;
import com.example.ymiyauchi.mylibrary.view.manager.ContainerManager;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * リストアイテムを長押しして表示されるコンテキストメニューの処理を請け負う
 */

public class ListContextMenu {
    private final MainFragment mFragment;
    private final Type mType;
    private final ItemEntryManager mItemEntryManager;
    private final ContainerManager mContainerManager;
    private final ListView mListView;

    private final String mModificationTag;
    private final String mDeleteTag;
    private final String mGraphTag;

    public ListContextMenu(MainFragment fragment, ItemEntryManager itemEntryManager,
                           ListView listView, ContainerManager containerManager) {
        mFragment = fragment;
        mType = fragment.type();
        mItemEntryManager = itemEntryManager;
        mContainerManager = containerManager;
        mListView = listView;

        mModificationTag = mType.getName() + "情報修正";
        mDeleteTag = mType.getName() + "情報削除";
        mGraphTag = mType.getName() + mFragment.getString(R.string.context_graph);
    }

    public void onCreateContextMenu(ContextMenu menu) {
        mFragment.getActivity().getMenuInflater().inflate(R.menu.main_context, menu);
        System.out.println("called onCreateContextMenu()");
        menu.setHeaderTitle(mType.getName());

        MenuItem modfItem = menu.getItem(1);
        modfItem.setTitle(mModificationTag);
        MenuItem delItem = menu.getItem(2);

        if (mType.hasProgress()) {
            MenuItem graphItem = menu.getItem(0);
            graphItem.setTitle(mGraphTag);
        } else {
            menu.removeItem(0);
        }

        delItem.setTitle(mDeleteTag);
    }

    public void onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position - mListView.getHeaderViewsCount();
        if (position < 0)
            return;

        DataConverter data = new ListItemDataConverter(mType, mContainerManager, position);

        // コンテキストメニューが選択されると、複数のページで処理がされてしまう
        // そのため、各ページの特定情報を含めたタイトルで判断して処理を行うことにする
        if (item.getTitle().equals(mModificationTag)) {
            startModification(data);
        }

        if (item.getTitle().equals(mDeleteTag)) {
            mItemEntryManager.deleteItem(data);
        }

        if (item.getTitle().equals(mGraphTag)) {
            DialogFragment fragment = ChartDialogFragment.newInstance(data);
            fragment.show(mFragment.getActivity().getSupportFragmentManager(), "graph");
        }
    }

    private void startModification(DataConverter data) {
        // [修正]選択時の処理
        Intent intent = new Intent(mFragment.getActivity(), InputActivity.class);
        mFragment.getActivity().startActivityForResult(data.stuffInto(intent), ((MainActivity) mFragment.getActivity()).getCurrentItem());
    }
}
