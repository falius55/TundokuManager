package jp.gr.java_conf.falius.tundokumanager.app.input;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.ymiyauchi.app.R;

import jp.gr.java_conf.falius.util.datetime.DateTime;

/**
 * Created by ymiyauchi on 2017/01/16.
 */

public class DateDialogFragment extends DialogFragment {
    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "day";

    public DateDialogFragment() {
        super();
        // empty
    }

    public static DateDialogFragment newInstance(String txtDate) {
        DateTime date = DateTime.newInstance(txtDate);
        Bundle bundle = new Bundle();

        bundle.putInt(ARG_YEAR, date.getYear());
        bundle.putInt(ARG_MONTH, date.getMonth());
        bundle.putInt(ARG_DAY, date.getDay());

        DateDialogFragment df = new DateDialogFragment();
        df.setArguments(bundle);
        return df;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        Activity activity = getActivity();
        final TextView dateText = (TextView) activity.findViewById(R.id.eDate);

        return new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String strDate = DateTime.newInstance(year, monthOfYear, dayOfMonth).format();
                dateText.setText(strDate);
            }
        }, bundle.getInt(ARG_YEAR),
                bundle.getInt(ARG_MONTH),
                bundle.getInt(ARG_DAY)
        );
    }
}
