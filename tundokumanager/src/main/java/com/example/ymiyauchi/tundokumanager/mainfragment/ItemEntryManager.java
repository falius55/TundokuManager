package com.example.ymiyauchi.tundokumanager.mainfragment;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.example.ymiyauchi.tundokumanager.database.BasicDatabase;
import com.example.ymiyauchi.tundokumanager.database.ItemColumns;
import com.example.ymiyauchi.tundokumanager.database.HistoryColumns;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.data.ListItemDataConverter;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.tundokumanager.mainfragment.listctrl.ListBuilder;
import com.example.ymiyauchi.tundokumanager.mainfragment.listctrl.SortFilter;
import com.example.ymiyauchi.mylibrary.AndroidDatabase;
import com.example.ymiyauchi.mylibrary.DateTime;
import com.example.ymiyauchi.mylibrary.view.manager.ContainerManager;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * リストアイテムの登録、更新、削除
 */

public class ItemEntryManager {
    private static String TAG = "ITEM_ENTRY_MANAGER";
    private final ContainerManager mContainerManager;
    private final Fragment mFragment;
    private final Type mType;
    private final SummaryView mSummaryView;
    private final ListBuilder mListBuilder;
    private final SortFilter mSortFilter;

    ItemEntryManager(MainFragment fragment, ListBuilder listBuilder, SortFilter sortFilter,
                     ContainerManager containerManager, SummaryView summaryView) {
        mFragment = fragment;
        mType = fragment.type();
        mContainerManager = containerManager;
        mSummaryView = summaryView;
        mListBuilder = listBuilder;
        mSortFilter = sortFilter;
    }

