package com.powerlifting.trainer.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.mrengineer13.snackbar.SnackBar;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.powerlifting.trainer.R;
import com.powerlifting.trainer.adapters.AdapterPagerWorkout;
import com.powerlifting.trainer.utils.DBHelperPrograms;
import com.powerlifting.trainer.utils.DBHelperWorkouts;
import com.powerlifting.trainer.utils.Utils;
import com.powerlifting.trainer.views.ViewSteps;
import com.powerlifting.trainer.views.ViewWorkout;
import com.trening.timer.circularbar.library.CircularBarPager;
import com.viewpagerindicator.CirclePageIndicator;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;


public class ActivityDetail extends AppCompatActivity implements
        View.OnClickListener {

    private Toolbar mToolbar;
    private FloatingActionButton mFabAdd;
    private CircleProgressBar mPrgLoading;
    private CircularBarPager mCircularBarPager;
    private TextView mTxtTitle, mTxtSubTitle;
    private LinearLayout lytTitleLayout;


    private DBHelperPrograms mDbHelperPrograms;
    private DBHelperWorkouts mDbHelperWorkouts;


    private String mWorkoutId;
    private String mWorkoutName;
    private String mWorkoutImage;
    private String mWorkoutTime;
    private String mWorkoutSteps;
    private ArrayList<String> mWorkoutGalleries = new ArrayList<>();
    private String[] mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDays = getResources().getStringArray(R.array.day_names);

        Intent iGet = getIntent();
        mWorkoutId = iGet.getStringExtra(Utils.ARG_WORKOUT_ID);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        mPrgLoading = (CircleProgressBar) findViewById(R.id.prgLoading);
        mCircularBarPager = (CircularBarPager) findViewById(R.id.circularBarPager);
        mTxtTitle = (TextView) findViewById(R.id.txtTitle);
        mTxtSubTitle = (TextView) findViewById(R.id.txtSubTitle);
        lytTitleLayout = (LinearLayout) findViewById(R.id.lytTitleLayout);

        mFabAdd.setOnClickListener(this);

        CirclePageIndicator mCirclePageIndicator = mCircularBarPager.getCirclePageIndicator();
        mCirclePageIndicator.setFillColor(ContextCompat.getColor(this, R.color.accent_color));
        mCirclePageIndicator.setStrokeColor(ContextCompat.getColor(this, R.color.divider_color));

        mPrgLoading.setColorSchemeResources(R.color.accent_color);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(0,
                getResources().getColor(R.color.primary_color)));

        new AsyncGetWorkoutDetail().execute();

        mDbHelperPrograms = new DBHelperPrograms(getApplicationContext());
        mDbHelperWorkouts = new DBHelperWorkouts(getApplicationContext());

        try {
            mDbHelperPrograms.createDataBase();
            mDbHelperWorkouts.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        mDbHelperPrograms.openDataBase();
        mDbHelperWorkouts.openDataBase();
    }


    private class AsyncGetWorkoutDetail extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPrgLoading.setVisibility(View.VISIBLE);
            mToolbar.setVisibility(View.GONE);
            mCircularBarPager.setVisibility(View.GONE);
            mFabAdd.setVisibility(View.GONE);
            lytTitleLayout.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getWorkoutDetailFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new AsyncGetWorkoutGalleryImages().execute();
        }
    }


    private class AsyncGetWorkoutGalleryImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getWorkoutGalleryImagesFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mTxtTitle.setText(mWorkoutName);
            mTxtSubTitle.setText(mWorkoutTime);
            startViewPagerThread();
        }
    }


    private void startViewPagerThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View[] viewFlippers = new View[2];
                viewFlippers[0] = new ViewWorkout(ActivityDetail.this, mWorkoutGalleries);
                viewFlippers[1] = new ViewSteps(ActivityDetail.this, mWorkoutSteps);
                mCircularBarPager.setViewPagerAdapter(new AdapterPagerWorkout(viewFlippers));

                mPrgLoading.setVisibility(View.GONE);
                mToolbar.setVisibility(View.VISIBLE);
                mCircularBarPager.setVisibility(View.VISIBLE);
                mFabAdd.setVisibility(View.VISIBLE);
                lytTitleLayout.setVisibility(View.VISIBLE);
            }
        });
    }


    public void getWorkoutDetailFromDatabase() {
        ArrayList<Object> data;
        data = mDbHelperWorkouts.getWorkoutDetail(mWorkoutId);

        mWorkoutId = data.get(0).toString();
        mWorkoutName = data.get(1).toString();
        mWorkoutImage = data.get(2).toString();
        mWorkoutTime = data.get(3).toString();
        mWorkoutSteps = data.get(4).toString();
    }

    private void getWorkoutGalleryImagesFromDatabase() {
        ArrayList<ArrayList<Object>> data;
        data = mDbHelperWorkouts.getImages(mWorkoutId);

        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                ArrayList<Object> row = data.get(i);
                mWorkoutGalleries.add(row.get(0).toString());
            }
        } else {
            mWorkoutGalleries.add(mWorkoutImage);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabAdd:
                showDayListDialog();
            default:
                break;
        }
    }

    public void showDayListDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.days)
                .items(R.array.day_names)
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .cancelable(false)
                .positiveColorRes(R.color.text_and_icon_color)
                .negativeColorRes(R.color.text_and_icon_color)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view,
                                               int selectedIndex, CharSequence text) {
                        if (mDbHelperPrograms.isDataAvailable((selectedIndex + 1), mWorkoutId)) {
                            showSnackbar(getString(R.string.workout_already_added) + " " +
                                    mDays[selectedIndex] + " " +
                                    getString(R.string.program) + ".");
                        } else {
                            mDbHelperPrograms.addData(
                                    Integer.valueOf(mWorkoutId),
                                    mWorkoutName,
                                    (selectedIndex + 1),
                                    mWorkoutImage,
                                    mWorkoutTime,
                                    mWorkoutSteps);
                            showSnackbar(getString(R.string.workout_successfully_added) +
                                    " " + mDays[selectedIndex] + " " +
                                    getString(R.string.program) + ".");
                        }
                        return true;
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .show();
    }


    public void showSnackbar(String message) {
        SnackBar snackbar = new SnackBar.Builder(this)
                .withMessage(message)
                .show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }
}