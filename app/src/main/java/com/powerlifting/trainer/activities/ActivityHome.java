package com.powerlifting.trainer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.powerlifting.trainer.fragments.FragmentCategories;
import com.powerlifting.trainer.fragments.FragmentPrograms;
import com.tabs.samples.apps.design.ui.widget.SlidingTabLayout;
import com.powerlifting.trainer.R;
import com.powerlifting.trainer.adapters.AdapterNavigation;
import com.powerlifting.trainer.utils.Utils;

public class ActivityHome extends AppCompatActivity implements
        FragmentCategories.OnSelectedCategoryListener,
        FragmentPrograms.OnSelectedDayListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        View mHeaderView = findViewById(R.id.header);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);

        AdapterNavigation mAdapterNavigation =
                new AdapterNavigation(this, getSupportFragmentManager());

        ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));
        mPager.setAdapter(mAdapterNavigation);
        setSupportActionBar(mToolbar);

        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mSlidingTabLayout.setSelectedIndicatorColors(ContextCompat.
                getColor(this, R.color.accent_color));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mPager);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menuAbout:
                        Intent aboutIntent = new Intent(getApplicationContext(),
                                ActivityAbout.class);
                        startActivity(aboutIntent);
                        overridePendingTransition(R.anim.open_next, R.anim.close_main);
                        return true;
                    default:
                        return true;
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_home, menu);
        return true;
    }

    @Override
    public void onSelectedCategory(String selectedID, String selectedName) {
        Intent detailIntent = new Intent(this, ActivityWorkouts.class);
        detailIntent.putExtra(Utils.ARG_WORKOUT_ID, selectedID);
        detailIntent.putExtra(Utils.ARG_WORKOUT_NAME, selectedName);
        detailIntent.putExtra(Utils.ARG_PARENT_PAGE, Utils.ARG_WORKOUTS);
        startActivity(detailIntent);
        overridePendingTransition(R.anim.open_next, R.anim.close_main);
    }

    @Override
    public void onSelectedDay(String selectedID, String selectedName) {
        Intent detailIntent = new Intent(this, ActivityWorkouts.class);
        detailIntent.putExtra(Utils.ARG_WORKOUT_ID, selectedID);
        detailIntent.putExtra(Utils.ARG_WORKOUT_NAME, selectedName);
        detailIntent.putExtra(Utils.ARG_PARENT_PAGE, Utils.ARG_PROGRAMS);
        startActivity(detailIntent);
        overridePendingTransition(R.anim.open_next, R.anim.close_main);

    }

}

