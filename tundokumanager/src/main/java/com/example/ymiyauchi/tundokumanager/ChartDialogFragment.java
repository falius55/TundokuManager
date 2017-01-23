package com.example.ymiyauchi.tundokumanager;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.example.ymiyauchi.tundokumanager.R;

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
        combinedChart.setDescription("");
        DataConverter data = new BundleDataConverter(getArguments());

        YAxis yAxisL = combinedChart.getAxisLeft();
        yAxisL.setAxisMaxValue(data.getCapacity());
        yAxisL.setAxisMinValue(0);

        YAxis yAxisR = combinedChart.getAxisRight();
        yAxisR.setAxisMaxValue(data.getCapacity());
        yAxisR.setAxisMinValue(0);

        combinedChart.setData(createData(data));
        combinedChart.invalidate();

        return builder.create();

    }

    private CombinedData createData(DataConverter data) {
        try (AndroidDatabase db = new BasicDatabase(getActivity())) {
            Cursor cursor = db.query("select date, cumulative_page, today_page from " + data.getType().historyTable()
                    + " where basic_id=? order by cumulative_page", Long.toString(data.getId()));

            List<String> labels = new ArrayList<>();
            while (cursor.moveToNext()) {
                DateTime date = DateTime.newInstance(
                        db.getString(HistoryColumns.DATE.getName()), DateTime.SQLITE_DATE_FORMAT);
                labels.add(date.format());
            }
            if (labels.size() < 10) {  // ラベルが一つだけなど少数の場合、棒グラフがかなりの幅を持ってしまうため追加
                DateTime date = DateTime.newInstance(labels.get(labels.size() - 1));
                for (int i = labels.size(); i < 10; i++) {
                    date = date.nextDay();
                    labels.add(date.format());
                }
            }

            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return Integer.toString((int) value);
                }
            };

            List<Entry> listEntries = new ArrayList<>();
            cursor.moveToPosition(-1);
            for (int i = 0; cursor.moveToNext(); i++) {
                int cumulativePage = db.getInt(HistoryColumns.CUMULATIVE_PAGE.getName());
                Entry entry = new Entry(cumulativePage, i);
                listEntries.add(entry);
            }
            LineDataSet lineDataSet = new LineDataSet(listEntries, "累積");
            LineData lineData = new LineData(labels, lineDataSet);
            lineData.setValueFormatter(formatter);

            List<BarEntry> barEntries = new ArrayList<>();
            cursor.moveToPosition(-1);
            for (int i = 0; cursor.moveToNext(); i++) {
                int todayPage = db.getInt(HistoryColumns.TODAY_PAGE.getName());
                BarEntry entry = new BarEntry(todayPage, i);
                barEntries.add(entry);
            }
            BarDataSet barDataSet = new BarDataSet(barEntries, "１日分");
            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            BarData barData = new BarData(labels, barDataSet);
            barData.setValueFormatter(formatter);

            CombinedData combinedData = new CombinedData(labels);
            combinedData.setData(lineData);
            combinedData.setData(barData);
            return combinedData;
        }
    }
}
