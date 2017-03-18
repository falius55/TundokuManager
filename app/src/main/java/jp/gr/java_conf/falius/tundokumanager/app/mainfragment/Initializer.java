package com.example.ymiyauchi.app.mainfragment;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ymiyauchi.app.R;
import com.example.ymiyauchi.app.Type;
import com.example.ymiyauchi.app.mainfragment.listctrl.ListBuilder;
import com.example.ymiyauchi.app.mainfragment.listctrl.ListContextMenu;
import com.example.ymiyauchi.app.mainfragment.listctrl.SortFilter;
import com.example.ymiyauchi.lib.view.containeradapter.CustomAdapter;
import com.example.ymiyauchi.lib.view.manager.ContainerManager;
import com.example.ymiyauchi.lib.view.manager.CustomContainerManager;

import static com.example.ymiyauchi.lib.view.containeradapter.ViewType.DEFAULT;
import static com.example.ymiyauchi.lib.view.containeradapter.ViewType.NONE;
import static com.example.ymiyauchi.lib.view.containeradapter.ViewType.TEXT;

/**
 * Created by ymiyauchi on 2017/01/20.
 * <p>
 * 主なオブジェクトの構築
 */

class Initializer {
    private final ListContextMenu mListContextMenu;
    private final CallbackTransmitter mCallbackTransmitter;

    Initializer(MainFragment fragment, Type type, View layout) {
        ListView listView = (ListView) layout.findViewById(R.id.newListView);

        ContainerManager containerManager = newContainerManager(fragment, type);
        ListBuilder listBuilder = new ListBuilder(fragment, type, containerManager);
        SortFilter sortFilter = new SortFilter(fragment, type, layout, listBuilder);
        SummaryView summaryView = new SummaryView(fragment, layout, type);
        ItemEntryManager itemEntryManager
                = new ItemEntryManager(fragment, listBuilder, sortFilter, containerManager, summaryView);

        mListContextMenu = new ListContextMenu(fragment, itemEntryManager, listView, containerManager);
        mCallbackTransmitter = new CallbackTransmitter(itemEntryManager);

        listBuilder.setupList(sortFilter, itemEntryManager, listView);
    }

    private ContainerManager newContainerManager(Fragment fragment, final Type type) {

        CustomAdapter.ViewSetter playedSetter = new CustomAdapter.ViewSetter() {
            @Override
            public void setView(Context context, int position, @NonNull View view, @Nullable Object data) {
                if (data == null) {
                    data = "";
                }
                TEXT.setView(context, position, view, data);
                if (data.equals(type.playedText(true))) {
                    view.setBackgroundColor(Color.YELLOW);
                    ((TextView) view).setTextColor(Color.BLACK);
                } else {
                    view.setBackgroundColor(Color.MAGENTA);
                    ((TextView) view).setTextColor(Color.WHITE);
                }
            }
        };

        return new CustomContainerManager(fragment.getActivity(),
                R.layout.list_item,
                new int[]{R.id.date, R.id.name, R.id.played, R.id.current, R.id.max, R.id.days,
                        R.id.value, R.id.memo, R.id.left_bracket, R.id.slash, R.id.right_bracket})
                .specifyViewSetter(
                        new int[]{R.id.left_bracket, R.id.current, R.id.slash, R.id.max, R.id.right_bracket, R.id.played},
                        type.getDialogTag() == null
                                ? new CustomAdapter.ViewSetter[]{NONE, NONE, NONE, NONE, NONE, playedSetter} :
                                new CustomAdapter.ViewSetter[]{DEFAULT, TEXT, DEFAULT, TEXT, DEFAULT, playedSetter}
                );
    }

    ListContextMenu getListContextMenu() {
        return mListContextMenu;
    }

    CallbackTransmitter getCallbackTransmitter() {
        return mCallbackTransmitter;
    }
}
