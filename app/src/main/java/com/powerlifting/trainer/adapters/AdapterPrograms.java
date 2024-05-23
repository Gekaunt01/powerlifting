package com.powerlifting.trainer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.powerlifting.trainer.listeners.OnTapListener;
import com.powerlifting.trainer.R;

import java.util.ArrayList;

public class AdapterPrograms extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private final ArrayList<String> mDayIds;
    private final ArrayList<String> mDayNames;
    private final ArrayList<String> mTotalPrograms;

    private OnTapListener onTapListener;

    private Context mContext;

    private View mHeaderView;
    private LayoutInflater mInflater;

    public AdapterPrograms(Context context, View headerView) {
        this.mDayIds = new ArrayList<>();
        this.mDayNames = new ArrayList<>();
        this.mTotalPrograms = new ArrayList<>();

        mContext = context;

        mHeaderView = headerView;

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        if (mHeaderView == null) {
            return mDayIds.size();
        } else {
            return mDayIds.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(mHeaderView);
        } else {
            return new ItemViewHolder(mInflater.inflate(R.layout.adapter_programs, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position)
    {
        if(viewHolder instanceof ItemViewHolder){
            ((ItemViewHolder) viewHolder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onTapListener != null)
                        onTapListener.onTapView(mDayIds.get(position - 1),
                                mDayNames.get(position - 1));
                }
            });

            ((ItemViewHolder) viewHolder).mTxtDayName.setText(mDayNames.get(position - 1));

            int count = Integer.parseInt(mTotalPrograms.get(position - 1));

            if(count > 1){
                ((ItemViewHolder) viewHolder).mTxtWorkoutNumber.setText(count+" "+mContext.getResources().getString(R.string.workouts));
            }else{
                ((ItemViewHolder) viewHolder).mTxtWorkoutNumber.setText(count+" "+mContext.getResources().getString(R.string.workout));
            }
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View view) {
            super(view);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mTxtDayName, mTxtWorkoutNumber;

        public ItemViewHolder(View view) {
            super(view);
            mTxtDayName          = (TextView) view.findViewById(R.id.txtDayName);
            mTxtWorkoutNumber    = (TextView) view.findViewById(R.id.txtWorkoutNumber);
        }
    }

    public void updateList(
            ArrayList<String> dayIds,
            ArrayList<String> dayNames,
            ArrayList<String> totalPrograms)
    {
        this.mDayIds.clear();
        this.mDayIds.addAll(dayIds);

        this.mDayNames.clear();
        this.mDayNames.addAll(dayNames);

        this.mTotalPrograms.clear();
        this.mTotalPrograms.addAll(totalPrograms);

        this.notifyDataSetChanged();
    }

    public void setOnTapListener(OnTapListener onTapListener)
    {
        this.onTapListener = onTapListener;
    }
}