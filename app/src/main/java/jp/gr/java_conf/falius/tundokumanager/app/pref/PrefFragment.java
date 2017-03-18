package jp.gr.java_conf.falius.tundokumanager.app.pref;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import jp.gr.java_conf.falius.tundokumanager.app.R;

/**
 * Created by ymiyauchi on 2017/02/02.
 */

public class PrefFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }
}
