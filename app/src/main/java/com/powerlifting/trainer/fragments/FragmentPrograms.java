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
import com.powerlifting.trainer.adapters.AdapterPrograms;
import com.powerlifting.trainer.listeners.OnTapListener;
import com.powerlifting.trainer.utils.DBHelperPrograms;

import java.io.IOException;
import java.util.ArrayList;
public class FragmentPrograms extends Fragment {

    private OnSelectedDayListener mCallback;
    private RecyclerView mList;
    private CircleProgressBar mPrgLoading;
    private AdapterPrograms mAdapterPrograms;
    private DBHelperPrograms mDbHelperPrograms;
    private ArrayList<ArrayList<Object>> data;
    private ArrayList<String> mDayIds = new ArrayList<>();
    private ArrayList<String> mDayNames = new ArrayList<>();
    private ArrayList<String> mTotalPrograms = new ArrayList<>();
    private boolean mIsFirstAppRun = true;

    public interface OnSelectedDayListener {
        void onSelectedDay(String selectedID, String selectedName);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_list, container, false);

        mPrgLoading = (CircleProgressBar) v.findViewById(R.id.prgLoading);
        mList = (RecyclerView) v.findViewById(R.id.list);

        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.setHasFixedSize(true);

        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.padding, null);


        mPrgLoading.setVisibility(View.GONE);

        mPrgLoading.setColorSchemeResources(R.color.accent_color);

        mDbHelperPrograms = new DBHelperPrograms(getActivity());

        try {
            mDbHelperPrograms.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        mDbHelperPrograms.openDataBase();

        mAdapterPrograms = new AdapterPrograms(getActivity(), headerView);

        new AsyncGetDays().execute();

        mAdapterPrograms.setOnTapListener(new OnTapListener() {

            @Override
            public void onTapView(String id, String name) {

                mCallback.onSelectedDay(id, name);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnSelectedDayListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSelectedDayListener");
        }
    }

    private class AsyncGetDays extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            clearData();
            mPrgLoading.setVisibility(View.VISIBLE);
            mList.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            getDaysFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mPrgLoading.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
            mAdapterPrograms.updateList(mDayIds, mDayNames, mTotalPrograms);
            mAdapterPrograms.notifyDataSetChanged();
            mIsFirstAppRun = false;
            if (mDayIds.size() != 0) {
                mList.setAdapter(mAdapterPrograms);
            }
        }
    }

    private void getDaysFromDatabase() {
        data = mDbHelperPrograms.getAllDays();

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);

            mDayIds.add(row.get(0).toString());
            mDayNames.add(row.get(1).toString());
            mTotalPrograms.add(row.get(2).toString());
        }
    }

    private void clearData() {
        mDayIds.clear();
        mDayNames.clear();
        mTotalPrograms.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbHelperPrograms.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsFirstAppRun) {
            clearData();
            getDaysFromDatabase();
            mAdapterPrograms.updateList(mDayIds, mDayNames, mTotalPrograms);
            mAdapterPrograms.notifyDataSetChanged();
        }
    }
}