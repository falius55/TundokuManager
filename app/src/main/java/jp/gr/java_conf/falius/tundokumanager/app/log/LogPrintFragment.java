package jp.gr.java_conf.falius.tundokumanager.app.log;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ymiyauchi.app.R;

public class LogPrintFragment extends Fragment {


    public LogPrintFragment() {
        // Required empty public constructor
    }

    public static LogPrintFragment newInstance() {
        LogPrintFragment fragment = new LogPrintFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_print, container, false);
        TextView textView = (TextView) view.findViewById(R.id.text_log);
        LogFile logFile = new LogFile(getActivity());

        String logText = logFile.readFile();
        textView.setText(logText);
        return view;
    }
}
