package com.powerlifting.trainer.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.powerlifting.trainer.activities.ActivityWorkouts;
import com.github.mrengineer13.snackbar.SnackBar;
import com.makeramen.roundedimageview.RoundedImageView;
import com.powerlifting.trainer.R;
import com.powerlifting.trainer.listeners.OnTapListener;
import com.powerlifting.trainer.utils.DBHelperPrograms;
import com.powerlifting.trainer.utils.ImageLoader;
import com.powerlifting.trainer.utils.Utils;

import java.util.ArrayList;

public class AdapterWorkouts extends RecyclerView.Adapter<AdapterWorkouts.ViewHolder>
{
    private final ArrayList<String> mProgramIds;
    private final ArrayList<String> mWorkoutIds;
    private final ArrayList<String> mWorkoutNames;
    private final ArrayList<String> mWorkoutImages;
    private final ArrayList<String> mWorkoutTimes;
    private final ArrayList<String> mWorkoutSteps;
    private OnTapListener onTapListener;
    private Activity mActivity;
    private DBHelperPrograms mDbHelperPrograms;
    private ImageLoader mImageLoader;
    private String[] mDays;
    private String mSelectedDay;
    private static String sParentPage;

    public AdapterWorkouts(Activity activity, String selectedDay, String parentPage,
                           DBHelperPrograms dbHelperPrograms) {

        this.mProgramIds    = new ArrayList<>();
        this.mWorkoutIds    = new ArrayList<>();
        this.mWorkoutNames  = new ArrayList<>();
        this.mWorkoutImages = new ArrayList<>();
        this.mWorkoutTimes  = new ArrayList<>();
        this.mWorkoutSteps  = new ArrayList<>();

        mActivity = activity;

        sParentPage = parentPage;

        mDbHelperPrograms = dbHelperPrograms;
        mSelectedDay = selectedDay;

        mDays = mActivity.getResources().getStringArray(R.array.day_names);

        int mImageWidth = mActivity.getResources().getDimensionPixelSize(R.dimen.thumb_width);
        int mImageHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.thumb_height);

