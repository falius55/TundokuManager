package com.example.ymiyauchi.tundokumanager.pref;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.ymiyauchi.tundokumanager.R;

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
