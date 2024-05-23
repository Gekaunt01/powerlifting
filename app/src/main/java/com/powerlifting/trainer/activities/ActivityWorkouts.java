package com.powerlifting.trainer.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.powerlifting.trainer.utils.DBHelperWorkouts;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.powerlifting.trainer.R;
import com.powerlifting.trainer.adapters.AdapterWorkouts;
import com.powerlifting.trainer.listeners.OnTapListener;
import com.powerlifting.trainer.utils.DBHelperPrograms;
import com.powerlifting.trainer.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class ActivityWorkouts extends AppCompatActivity implements View.OnClickListener {

    private CircleProgressBar mPrgLoading;
    public static RecyclerView sList;
    public static TextView sTxtAlert;
    public static RelativeLayout sLytSubHeader;

    private String mProgramName;
    private String mSelectedId;
    private String mParentPage;

    private AdapterWorkouts mAdapterWorkouts;

    private DBHelperWorkouts mDbHelperWorkouts;
    private DBHelperPrograms mDbHelperPrograms;

    private ArrayList<String> mProgramIds     = new ArrayList<>();
    private ArrayList<String> mWorkoutIds     = new ArrayList<>();
    private ArrayList<String> mWorkoutNames   = new ArrayList<>();
    private ArrayList<String> mWorkoutImages  = new ArrayList<>();
    private ArrayList<String> mWorkoutTimes   = new ArrayList<>();
    private ArrayList<String> mWorkoutSteps   = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        Intent i        = getIntent();
        mSelectedId     = i.getStringExtra(Utils.ARG_WORKOUT_ID);
        mProgramName    = i.getStringExtra(Utils.ARG_WORKOUT_NAME);
        mParentPage     = i.getStringExtra(Utils.ARG_PARENT_PAGE);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mPrgLoading     = (CircleProgressBar) findViewById(R.id.prgLoading);
        AppCompatButton mRaisedStart = (AppCompatButton) findViewById(R.id.raisedStart);
        sList           = (RecyclerView) findViewById(R.id.list);
        sTxtAlert       = (TextView) findViewById(R.id.txtAlert);
        sLytSubHeader   = (RelativeLayout) findViewById(R.id.lytSubHeaderLayout);

        mRaisedStart.setOnClickListener(this);

        if(mParentPage.equals(Utils.ARG_WORKOUTS)){
            sLytSubHeader.setVisibility(View.GONE);
        }else{
            sLytSubHeader.setVisibility(View.VISIBLE);
        }

        mToolbar.setTitle(mProgramName);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sList.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        sList.setLayoutManager(mLayoutManager);

        mPrgLoading.setColorSchemeResources(R.color.accent_color);

        mDbHelperWorkouts = new DBHelperWorkouts(this);
        mDbHelperPrograms = new DBHelperPrograms(this);

        try {
            mDbHelperWorkouts.createDataBase();
            mDbHelperPrograms.createDataBase();
        }catch(IOException ioe){
            throw new Error("Unable to create database");
        }

        mDbHelperWorkouts.openDataBase();
        mDbHelperPrograms.openDataBase();

        mAdapterWorkouts = new AdapterWorkouts(this, mSelectedId, mParentPage, mDbHelperPrograms);
        new AsyncGetWorkoutList().execute();

        mAdapterWorkouts.setOnTapListener(new OnTapListener() {
            @Override
            public void onTapView(String id, String Name) {
                Intent i = new Intent(getApplicationContext(), ActivityDetail.class);
                i.putExtra(Utils.ARG_WORKOUT_ID, id);
                i.putExtra(Utils.ARG_PARENT_PAGE, mParentPage);
                startActivity(i);
                overridePendingTransition(R.anim.open_next, R.anim.close_main);
            }
        });

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.raisedStart:
                Intent detailIntent = new Intent(this, ActivityStopWatch.class);
                mWorkoutIds = mAdapterWorkouts.getData(0);
                mWorkoutNames = mAdapterWorkouts.getData(1);
                mWorkoutImages = mAdapterWorkouts.getData(2);
                mWorkoutTimes = mAdapterWorkouts.getData(3);
                detailIntent.putExtra(Utils.ARG_WORKOUT_IDS, mWorkoutIds);
                detailIntent.putExtra(Utils.ARG_WORKOUT_NAMES, mWorkoutNames);
                detailIntent.putExtra(Utils.ARG_WORKOUT_IMAGES, mWorkoutImages);
                detailIntent.putExtra(Utils.ARG_WORKOUT_TIMES, mWorkoutTimes);
                detailIntent.putExtra(Utils.ARG_WORKOUT_NAME, mProgramName);
                startActivity(detailIntent);
                overridePendingTransition(R.anim.open_next, R.anim.close_main);
                break;
        }
    }
    private class AsyncGetWorkoutList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPrgLoading.setVisibility(View.VISIBLE);
            sList.setVisibility(View.GONE);
            sLytSubHeader.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getWorkoutListDataFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mPrgLoading.setVisibility(View.GONE);
            if(mWorkoutIds.isEmpty()){
                sTxtAlert.setVisibility(View.VISIBLE);
            } else {
                sTxtAlert.setVisibility(View.GONE);
                sList.setVisibility(View.VISIBLE);
                mAdapterWorkouts.updateList(mProgramIds, mWorkoutIds, mWorkoutNames,
                        mWorkoutImages, mWorkoutTimes, mWorkoutSteps);
            }

            if(!mWorkoutIds.isEmpty() && mParentPage.equals(Utils.ARG_PROGRAMS)){
                sLytSubHeader.setVisibility(View.VISIBLE);
            } else {
                sLytSubHeader.setVisibility(View.GONE);
            }

            sList.setAdapter(mAdapterWorkouts);

        }
    }

    private void getWorkoutListDataFromDatabase() {
        ArrayList<ArrayList<Object>> data;

        if(mParentPage.equals(Utils.ARG_WORKOUTS)){
            data = mDbHelperWorkouts.getAllWorkoutsByCategory(mSelectedId);

            for (int i = 0; i < data.size(); i++) {
                ArrayList<Object> row = data.get(i);

                mWorkoutIds.add(row.get(0).toString());
                mWorkoutNames.add(row.get(1).toString());
                mWorkoutImages.add(row.get(2).toString());
                mWorkoutTimes.add(row.get(3).toString());
                mWorkoutSteps.add(row.get(4).toString());
            }
        } else {
            data = mDbHelperPrograms.getAllWorkoutsByDay(mSelectedId);
            for (int i = 0; i < data.size(); i++) {
                ArrayList<Object> row = data.get(i);

                mProgramIds.add(row.get(0).toString());
                mWorkoutIds.add(row.get(1).toString());
                mWorkoutNames.add(row.get(2).toString());
                mWorkoutImages.add(row.get(3).toString());
                mWorkoutTimes.add(row.get(4).toString());
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

}
