package com.powerlifting.trainer.activities;

import android.os.Bundle;

import com.lb.material_preferences_library.PreferenceActivity;
import com.lb.material_preferences_library.custom_preferences.Preference;
import com.powerlifting.trainer.R;


public class ActivityAbout extends PreferenceActivity
        implements Preference.OnPreferenceClickListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {

        setTheme(R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);




    }

    @Override
    protected int getPreferencesXmlId()
    {

        return R.xml.pref_about;
    }


    @Override
    public boolean onPreferenceClick(android.preference.Preference preference) {
        switch(preference.getKey()){

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

}
