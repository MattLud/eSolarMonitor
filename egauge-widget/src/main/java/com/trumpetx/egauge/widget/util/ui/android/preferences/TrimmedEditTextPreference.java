package com.trumpetx.egauge.widget.util.ui.android.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by mludlum on 3/3/16.
 */
public class TrimmedEditTextPreference extends EditTextPreference {

    public TrimmedEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TrimmedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrimmedEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public void setText(String text){
        super.setText(text.trim());
    }

    @Override
    public String getText()
    {
        return super.getText().trim();
    }

}
