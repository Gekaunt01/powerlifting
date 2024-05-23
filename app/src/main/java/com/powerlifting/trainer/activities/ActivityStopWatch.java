package com.powerlifting.trainer.activities;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.powerlifting.trainer.R;
import com.powerlifting.trainer.adapters.AdapterPagerWorkout;
import com.powerlifting.trainer.utils.DBHelperWorkouts;
import com.powerlifting.trainer.utils.Utils;
import com.powerlifting.trainer.views.ViewWorkout;
import com.trening.timer.circularbar.library.CircularBarPager;
import com.viewpagerindicator.CirclePageIndicator;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActivityStopWatch extends AppCompatActivity implements View.OnClickListener {

    private static final int INTERVAL = 1000;

    private Toolbar mToolbar;
    private FloatingActionButton mFabPlay;
    private CircleProgressBar mPrgLoading;
    private CircularBarPager mCircularBarPager;
    private LinearLayout mLytContainerLayout;
    private TextView mTxtTitle, mTxtSubTitle, mTxtBreakTime;
    private CirclePageIndicator mCirclePageIndicator;
    private FrameLayout mLytBreakLayout;

    private AudioManager mAudioManager;
    private Counter mCounter;
    private MediaPlayer mMediaPlayer;

    private DBHelperWorkouts mDbHelperWorkouts;

    private ArrayList<String> mWorkoutIds = new ArrayList<>();
    private ArrayList<String> mWorkoutNames = new ArrayList<>();
    private ArrayList<String> mWorkoutImages = new ArrayList<>();
    private ArrayList<String> mWorkoutTimes = new ArrayList<>();
    private Map<String, ArrayList<String>> mWorkoutGalleries = new HashMap<>();

    private String mProgramName;

    private boolean mIsPlay = false;
    private boolean mIsPause = false;
    private boolean mIsBreak = true;
    private boolean mIsFirstAppRun = true;

    private String mCurrentTime = "00:00";
    private int mCurrentWorkout = 0;
    private int mCurrentData = 0;

    private int mStart = 0;
    private int mEnd = 0;
    private int mStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        Intent iGet     = getIntent();
        mWorkoutIds     = iGet.getStringArrayListExtra(Utils.ARG_WORKOUT_IDS);
        mWorkoutNames   = iGet.getStringArrayListExtra(Utils.ARG_WORKOUT_NAMES);
        mWorkoutImages   = iGet.getStringArrayListExtra(Utils.ARG_WORKOUT_IMAGES);
        mWorkoutTimes   = iGet.getStringArrayListExtra(Utils.ARG_WORKOUT_TIMES);
        mProgramName    = iGet.getStringExtra(Utils.ARG_WORKOUT_NAME);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFabPlay = (FloatingActionButton) findViewById(R.id.fabPlay);
        mPrgLoading = (CircleProgressBar) findViewById(R.id.prgLoading);
        mCircularBarPager = (CircularBarPager) findViewById(R.id.circularBarPager);
        mLytContainerLayout = (LinearLayout) findViewById(R.id.lytContainerLayout);
        mTxtTitle           = (TextView) findViewById(R.id.txtTitle);
        mTxtSubTitle        = (TextView) findViewById(R.id.txtSubTitle);
        mTxtBreakTime       = (TextView) findViewById(R.id.txtBreakTime);
        mLytBreakLayout     = (FrameLayout) findViewById(R.id.lytBreakLayout);


        mTxtTitle.setText(getString(R.string.get_started));
        mTxtSubTitle.setText(getString(R.string.initial_time));
        mTxtBreakTime.setText(Utils.ARG_DEFAULT_START);

        mFabPlay.setOnClickListener(this);

        mCirclePageIndicator = mCircularBarPager.getCirclePageIndicator();
        mCirclePageIndicator.setVisibility(View.GONE);
        mCirclePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (mIsFirstAppRun) {
                    mTxtTitle.setText(getString(R.string.get_started));
                    mTxtSubTitle.setText(getString(R.string.initial_time));
                } else {
                    mTxtTitle.setText("(" + (position + 1) + "/" +
                            mWorkoutIds.size() + ") " +
                            mWorkoutNames.get(position));
                    mTxtSubTitle.setText(mWorkoutTimes.get(position));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Utils.ARG_SOUND_VOLUME, 0);

        mMediaPlayer = MediaPlayer.create(getApplicationContext(),
                R.raw.countdown);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        switch (mAudioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                mMediaPlayer.setVolume(0, 0);
                break;
        }

        mPrgLoading.setColorSchemeResources(R.color.accent_color);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(0,
                ContextCompat.getColor(this, R.color.primary_color)));

        mCurrentData += 1;

        new AsyncGetWorkoutGalleryImages().execute();

        mDbHelperWorkouts = new DBHelperWorkouts(getApplicationContext());

        try {
            mDbHelperWorkouts.createDataBase();
        }catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        mDbHelperWorkouts.openDataBase();

    }

    private class AsyncGetWorkoutGalleryImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mLytContainerLayout.setVisibility(View.GONE);
            mToolbar.setVisibility(View.GONE);
            mPrgLoading.setVisibility(View.VISIBLE);
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
        protected void onPostExecute(Void result) {
            startViewPagerThread();

        }
    }
    private void startViewPagerThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                View[] viewFlippers = new View[mWorkoutIds.size()];
                while (i < mWorkoutIds.size()) {
                    viewFlippers[i] = new ViewWorkout(ActivityStopWatch.this,
                            mWorkoutGalleries.get(mWorkoutIds.get(i)));
                    i++;
                }
                mCircularBarPager.setViewPagerAdapter(new AdapterPagerWorkout(
                        viewFlippers));
                mLytContainerLayout.setVisibility(View.VISIBLE);
                mToolbar.setVisibility(View.VISIBLE);
                mPrgLoading.setVisibility(View.GONE);
            }
        });

    }
    private void getWorkoutGalleryImagesFromDatabase(){
        ArrayList<ArrayList<Object>> data;

        for(int i = 0; i < mWorkoutIds.size(); i++) {
            data = mDbHelperWorkouts.getImages(mWorkoutIds.get(i));

            ArrayList<String> gallery = new ArrayList<String>();


            if(data.size() > 0) {

                for (int j = 0; j < data.size(); j++) {
                    ArrayList<Object> row = data.get(j);
                    gallery.add(row.get(0).toString());
                }
            }else{
                gallery.add(mWorkoutImages.get(i));
            }
            mWorkoutGalleries.put(mWorkoutIds.get(i), gallery);
        }
    }
    private void startTimer(String time){

        String[] splitTime = time.split(":");

        int splitMinute = Integer.valueOf(splitTime[0]);
        int splitSecond = Integer.valueOf(splitTime[1]);

        Long mMilisSecond = (long) (((splitMinute * 60) + splitSecond) * 1000);

        int max = (((splitMinute * 60) + splitSecond) * 1000);
        mCircularBarPager.getCircularBar().setMax(max);
        mStep = (int) ((max * INTERVAL) / mMilisSecond);
        mCounter = new Counter(mMilisSecond, INTERVAL);

        mStart = mEnd;
        mEnd = mEnd + mStep;
        mCounter.start();
    }
    public class Counter extends CountDownTimer {

        private long mAlert=4000;
        private int paramAlert=1;
        private String mTimer;
        boolean isRunning = false;

        public Counter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);


            mTimer = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millisInFuture),
                    TimeUnit.MILLISECONDS.toSeconds(millisInFuture) -
                            TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisInFuture)));


            if(mIsFirstAppRun){
                mTxtBreakTime.setText(mTimer);
            }else {
                if (mIsBreak) {
                    mTxtBreakTime.setText(mTimer);
                    mTxtSubTitle.setText(getResources().getString(R.string.initial_time));
                } else {
                    mTxtSubTitle.setText(mTimer);
                }
            }
        }
        @Override
        public void onFinish() {
            mStart = 0;
            mEnd = 0;
            isRunning = false;

            mTxtBreakTime.setText(getResources().getString(R.string.initial_time));
            mTxtSubTitle.setText(getResources().getString(R.string.initial_time));

            if(mIsFirstAppRun){
                startTimer(mWorkoutTimes.get(mCurrentData - 1));
                mTxtTitle.setText("("+(mCurrentWorkout + 1) + "/" +
                        mWorkoutNames.size() + ") " + mWorkoutNames.get(mCurrentData - 1));
                mLytBreakLayout.setVisibility(View.GONE);
                mIsFirstAppRun = false;
            }else {
                if (mCurrentData != (mWorkoutIds.size())) {
                    if (mIsBreak) {
                        takeABreak();
                    } else {
                        mLytBreakLayout.setVisibility(View.GONE);
                        getNextWorkout();
                    }
                } else {
                    new MaterialDialog.Builder(ActivityStopWatch.this)
                            .title(R.string.workout_completed)
                            .content(R.string.all_workouts_completed)
                            .positiveText(R.string.done)
                            .positiveColorRes(R.color.primary_color)
                            .negativeColorRes(R.color.primary_color)
                            .contentColorRes(R.color.text_and_icon_color)
                            .backgroundColorRes(R.color.material_background_color)
                            .cancelable(false)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {

                                    getWindow().clearFlags(
                                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                    finish();
                                }
                                @Override
                    public void onNegative(MaterialDialog dialog) {

                    }
                })
                            .show();

                }
            }
        }
        @Override
        public void onTick(long millisUntilFinished) {

            mCirclePageIndicator.setCurrentItem(mCurrentWorkout);

            isRunning = true;

            mStart = mEnd;
            mEnd = mEnd + mStep;

            mCircularBarPager.getCircularBar().animateProgress(mStart, mEnd, INTERVAL);


            mTimer = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

            if(mIsFirstAppRun){
                mTxtBreakTime.setText(mTimer);
            }else{
                if(mIsBreak){
                    mTxtSubTitle.setText(mTimer);
                } else {
                    mTxtBreakTime.setText(mTimer);
                    mTxtSubTitle.setText(getResources().getString(R.string.initial_time));
                }
            }
            if (millisUntilFinished < mAlert && paramAlert==1){

                mMediaPlayer.start();

                if(mIsFirstAppRun){

                }

                if(!mIsBreak) {
                    if(mCurrentData == (mWorkoutIds.size() - 1)) {
                                mWorkoutNames.get(mCurrentData);
                    }else{
                                mWorkoutNames.get(mCurrentData);
                    }
                }

                paramAlert+=1;
            }
        }


        public String timerPause(){
            return mTimer;
        }


        public Boolean timerCheck(){
            return isRunning;
        }

    }
    private void getNextWorkout(){
        mCurrentWorkout += 1;
        mCurrentData += 1;

        if (mCurrentData > 1) {
            mTxtTitle.setText("("+mCurrentWorkout + "/" + mWorkoutNames.size() + ") " +
                    mWorkoutNames.get(mCurrentData - 1));
            mIsPlay = true;
            mIsBreak = true;

            startTimer((mWorkoutTimes.get(mCurrentData - 1)));
        }

        mCirclePageIndicator.setCurrentItem(mCurrentData - 1);
    }


    private void takeABreak(){
        mIsBreak = false;
        mLytBreakLayout.setVisibility(View.VISIBLE);
        startTimer(Utils.ARG_DEFAULT_BREAK);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mCounter != null) {
            if (mCounter.timerCheck()) mCounter.cancel();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public void onStop() {
        super.onStop();

        if(mCounter != null) {
            if (mCounter.timerCheck()) mCounter.cancel();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onPause() {
        super.onPause();

        pauseWorkout();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabPlay:

                if(!mIsPlay){
                    playWorkouts();
                } else {
                    pauseWorkout();
                }
            default:
                break;
        }
    }

    public void playWorkouts(){
        mFabPlay.setIcon(R.mipmap.ic_pause_white_24dp);
        mIsPlay = true;
        mCirclePageIndicator.setCurrentItem(mCurrentWorkout);

        if(mIsPause) {

            mCounter.cancel();
            startTimer(mCurrentTime);
        } else {

            mLytBreakLayout.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            } else {

            }
            startTimer(Utils.ARG_DEFAULT_START);
        }
    }

    public void pauseWorkout(){
        mStart = 0;
        mEnd = 0;
        mIsPlay = false;
        mIsPause = true;
        mFabPlay.setIcon(R.mipmap.ic_play_arrow_white_24dp);
        if(mCounter != null) {
            if (mCounter.timerCheck()) mCounter.cancel();
            mCurrentTime = mCounter.timerPause();
        }
        if(mIsFirstAppRun){
            mTxtBreakTime.setText(mCurrentTime);
        }else {
            if (mIsBreak) {

                mTxtBreakTime.setText(mCurrentTime);
                mTxtSubTitle.setText(mCurrentTime);
            } else {
                mTxtSubTitle.setText(mCurrentTime);
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }
}