    void registerItem(DataConverter data) {
        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            db.insert(mType.table(), data.toContentValuesForDB());
            initHistory(db, data);
        }
        mListBuilder.build(mSortFilter.getFilter(), mSortFilter.getSort());
        mSummaryView.build();
    }

    private void initHistory(AndroidDatabase db, DataConverter data) {
        if (!data.getType().hasProgress()) {
            return;
        }
        // 新規登録する前ではまだidはないので(dataからは-1が返ってくる)、登録後の時点でデータベースからidを取得する
        db.selectAllColumn(mType.table(), "name=? and date=? and price=? and current=? and capacity=? and memo=?",
                data.getName(), data.getDateForDB(), Integer.toString(data.getPrice()),
                Integer.toString(data.getCurrent()), Integer.toString(data.getCapacity()), data.getMemo());
        if (!db.next()) {
            throw new SQLException("could\'nt register history table");
        }
        int id = db.getInt(ItemColumns.ID.getName());

        ContentValues values = new ContentValues();
        values.put(HistoryColumns.BASIC_ID.getName(), id);
        values.put(HistoryColumns.TODAY_PAGE.getName(), data.getCurrent());  // 未読なら０、既読ならcapacityが登録される
        values.put(HistoryColumns.CUMULATIVE_PAGE.getName(), data.getCurrent());
        values.put(HistoryColumns.DATE.getName(), DateTime.now().formatTo(DateTime.SQLITE_DATE_FORMAT));
        db.insert(mType.historyTable(), values);
    }

    public void updateItem(DataConverter newData) {
        DataConverter curData = new ListItemDataConverter(mType, mContainerManager, newData.getPosition());
        int oldCurrent = curData.getCurrent();

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
            updateHistoryDB(newData, oldCurrent);
        }

        mSummaryView.build();
    }

    private void updateHistoryDB(DataConverter newData, int oldCurrent) {
        if (!mType.hasProgress()) {
            return;
        }

        try (BasicDatabase db = new BasicDatabase(mFragment.getActivity())) {
            int playedPage = newData.getCurrent() - oldCurrent;  // 今日読んだページ数
            int cumulativePage = newData.getCurrent();  // 累積ページ数

            if (playedPage == 0) {
                return;
            }  // 読んだページ数に変化なし

            if (playedPage < 0) {  // 読んだページを取り消した
                decreaseHistoryPage(db, newData, cumulativePage);
                return;
            }

            // 今日の分がすでに登録されているか確認
            String id = Long.toString(newData.getId());
            String today = DateTime.now().formatTo(DateTime.SQLITE_DATE_FORMAT);
            if (db.isExist(mType.historyTable(), "basic_id=? and date=?", id, today)) {
                // 今日の分がすでに登録されている場合の処理
                updateHistoryRecord(db, newData, cumulativePage, playedPage);
                return;
            }
            // 今日の分がまだ登録されていないので新規登録
            addHistoryRecord(db, newData, cumulativePage, playedPage);
        }
    }

    private void decreaseHistoryPage(BasicDatabase db, DataConverter data, int cumulativePage) {
        // 従来の登録が
        // 1/1 累積;100p, 当日:100p
        // 1/2 累積:150p, 当日:50p
        // 1/3 累積:180p, 当日:30p
        // 1/4 累積:250p, 当日:70p
        // の場合に、累積を160pまで減らした場合
        // 1/1 累積:100p, 当日:100p
        // 1/2 累積:150p, 当日:50p
        // 1/3 累積:160p, 当日:10p
        // と変更する
        String id = Long.toString(data.getId());

        // 累積ページが超えている中で最小の値を持っている日(再登録される日、上の例では1/3を取得)
        int targetCumulativePage = db.min(mType.historyTable(),
                "cumulative_page", "basic_id=? and cumulative_page > ?", id, Integer.toString(cumulativePage));
        db.selectAllColumn(mType.historyTable(),
                "basic_id=? and cumulative_page=?", id, Integer.toString(targetCumulativePage));
        if (!db.next()) {
            throw new SQLiteException("no data date : " + targetCumulativePage);
        }
        String date = db.getString(HistoryColumns.DATE.getName());

        db.delete(mType.historyTable(), "basic_id=? and cumulative_page > ?",  // 累積ページ数が新しい数値より大きいもの(上の例では1/3と1/4)を削除
                id, Integer.toString(cumulativePage));

        // 削除した以前のデータで累積ページ数が最大のものを取得(上の例では1/2。今回の累積ページとの差が再登録の日に読んだ分)
        int maxCumulative = db.max(mType.historyTable(), "cumulative_page", "basic_id=?", id);

        int newPlayedPage = cumulativePage - maxCumulative;
        if (newPlayedPage == 0) {
            return;
        }

        // 差分を再登録
        ContentValues values = new ContentValues();
        values.put(HistoryColumns.DATE.getName(), date);
        values.put(HistoryColumns.CUMULATIVE_PAGE.getName(), cumulativePage);
        values.put(HistoryColumns.TODAY_PAGE.getName(), newPlayedPage);
        values.put(HistoryColumns.BASIC_ID.getName(), data.getId());
        db.insert(mType.historyTable(), values);
    }

    /**
     * @param db
     * @param data
     * @param cumulativePage 新しい累積ページ数
     * @param playedPage     新しく読んだページ数
     */
    private void updateHistoryRecord(BasicDatabase db, DataConverter data, int cumulativePage, int playedPage) {
        // 新しい累積が前回より減った状態では呼ばれないので、増えた場合だけを想定する
        db.selectAllColumn(mType.historyTable(), "basic_id=? and date = ?",
                Long.toString(data.getId()), DateTime.now().formatTo(DateTime.SQLITE_DATE_FORMAT));
        if (!db.next()) {
            throw new SQLiteException();
        }
        int newPlayedPage = db.getInt(HistoryColumns.TODAY_PAGE.getName()) + playedPage;
        ContentValues values = new ContentValues();
        values.put(HistoryColumns.TODAY_PAGE.getName(), newPlayedPage);
        values.put(HistoryColumns.CUMULATIVE_PAGE.getName(), cumulativePage);
        db.update(mType.historyTable(), values, "basic_id=? and date = ?",
                Long.toString(data.getId()), DateTime.now().formatTo(DateTime.SQLITE_DATE_FORMAT));
    }

    private void addHistoryRecord(BasicDatabase db, DataConverter data, int cumulativePage, int playedPage) {
        ContentValues values = new ContentValues();
        values.put(HistoryColumns.DATE.getName(), DateTime.now().formatTo(DateTime.SQLITE_DATE_FORMAT));
        values.put(HistoryColumns.TODAY_PAGE.getName(), playedPage);
        values.put(HistoryColumns.CUMULATIVE_PAGE.getName(), cumulativePage);
        values.put(HistoryColumns.BASIC_ID.getName(), data.getId());
        db.insert(mType.historyTable(), values);
    }

    /**
     * 指定されたポジションにあるデータを削除する
     * コンテキストメニューからの削除と、InputActivityからの削除の場合がある
     *
     * @param data
     */
    public void deleteItem(DataConverter data) {
        long id = mContainerManager.getItemId(data.getPosition());

        try (AndroidDatabase db = new BasicDatabase(mFragment.getActivity())) {
            db.delete(mType.table(), ItemColumns.ID.getName() + "=?", Long.toString(id));
            db.delete(mType.historyTable(), "basic_id=?", Long.toString(id));
        }
        mContainerManager.removeItem(data.getPosition());

        mSummaryView.build();

        Toast.makeText(mFragment.getActivity(), R.string.msg_del, Toast.LENGTH_SHORT).show();
    }

}