        mImageLoader = new ImageLoader(mActivity, mImageWidth, mImageHeight);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_list, null);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position)
    {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (onTapListener != null)
                    onTapListener.onTapView(mWorkoutIds.get(position),mWorkoutNames.get(position));
            }
        });

        viewHolder.mBtnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sParentPage.equals(Utils.ARG_WORKOUTS)){
                    showDayListDialog(position);
                }else if(sParentPage.equals(Utils.ARG_PROGRAMS)){
                    showAlertDialog(position, mSelectedDay);
                }
            }
        });

        viewHolder.mTxtTitle.setText(mWorkoutNames.get(position));
        viewHolder.mTxtTime.setText(mWorkoutTimes.get(position));

        int image = mActivity.getResources().getIdentifier(mWorkoutImages.get(position),
                "drawable", mActivity.getPackageName());

        mImageLoader.loadBitmap(image, viewHolder.mImgThumbnail);
    }

    public void showAlertDialog(int i, String selectedDay){
        final int position = i;
        final String day = mDays[(Integer.valueOf(selectedDay) - 1)];
        String confirmMessage = mActivity.getString(R.string.confirm_message)+" "+
                day+" "+mActivity.getString(R.string.program) + "?";
        new MaterialDialog.Builder(mActivity)
                .title(R.string.confirm)
                .content(confirmMessage)
                .positiveText(R.string.remove)
                .negativeText(R.string.cancel)
                .cancelable(false)
                .positiveColorRes(R.color.text_and_icon_color)
                .negativeColorRes(R.color.text_and_icon_color)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        boolean result = mDbHelperPrograms.deleteWorkoutFromDay(
                                mProgramIds.get(position));
                        if (result) {
                            removeAt(position);
                            if (mProgramIds.size() == 0) {
                                updateViews();
                            }
                            notifyDataSetChanged();
                            new SnackBar.Builder(mActivity)
                                    .withMessage(mActivity.getString(R.string.success_remove) + " " +
                                            day + " " + mActivity.getString(R.string.program))
                                    .show();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    public void showDayListDialog(int i) {
        final int position = i;
        new MaterialDialog.Builder(mActivity)
                .title(R.string.days)
                .items(R.array.day_names)
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .cancelable(false)
                .positiveColorRes(R.color.text_and_icon_color)
                .negativeColorRes(R.color.text_and_icon_color)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int selectedIndex, CharSequence text) {
                                if (mDbHelperPrograms.isDataAvailable((selectedIndex + 1),
                                        mWorkoutIds.get(position))) {
                                    showSnackbar(
                                            mActivity.getString(R.string.workout_already_added) +
                                                    " " + mDays[selectedIndex] + " " +
                                                    mActivity.getString(R.string.program) + ".");
                                } else {
                                    mDbHelperPrograms.addData(
                                            Integer.valueOf(mWorkoutIds.get(position)),
                                            mWorkoutNames.get(position),
                                            (selectedIndex + 1),
                                            mWorkoutImages.get(position),
                                            mWorkoutTimes.get(position),
                                            mWorkoutSteps.get(position));
                                    showSnackbar(
                                            mActivity.getString(R.string.workout_successfully_added)
                                                    + " " + mDays[selectedIndex] + " " +
                                                    mActivity.getString(R.string.program) + ".");
                                }
                                return true;
                            }
                        }
                )
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .show();
    }
    public void showSnackbar(String message){
        new SnackBar.Builder(mActivity)
                .withMessage(message)
                .show();
    }
    @Override
    public int getItemCount()
    {
        return mWorkoutIds.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView mImgThumbnail;
        private TextView mTxtTitle, mTxtTime;
        private RelativeLayout mBtnAction;
        public ViewHolder(View v)
        {
            super(v);
            mImgThumbnail= (RoundedImageView) v.findViewById(R.id.imgThumbnail);
            mTxtTitle    = (TextView) v.findViewById(R.id.txtPrimaryText);
            mTxtTime     = (TextView) v.findViewById(R.id.txtSecondaryText);
            mBtnAction   = (RelativeLayout) v.findViewById(R.id.btnAction);
            mBtnAction.setVisibility(View.VISIBLE);
        }
    }
    public void updateList(
            ArrayList<String> mProgramIds,
            ArrayList<String> mWorkoutIds,
            ArrayList<String> workoutNames,
            ArrayList<String> workoutImages,
            ArrayList<String> workoutTimes,
            ArrayList<String> workoutSteps)
    {
        this.mProgramIds.clear();
        this.mProgramIds.addAll(mProgramIds);

        this.mWorkoutIds.clear();
        this.mWorkoutIds.addAll(mWorkoutIds);

        this.mWorkoutNames.clear();
        this.mWorkoutNames.addAll(workoutNames);

        this.mWorkoutTimes.clear();
        this.mWorkoutTimes.addAll(workoutTimes);

        this.mWorkoutImages.clear();
        this.mWorkoutImages.addAll(workoutImages);

        this.mWorkoutSteps.clear();
        this.mWorkoutSteps.addAll(workoutSteps);

        this.notifyDataSetChanged();
    }
    public void removeAt(int position) {

        this.mProgramIds.remove(position);

        this.mWorkoutIds.remove(position);

        this.mWorkoutNames.remove(position);

        this.mWorkoutTimes.remove(position);

        this.mWorkoutImages.remove(position);
    }
    public ArrayList<String> getData(int i){
        switch(i){
            case 0:
                return mWorkoutIds;
            case 1:
                return mWorkoutNames;
            case 2:
                return mWorkoutImages;
            case 3:
                return mWorkoutTimes;
        }
        return null;
    }
    public void updateViews(){
        ActivityWorkouts.sList.setVisibility(View.GONE);
        ActivityWorkouts.sLytSubHeader.setVisibility(View.GONE);
        ActivityWorkouts.sTxtAlert.setVisibility(View.VISIBLE);
    }
    public void setOnTapListener(OnTapListener onTapListener)
    {
        this.onTapListener = onTapListener;
    }
}