package com.example.ymiyauchi.tundokumanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.ymiyauchi.tundokumanager.data.BundleDataConverter;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.database.BasicDatabase;
import com.example.ymiyauchi.tundokumanager.database.HistoryColumns;
import com.example.ymiyauchi.mylibrary.AndroidDatabase;
import com.example.ymiyauchi.mylibrary.DateTime;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymiyauchi on 2017/01/22.
 */

public class ChartDialogFragment extends DialogFragment {
    private static String TAG = "CHART_DIALOG";

    private DataConverter mItemData;

    private final List<String> mLabels = new ArrayList<>();
    private final List<BarEntry> mBarEntries = new ArrayList<>();
    private final List<Entry> mLineEntries = new ArrayList<>();
    private CombinedChart mCombinedChart;

    public static ChartDialogFragment newInstance(DataConverter data) {
        ChartDialogFragment fragment = new ChartDialogFragment();
        fragment.setArguments(data.toBundle());
        return fragment;
    }

    public ChartDialogFragment() {
        super();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")  // setDescription()で警告が出てくるので抑制(原因不明)
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_graph, null);
        builder.setView(layout);

        CombinedChart combinedChart = (CombinedChart) layout.findViewById(R.id.combined_chart);
        mCombinedChart = combinedChart;
        combinedChart.setDescription("");
        DataConverter data = new BundleDataConverter(getArguments());
        mItemData = data;

        YAxis yAxisL = combinedChart.getAxisLeft();
        yAxisL.setAxisMaxValue(data.getCapacity());
        yAxisL.setAxisMinValue(0);

        YAxis yAxisR = combinedChart.getAxisRight();
        yAxisR.setAxisMaxValue(data.getCapacity());
        yAxisR.setAxisMinValue(0);

        combinedChart.setData(createData(data));

        ResultChanger resultChanger = new ResultChanger(this, layout, data, mLabels, mBarEntries, mLineEntries);
        Button reflectButton = (Button) layout.findViewById(R.id.btn_reflect);
        reflectButton.setOnClickListener(resultChanger);
        combinedChart.setOnChartValueSelectedListener(resultChanger);

        combinedChart.invalidate();

        return builder.setTitle(data.getName()).create();

    }

    void reload() {
        loadEntries(mItemData);
        mCombinedChart.invalidate();
    }

    private void loadEntries(DataConverter data) {
        try (AndroidDatabase db = new BasicDatabase(getActivity())) {
            db.selectWithOrder(data.getType().historyTable(),
                    HistoryColumns.values(),
                    /* order by */ HistoryColumns.DATE.getName(),
                    /* where */ HistoryColumns.BASIC_ID.getName() + "=?", Long.toString(data.getId()));
            if (!db.next()) {
                throw new IllegalStateException();
            }

            mLabels.clear();
            mBarEntries.clear();
            mLineEntries.clear();
            List<String> labels = mLabels;
            List<BarEntry> barEntries = mBarEntries;
            List<Entry> lineEntries = mLineEntries;

            // 購入日から始まり、データのない日も値を格納していく
            // データのない日は当日分は０、累計はその前日の値をそのまま受け継ぐ
            // cursorDateと一致するまでdateを更新していき、一致するとcursorDateも更新して続ける(cursorDateが更新できなくなるまで)
            // するとカーソルの最初日から最終日までdateが逐次更新される
            DateTime firstDate = DateTime.newInstance(data.getDate());
            DateTime endDate = DateTime.now().nextDay();
            DateTime date = firstDate;
            DateTime cursorDate = DateTime.newInstance(db.getString(HistoryColumns.DATE.getName()), DateTime.SQLITE_DATE_FORMAT);
            if (firstDate.compareTo(cursorDate) > 0) {
                // 購入日以前のデータが存在するとdateとcursorDateが一致しなくなり、無限ループに陥るため例外送出
                throw new IllegalStateException("firstData:" + firstDate + ", cursorDate:" + cursorDate);
            }
            int cumulativePage = 0;
            for (int i = 0; !date.equals(endDate); i++, date = date.nextDay()) {
                labels.add(date.format());
                if (date.equals(cursorDate)) {
                    int todayPage = db.getInt(HistoryColumns.TODAY_PAGE.getName());
                    BarEntry barEntry = new BarEntry(todayPage, i);
                    barEntries.add(barEntry);

                    cumulativePage += todayPage;
                    Entry entry = new Entry(cumulativePage, i);
                    lineEntries.add(entry);

                    if (db.next()) {
                        cursorDate = DateTime.newInstance(db.getString(HistoryColumns.DATE.getName()), DateTime.SQLITE_DATE_FORMAT);
                    }
                } else {
                    BarEntry barEntry = new BarEntry(0, i);
                    barEntries.add(barEntry);

                    Entry entry = new Entry(cumulativePage, i);
                    lineEntries.add(entry);
                }
            }

            if (labels.size() < 10) {  // ラベルが一つだけなど少数の場合、棒グラフがかなりの幅を持ってしまうため追加
                for (int i = labels.size(); i < 10; i++, date = date.nextDay()) {
                    labels.add(date.format());
                }
            }
        }
    }

    private CombinedData createData(DataConverter data) {
        loadEntries(data);

        List<String> labels = mLabels;
        List<BarEntry> barEntries = mBarEntries;
        List<Entry> lineEntries = mLineEntries;

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return Integer.toString((int) value);
            }
        };

        BarDataSet barDataSet = new BarDataSet(barEntries, "１日分");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(labels, barDataSet);
        barData.setValueFormatter(formatter);

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "累積");
        LineData lineData = new LineData(labels, lineDataSet);
        lineData.setValueFormatter(formatter);

        CombinedData combinedData = new CombinedData(labels);
        combinedData.setData(barData);
        combinedData.setData(lineData);

        return combinedData;
    }
}
