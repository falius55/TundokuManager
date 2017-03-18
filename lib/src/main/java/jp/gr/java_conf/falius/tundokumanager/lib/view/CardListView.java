package jp.gr.java_conf.falius.tundokumanager.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import jp.gr.java_conf.falius.tundokumanager.lib.R;

/**
 * TODO: document your custom view class.
 */
public class CardListView extends ListView {

    public CardListView(Context context) {
        super(context);
        init(context);
    }

    public CardListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CardListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        int padding = (int) (context.getResources().getDisplayMetrics().density * 8);
        setPadding(padding, 0, padding, 0);
        setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        setDivider(null);

        LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
        View header = inflater.inflate(R.layout.list_header_footer, this, false);
        View footer = inflater.inflate(R.layout.list_header_footer, this, false);
        addHeaderView(header, null, false);
        addFooterView(footer, null, false);
    }
}
