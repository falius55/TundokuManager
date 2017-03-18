package jp.gr.java_conf.falius.tundokumanager.app;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ymiyauchi.app.R;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.List;

import jp.gr.java_conf.falius.tundokumanager.app.data.DataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.data.MutableDataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.mainfragment.HistoryController;
import jp.gr.java_conf.falius.util.datetime.DateTime;

/**
 * Created by ymiyauchi on 2017/01/26.
 *
 * チャートダイアログで利用するリスナー定義
 */

class ResultChanger implements OnChartValueSelectedListener, View.OnClickListener {
    private final CombinedChart mCombinedChart;
    private final List<String> mLabels;
    private final List<BarEntry> mBarEntries;
    private final List<Entry> mLineEntries;
    private DataConverter mItemData;

    private final TextView mDateText;
    private final SeekBar mDaySeekBar;
    private final ChartDialogFragment mFragment;

    private final HistoryController mHistoryController;

    ResultChanger(ChartDialogFragment fragment, View layout, DataConverter itemData, CombinedChart combinedChart,
                  List<String> labels, List<BarEntry> barEntries, List<Entry> lineEntries) {
        mFragment = fragment;
        mItemData = itemData;
        mCombinedChart = combinedChart;
        mLabels = labels;
        mBarEntries = barEntries;
        mLineEntries = lineEntries;

        mDateText = (TextView) layout.findViewById(R.id.txt_graph_date);
        mDaySeekBar = (SeekBar) layout.findViewById(R.id.seekbar_result_day);

        mHistoryController = new HistoryController(fragment, itemData.getType());
        init(layout);
    }

    private void init(View layout) {
        int lastIndex = mBarEntries.size() - 1;
        mDateText.setText(mLabels.get(lastIndex));

        ProgressTextChanger dayProgressTextChanger = new ProgressTextChanger(mFragment, layout, R.id.txt_seekbar_day);
        mDaySeekBar.setOnSeekBarChangeListener(dayProgressTextChanger);
        int lastDayResult = (int) mBarEntries.get(mBarEntries.size() - 1).getVal();
        dayProgressTextChanger.onProgressChanged(mDaySeekBar, lastDayResult, false);

        setSeekBar();

        Button btnYesterday = (Button) layout.findViewById(R.id.btn_yesterday);
        btnYesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTime currentDate = DateTime.newInstance(mDateText.getText().toString());
                String prevDay = currentDate.prevDay().format();
                if (currentDate.format().equals(mItemData.getDate())) {
                    return;
                }
                mDateText.setText(prevDay);
                setSeekBar();
            }
        });
        Button btnTomorrow = (Button) layout.findViewById(R.id.btn_tomorrow);
        btnTomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTime currentDate = DateTime.newInstance(mDateText.getText().toString());
                DateTime today = DateTime.now();
                DateTime nextDay = currentDate.nextDay();
                if (nextDay.compareTo(today) > 0) {
                    return;
                }
                mDateText.setText(nextDay.format());
                setSeekBar();
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

        DateTime selectedDate = DateTime.newInstance(mLabels.get(index));
        mDateText.setText(selectedDate.format());

        setSeekBar();
    }

    @Override
    public void onNothingSelected() {
    }

    private void setSeekBar() {
        DateTime currentDate = DateTime.newInstance(mDateText.getText().toString());
        DateTime firstDate = DateTime.newInstance(mLabels.get(0));
        if (currentDate.compareTo(firstDate) < 0) {
            return;
        }

        int dataIndex = mLabels.indexOf(currentDate.format());
        boolean isExistData = dataIndex != -1 && dataIndex < mBarEntries.size();
        int resultDay;
        int cumulativeOfDayBeforeSelected;
        if (isExistData) {
            resultDay = (int) mBarEntries.get(dataIndex).getVal();
            cumulativeOfDayBeforeSelected = dataIndex == 0 ? 0 : (int) mLineEntries.get(dataIndex - 1).getVal();
        } else {
            resultDay = 0;
            int lastDateIndex = mLineEntries.size() - 1;
            cumulativeOfDayBeforeSelected = (int) mLineEntries.get(lastDateIndex).getVal();
        }
        int dayLimit = mItemData.getCapacity() - cumulativeOfDayBeforeSelected;
        mDaySeekBar.setMax(dayLimit);
        mDaySeekBar.setProgress(resultDay);
    }

    @Override
    public void onClick(View view) {
        DateTime selectedDate = DateTime.newInstance(mDateText.getText().toString());

        int dayProgress = mDaySeekBar.getProgress();
        mHistoryController.updateDayResult(mItemData, selectedDate, dayProgress);

        mFragment.reload();

        int lastCumulative = (int) mLineEntries.get(mLineEntries.size() - 1).getVal();
        if (lastCumulative != mItemData.getCurrent()) {
            DataConverter newData = new MutableDataConverter(mItemData)
                    .putCurrent(lastCumulative).putPlayed(lastCumulative == mItemData.getCapacity());
            MainActivity mainActivity = (MainActivity) mFragment.getActivity();
            mainActivity.onDialogLeaved(newData);
            mItemData = newData;
        }
    }


    private static class ProgressTextChanger implements SeekBar.OnSeekBarChangeListener {
        private final Fragment mFragment;
        private final TextView mCurValText;

        ProgressTextChanger(Fragment fragment, View layout, @IdRes int textViewRes) {
            mFragment = fragment;
            mCurValText = (TextView) layout.findViewById(textViewRes);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String curText = mFragment.getString(R.string.seekbar_value, progress);
            mCurValText.setText(curText);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
