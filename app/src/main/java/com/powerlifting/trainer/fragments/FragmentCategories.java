package com.powerlifting.trainer.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.powerlifting.trainer.R;
import com.powerlifting.trainer.adapters.AdapterCategories;
import com.powerlifting.trainer.listeners.OnTapListener;
import com.powerlifting.trainer.utils.DBHelperWorkouts;

import java.io.IOException;
import java.util.ArrayList;

public class FragmentCategories extends Fragment {
    private OnSelectedCategoryListener mCallback;
    private RecyclerView mList;
    private CircleProgressBar mPrgLoading;
    private AdapterCategories mAdapterCategories;
    private DBHelperWorkouts mDbHelperWorkouts;
    private ArrayList<ArrayList<Object>> data;
    private ArrayList<String> mCategoryIds = new ArrayList<>();
    private ArrayList<String> mCategoryNames = new ArrayList<>();
    private ArrayList<String> mCategoryImages = new ArrayList<>();
    private ArrayList<String> mTotalWorkouts = new ArrayList<>();

    public interface OnSelectedCategoryListener {
        void onSelectedCategory(String selectedID, String selectedName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_list, container, false);

        mPrgLoading     = (CircleProgressBar) v.findViewById(R.id.prgLoading);
        mList           = (RecyclerView) v.findViewById(R.id.list);

        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.setHasFixedSize(false);

        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.padding, null);


        mPrgLoading.setColorSchemeResources(R.color.accent_color);


        mDbHelperWorkouts = new DBHelperWorkouts(getActivity());

        try {
            mDbHelperWorkouts.createDataBase();
        }catch(IOException ioe){
            throw new Error("Unable to create database");
        }

        mDbHelperWorkouts.openDataBase();

        new AsyncGetWorkoutCategories().execute();

        mAdapterCategories = new AdapterCategories(getActivity(), headerView);

        mAdapterCategories.setOnTapListener(new OnTapListener() {
            @Override
            public void onTapView(String id, String name) {
                mCallback.onSelectedCategory(id, name);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnSelectedCategoryListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSelectedCategoryListener");
        }
    }

    private class AsyncGetWorkoutCategories extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPrgLoading.setVisibility(View.VISIBLE);
            mList.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getWorkoutCategoryFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mPrgLoading.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);

            mAdapterCategories.updateList(mCategoryIds, mCategoryNames,
                    mCategoryImages, mTotalWorkouts);
            if(mCategoryIds.size() != 0) {
                mList.setAdapter(mAdapterCategories);
            }

        }
    }

    public void getWorkoutCategoryFromDatabase() {
        data = mDbHelperWorkouts.getAllCategories();

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);

            mCategoryIds.add(row.get(0).toString());
            mCategoryNames.add(row.get(1).toString());
            mCategoryImages.add(row.get(2).toString());
            mTotalWorkouts.add(row.get(3).toString());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbHelperWorkouts.close();
    }
}