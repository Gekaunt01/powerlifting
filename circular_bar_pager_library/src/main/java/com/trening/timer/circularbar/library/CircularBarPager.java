package com.trening.timer.circularbar.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.trening.timer.circularbar.library.view.FadeViewPagerTransformer;
import com.nineoldandroids.animation.Animator;
import com.viewpagerindicator.CirclePageIndicator;

import unt.evgenii.maksimovich.project.R;


public class CircularBarPager extends RelativeLayout {

    private Context mContext;

    private CircularBar mCircularBar;

    private ViewPager mViewPager;

    private CirclePageIndicator mCirclePageIndicator;

    private int mPaddingRatio = 12;

    private boolean isPaddingSet;

    public CircularBarPager(Context context) {
        this(context, null);
    }

    public CircularBarPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularBarPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        initializeView(attrs, defStyleAttr);
    }

    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            final TypedArray attributes = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularViewPager,
                    defStyleAttr, 0);

            boolean enableOnClick = attributes.getBoolean(R.styleable.CircularViewPager_progress_pager_on_click_enabled, false);
            isPaddingSet = false;

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.circularbar_view_pager, this);

            mCircularBar = (CircularBar) view.findViewById(R.id.circular_bar);
            mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
            mCirclePageIndicator = (CirclePageIndicator) view.findViewById(R.id.circle_page_indicator);


            if(mCircularBar != null){
                mCircularBar.loadStyledAttributes(attrs, defStyleAttr);
            }
            if(mViewPager != null){
                mViewPager.setPageTransformer(false, new FadeViewPagerTransformer());
            }


            if (enableOnClick) {
                final GestureDetectorCompat tapGestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        mViewPager.setCurrentItem((mViewPager.getCurrentItem() + 1) % mViewPager.getAdapter().getCount());
                        return super.onSingleTapConfirmed(e);
                    }
                });
                if(mViewPager != null){
                    mViewPager.setOnTouchListener(new OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            tapGestureDetector.onTouchEvent(event);
                            return false;
                        }
                    });
                }

            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!isPaddingSet && mViewPager != null) {
            int paddingForViewPager = this.getMeasuredWidth() / mPaddingRatio;
            mViewPager.setPadding(paddingForViewPager, mViewPager.getPaddingTop(), paddingForViewPager, mViewPager.getPaddingBottom());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mViewPager.setPageMargin(-(int) (((float) mViewPager.getPaddingLeft() + (float) mViewPager.getPaddingRight()) * 2.0f));
            }

            isPaddingSet = true;
        }
    }

    public void setViewPagerAdapter(PagerAdapter pagerAdapter) {
        mViewPager.setAdapter(pagerAdapter);
        mCirclePageIndicator.setViewPager(mViewPager);
    }


    public void addListener(Animator.AnimatorListener listener) {
        mCircularBar.addListener(listener);
    }

    public boolean removeListener(Animator.AnimatorListener listener) {
        return mCircularBar.removeListener(listener);
    }

    public void removeAllListeners() {
        mCircularBar.removeAllListeners();
    }

    public CircularBar getCircularBar() {
        return mCircularBar;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public CirclePageIndicator getCirclePageIndicator() {
        return mCirclePageIndicator;
    }

    public void setPaddingRatio(int paddingRatio) {
        this.mPaddingRatio = paddingRatio;
        isPaddingSet = false;
    }
}
