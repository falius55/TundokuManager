package jp.gr.java_conf.falius.tundokumanager.app.mainfragment;

import android.support.v4.app.Fragment;
import android.widget.Toast;

import jp.gr.java_conf.falius.tundokumanager.app.R;
import jp.gr.java_conf.falius.tundokumanager.app.Type;
import jp.gr.java_conf.falius.tundokumanager.app.data.DataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.data.ListItemDataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.database.BasicDatabase;
import jp.gr.java_conf.falius.tundokumanager.app.database.ItemColumns;
import jp.gr.java_conf.falius.tundokumanager.app.mainfragment.listctrl.ListBuilder;
import jp.gr.java_conf.falius.tundokumanager.app.mainfragment.listctrl.SortFilter;
import jp.gr.java_conf.falius.tundokumanager.lib.AndroidDatabase;
import jp.gr.java_conf.falius.tundokumanager.lib.view.manager.ContainerManager;

/**
 * Created by ymiyauchi on 2017/01/07.
 *
 * リストアイテムの登録、更新、削除
 *
 *
 */

public class ItemEntryManager {
    private static String TAG = "ITEM_ENTRY_MANAGER";
    private final ContainerManager mContainerManager;
    private final Fragment mFragment;
    private final Type mType;
    private final SummaryView mSummaryView;
    private final ListBuilder mListBuilder;
    private final SortFilter mSortFilter;
    private final HistoryController mHistoryController;

    ItemEntryManager(MainFragment fragment, ListBuilder listBuilder, SortFilter sortFilter,
                     ContainerManager containerManager, SummaryView summaryView) {
        mFragment = fragment;
        mType = fragment.type();
        mContainerManager = containerManager;
        mSummaryView = summaryView;
        mListBuilder = listBuilder;
        mSortFilter = sortFilter;
        mHistoryController = new HistoryController(fragment, mType);
    }

    void registerItem(DataConverter data) {
        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            long id = db.insert(mType.table(), data.toContentValuesForDB());
            mHistoryController.initHistory(id, db, data);
        }
        mListBuilder.build(mSortFilter.getFilter(), mSortFilter.getSort());
        mSummaryView.build();
    }


    public void updateItem(DataConverter newData) {
        DataConverter curData = new ListItemDataConverter(mType, mContainerManager, newData.getPosition());

        // 当初はupdateRowの中で一律にbuildListしていたが、そうするとリストをスクロールしていた場合に勝手に一番上に
        // 戻される形になるので煩わしく感じる
        // しかし、ただchangeItemContentでデータベースとは別にリストを操作した場合、
        // 例えば未読のみ表示していてダイアログから既読に変えても表示されたままとなり、Filter内容と実際の表示が合わなくなる
        // よって、Filterがすべてを表示になっている場合とplayedに変化がない場合のみリストを独自に操作し、
        // それ以外の場合にリストをすべて作り直す形にする
        if (mSortFilter.isDefaultFilter() || curData.getPlayedText().equals(newData.getPlayedText())) {
            mContainerManager.updateItem(newData.getPosition(),
                    new int[]{R.id.name, R.id.date, R.id.value, R.id.played,
                            R.id.current, R.id.max, R.id.memo, R.id.days},
                    new Object[]{
                            newData.getName(), newData.getDate(), newData.getPrice(), newData.getPlayedText(),
                            newData.getCurrent(), newData.getCapacity(), newData.getMemo(), newData.getDays()});
        } else {
            mListBuilder.build(mSortFilter.getFilter(), mSortFilter.getSort());
        }

        long id = newData.getId();
        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            db.update(mType.table(), newData.toContentValuesForDB(),
                    ItemColumns.ID.getName() + "=?", Long.toString(id));
        }

        mHistoryController.onUpdateItem(newData);
        mSummaryView.build();
    }

    /**
     * 指定されたポジションにあるデータを削除する
     * コンテキストメニューからの削除と、InputActivityからの削除の場合がある
     * @param data
     */
    public void deleteItem(DataConverter data) {
        long id = mContainerManager.getItemId(data.getPosition());

        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            db.delete(mType.table(), ItemColumns.ID.getName() + "=?", Long.toString(id));
            mHistoryController.deleteHistory(id, db);
        }
        mContainerManager.removeItem(data.getPosition());

        mSummaryView.build();

        Toast.makeText(mFragment.getActivity(), R.string.msg_del, Toast.LENGTH_SHORT).show();
    }

}
