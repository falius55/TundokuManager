package com.example.ymiyauchi.tundokumanager;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ymiyauchi.mylibrary.DateTime;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.mainfragment.HistoryController;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.List;

/**
 * Created by ymiyauchi on 2017/01/26.
 */

public class ResultChanger implements OnChartValueSelectedListener, View.OnClickListener {
    private final DataConverter mItemData;
    private final List<String> mLabels;
    private final List<BarEntry> mBarEntries;
    private final List<Entry> mLineEntries;

    private final TextView mDateText;
    private final SeekBar mDaySeekBar;
    private final SeekBar mCumulativeSeekBar;
    private final ChartDialogFragment mFragment;

    private final HistoryController mHistoryController;

    private DateTime mSelectedDate;

    public ResultChanger(ChartDialogFragment fragment, View layout, DataConverter itemData, List<String> labels, List<BarEntry> barEntries, List<Entry> lineEntries) {
        mFragment = fragment;
        mItemData = itemData;
        mLabels = labels;
        mBarEntries = barEntries;
        mLineEntries = lineEntries;

        mDateText = (TextView) layout.findViewById(R.id.txt_graph_date);
        mDaySeekBar = (SeekBar) layout.findViewById(R.id.seekbar_result_day);
        mCumulativeSeekBar = (SeekBar) layout.findViewById(R.id.seekbar_result_cumulative);

        mHistoryController = new HistoryController(fragment, itemData.getType());
        init(layout);
    }

    private void init(View layout) {
        mCumulativeSeekBar.setMax(mItemData.getCapacity());
        mDateText.setText(mLabels.get(mLabels.size() - 1));

        TextChanger dayTextChanger = new TextChanger(mFragment, layout, R.id.txt_seekbar_day, mCumulativeSeekBar);
        mDaySeekBar.setOnSeekBarChangeListener(dayTextChanger);
        int lastDayResult = (int) mBarEntries.get(mBarEntries.size() - 1).getVal();
        dayTextChanger.onProgressChanged(mDaySeekBar, lastDayResult, false);
        mDaySeekBar.setEnabled(false);

        TextChanger cumulativeTextChanger = new TextChanger(mFragment, layout, R.id.txt_seekbar_cumulative, mDaySeekBar);
        mCumulativeSeekBar.setOnSeekBarChangeListener(cumulativeTextChanger);
        int lastCumulativeResult = (int) mLineEntries.get(mLineEntries.size() - 1).getVal();
        cumulativeTextChanger.onProgressChanged(mCumulativeSeekBar, lastCumulativeResult, false);
        mCumulativeSeekBar.setEnabled(false);

        Button btnYesterday = (Button) layout.findViewById(R.id.btn_yesterday);
        btnYesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTime currentDate = DateTime.newInstance(mDateText.getText().toString());
                String prevDay = currentDate.prevDay().format();
                int index = mLabels.indexOf(prevDay);
                // FIXME: もしグラフにない日付が選ばれるとIndexOutOfBoundsExceptionが発生する
                onValueSelected(mBarEntries.get(index), 0, null);
                mDateText.setText(prevDay);
            }
        });
        Button btnTomorrow = (Button) layout.findViewById(R.id.btn_tomorrow);
        btnTomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTime currentDate = DateTime.newInstance(mDateText.getText().toString());
                String nextDay = currentDate.nextDay().format();
                int index = mLabels.indexOf(nextDay);
                onValueSelected(mBarEntries.get(index), 0, null);
                mDateText.setText(nextDay);
            }
        });
    }

    /**
     * @param e            BarEntry
     * @param dataSetIndex DataSetのインデックス。今回は常に0
     * @param h
     */
    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        int index = e.getXIndex();
        mSelectedDate = DateTime.newInstance(mLabels.get(index));
        int resultDay = (int) mBarEntries.get(index).getVal();
        int resultCumulative = (int) mLineEntries.get(index).getVal();
        int cumulativeOfDayBeforeSelected = index == 0 ? 0 : (int) mLineEntries.get(index - 1).getVal();
        int dayLimit = mItemData.getCapacity() - cumulativeOfDayBeforeSelected;

        mDateText.setText(mSelectedDate.format());

        mDaySeekBar.setMax(dayLimit);

        mDaySeekBar.setProgress(resultDay);
        mCumulativeSeekBar.setProgress(resultCumulative);

        mDaySeekBar.setEnabled(true);
        mCumulativeSeekBar.setEnabled(true);
    }

    @Override
    public void onNothingSelected() {
        mDaySeekBar.setEnabled(false);
        mCumulativeSeekBar.setEnabled(false);
        mSelectedDate = null;
    }

    @Override
    public void onClick(View view) {
        if (mSelectedDate == null) {
            return;
        }

        int dayProgress = mDaySeekBar.getProgress();
        if (dayProgress == 0) {
            mHistoryController.updateHistoryCumulativePlayed(mItemData, mSelectedDate, mCumulativeSeekBar.getProgress());
        } else {
            mHistoryController.updateHistoryTodayPlayed(mItemData, mSelectedDate, dayProgress);
        }
        mFragment.reload();
    }


    private static class TextChanger implements SeekBar.OnSeekBarChangeListener {
        private final Fragment mFragment;
        private final TextView mCurValText;
        private final SeekBar mAnotherSeekBar;
        private int mMemory = 0;

        TextChanger(Fragment fragment, View layout, @IdRes int textViewRes, SeekBar anotherSeekBar) {
            mFragment = fragment;
            mCurValText = (TextView) layout.findViewById(textViewRes);
            mAnotherSeekBar = anotherSeekBar;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String curText = mFragment.getString(R.string.seekbar_value, progress);
            mCurValText.setText(curText);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mMemory = seekBar.getProgress();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int delta = seekBar.getProgress() - mMemory;
            mMemory = seekBar.getProgress();
            mAnotherSeekBar.setProgress(mAnotherSeekBar.getProgress() + delta);
        }
    }
}
