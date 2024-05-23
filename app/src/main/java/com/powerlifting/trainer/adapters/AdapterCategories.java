package com.powerlifting.trainer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.powerlifting.trainer.R;
import com.powerlifting.trainer.listeners.OnTapListener;
import com.powerlifting.trainer.utils.ImageLoader;

import java.util.ArrayList;
public class AdapterCategories extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private final ArrayList<String> mCategoryIds;
    private final ArrayList<String> mCategoryNames;
    private final ArrayList<String> mCategoryImages;
    private final ArrayList<String> mTotalWorkouts;

    private OnTapListener onTapListener;

    private Context mContext;
    private ImageLoader mImageLoader;

    private View mHeaderView;
    private LayoutInflater mInflater;

    public AdapterCategories(Context context, View headerView)
    {
        this.mCategoryIds = new ArrayList<>();
        this.mCategoryNames = new ArrayList<>();
        this.mCategoryImages = new ArrayList<>();
        this.mTotalWorkouts = new ArrayList<>();

        mContext = context;

        mHeaderView = headerView;

        mInflater = LayoutInflater.from(context);

        int mImageWidth = mContext.getResources().getDimensionPixelSize(R.dimen.thumb_width);
        int mImageHeight = mContext.getResources().getDimensionPixelSize(R.dimen.thumb_height);

        mImageLoader = new ImageLoader(mContext, mImageWidth, mImageHeight);

    }

    @Override
    public int getItemCount() {
        if (mHeaderView == null) {
            return mCategoryIds.size();
        } else {
            return mCategoryIds.size() + 1;
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
            return new ItemViewHolder(mInflater.inflate(R.layout.adapter_list, parent, false));
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
                        onTapListener.onTapView(mCategoryIds.get(position - 1),
                                mCategoryNames.get(position - 1));
                }
            });

            ((ItemViewHolder) viewHolder).mTxtWorkoutName.setText(mCategoryNames.get(position - 1));

            int count = Integer.parseInt(mTotalWorkouts.get(position - 1));

            if(count > 1){
                ((ItemViewHolder) viewHolder).mTxtWorkoutNumber.setText(count+" "+mContext.getResources().getString(R.string.workouts));
            }else{
                ((ItemViewHolder) viewHolder).mTxtWorkoutNumber.setText(count+" "+mContext.getResources().getString(R.string.workout));
            }

            int image = mContext.getResources().getIdentifier(mCategoryImages.get(position - 1),
                    "drawable", mContext.getPackageName());

            mImageLoader.loadBitmap(image, ((ItemViewHolder) viewHolder).mImgCategoryImage);
        }


    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View view) {
            super(view);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private RoundedImageView mImgCategoryImage;
        private TextView mTxtWorkoutName, mTxtWorkoutNumber;

        public ItemViewHolder(View view) {
            super(view);
            mImgCategoryImage   = (RoundedImageView) view.findViewById(R.id.imgThumbnail);
            mTxtWorkoutName     = (TextView) view.findViewById(R.id.txtPrimaryText);
            mTxtWorkoutNumber   = (TextView) view.findViewById(R.id.txtSecondaryText);
        }
    }

    public void updateList(
            ArrayList<String> categoryIds,
            ArrayList<String> categoryNames,
            ArrayList<String> categoryImages,
            ArrayList<String> totalWorkouts) {

        this.mCategoryIds.clear();
        this.mCategoryIds.addAll(categoryIds);

        this.mCategoryNames.clear();
        this.mCategoryNames.addAll(categoryNames);

        this.mCategoryImages.clear();
        this.mCategoryImages.addAll(categoryImages);

        this.mTotalWorkouts.clear();
        this.mTotalWorkouts.addAll(totalWorkouts);

        this.notifyDataSetChanged();
    }

    public void setOnTapListener(OnTapListener onTapListener)
    {
        this.onTapListener = onTapListener;
    }


}