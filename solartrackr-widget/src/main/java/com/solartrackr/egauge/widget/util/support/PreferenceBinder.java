package com.solartrackr.egauge.widget.util.support;

import android.preference.Preference;

public interface PreferenceBinder {
    Preference findPreference(CharSequence prefName);

    void addPreferencesFromResource(int preferencesResId);
}