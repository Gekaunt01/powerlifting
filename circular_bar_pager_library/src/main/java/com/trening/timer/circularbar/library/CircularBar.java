package com.trening.timer.circularbar.library;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.daimajia.easing.Glider;
import com.daimajia.easing.Skill;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

import unt.evgenii.maksimovich.project.R;



public class CircularBar extends View implements Animator.AnimatorListener {

    private static final String TAG = "CircularBar";

    private Context mContext;

    public static final int DEFAULT_ARC_MAX = 100;

    private int mMax = DEFAULT_ARC_MAX;

    private float progress = 0;

    private int mClockwiseArcColor;

    private int mCounterClockwiseArcColor;

    private int mClockwiseOutlineArcColor;

    private int mCounterClockwiseOutlineArcColor;

    private int mCircleFillColor;

    private int mCircleFillMode;

    private float mClockwiseReachedArcWidth;

    private float mCounterClockwiseReachedArcWidth;

    private float mClockwiseOutlineArcWidth;

    private float mCounterClockwiseOutlineArcWidth;

    private Paint mReachedArcPaint;

    private Paint mClockwiseReachedArcPaint;

    private Paint mCounterClockwiseReachedArcPaint;

    private Paint mOutlineArcPaint;

    private Paint mClockwiseOutlineArcPaint;

    private Paint mCounterClockwiseOutlineArcPaint;

    private Paint mCircleFillPaint;

    private RectF mReachedArcRectF = new RectF(0, 0, 0, 0);

    private RectF mOutlineArcRectF = new RectF(0, 0, 0, 0);

    private RectF mFillCircleRectF = new RectF(0, 0, 0, 0);

    private float mDiameter;

    private boolean mStartLineEnabled;

    private boolean mDrawOutlineArc = true;

    private boolean mDrawReachedArc = true;

    private boolean mCircleFillEnabled = false;

    private ProgressSweep mProgressSweep;

    private String mSuffix = "%";

    private String mPrefix = "";

    private List<Animator.AnimatorListener> mListeners;

    public enum CircleFillMode {
        DEFAULT(0),
        PIE(1);

        private int value;

        CircleFillMode(int val) {
            this.value = val;
        }

        public final int getValue() {
            return this.value;
        }

        public static CircleFillMode getMode(int val) {
            switch (val) {
                case 1:
                    return PIE;
                case 0:
                default:
                    return DEFAULT;
            }
        }
    }

    private final int default_clockwise_reached_color = Color.parseColor("#00c853");
    private final int default_clockwise_outline_color = Color.parseColor("#00c853");
    private final int default_counter_clockwise_reached_color = Color.parseColor("#ffffff");
    private final int default_counter_clockwise_outline_color = Color.parseColor("#ffffff");
    private final int default_circle_fill_color = Color.parseColor("#00000000");
    private final int default_circle_fill_mode = CircleFillMode.DEFAULT.getValue();
    private final float default_reached_arc_width;
    private final float default_outline_arc_width;


    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_START_LINE_ENABLED = "progress_start_line_enabled";
    private static final String INSTANCE_CLOCKWISE_REACHED_BAR_HEIGHT = "clockwise_reached_bar_height";
    private static final String INSTANCE_CLOCKWISE_REACHED_BAR_COLOR = "clockwise_reached_bar_color";
    private static final String INSTANCE_CLOCKWISE_OUTLINE_BAR_HEIGHT = "clockwise_outline_bar_height";
    private static final String INSTANCE_CLOCKWISE_OUTLINE_BAR_COLOR = "clockwise_outline_bar_color";
    private static final String INSTANCE_COUNTER_CLOCKWISE_REACHED_BAR_HEIGHT = "counter_clockwise_reached_bar_height";
    private static final String INSTANCE_COUNTER_CLOCKWISE_REACHED_BAR_COLOR = "counter_clockwise_reached_bar_color";
    private static final String INSTANCE_COUNTER_CLOCKWISE_OUTLINE_BAR_HEIGHT = "counter_clockwise_outline_bar_height";
    private static final String INSTANCE_COUNTER_CLOCKWISE_OUTLINE_BAR_COLOR = "counter_clockwise_outline_bar_color";
    private static final String INSTANCE_CIRCLE_FILL_ENABLED = "progress_pager_fill_circle_enabled";
    private static final String INSTANCE_CIRCLE_FILL_COLOR = "progress_pager_fill_circle_color";
    private static final String INSTANCE_CIRCLE_FILL_MODE = "progress_pager_fill_mode";
    private static final String INSTANCE_MAX = "max";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_SUFFIX = "suffix";
    private static final String INSTANCE_PREFIX = "prefix";

    public CircularBar(Context context) {
        this(context, null);
    }

    public CircularBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;

        default_reached_arc_width = dp2px(5f);
        default_outline_arc_width = dp2px(1.0f);

        mListeners = new ArrayList<>();
        loadStyledAttributes(attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        calculateDrawRectF();

        if (mCircleFillEnabled) {
            switch (CircleFillMode.getMode(mCircleFillMode)) {
                case PIE:
                    canvas.drawArc(mFillCircleRectF, mProgressSweep.reachedStart, mProgressSweep.reachedSweep, true, mCircleFillPaint);
                    break;
                case DEFAULT:
                default:
                    canvas.drawArc(mOutlineArcRectF, ProgressSweep.START_12, 360f, true, mCircleFillPaint);
                    break;
            }
        }
        if (mDrawOutlineArc) {
            canvas.drawArc(mOutlineArcRectF, mProgressSweep.outlineStart, mProgressSweep.outlineSweep, false, mOutlineArcPaint);
        }

        if (mDrawReachedArc) {
            canvas.drawArc(mReachedArcRectF, mProgressSweep.reachedStart, mProgressSweep.reachedSweep, false, mReachedArcPaint);
            if (mStartLineEnabled) {
                canvas.drawLine(mReachedArcRectF.centerX(), mReachedArcRectF.top - mClockwiseReachedArcWidth / 2, mReachedArcRectF.centerX() + 1, mReachedArcRectF.top + mClockwiseReachedArcWidth * 1.5f, mOutlineArcPaint);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_START_LINE_ENABLED, isStartLineEnabled());
        bundle.putFloat(INSTANCE_CLOCKWISE_REACHED_BAR_HEIGHT, getClockwiseReachedArcWidth());
        bundle.putFloat(INSTANCE_CLOCKWISE_OUTLINE_BAR_HEIGHT, getClockwiseOutlineArcWidth());
        bundle.putInt(INSTANCE_CLOCKWISE_REACHED_BAR_COLOR, getClockwiseReachedArcColor());
        bundle.putInt(INSTANCE_CLOCKWISE_OUTLINE_BAR_COLOR, getClockwiseOutlineArcColor());
        bundle.putFloat(INSTANCE_COUNTER_CLOCKWISE_REACHED_BAR_HEIGHT, getCounterClockwiseReachedArcWidth());
        bundle.putFloat(INSTANCE_COUNTER_CLOCKWISE_OUTLINE_BAR_HEIGHT, getCounterClockwiseOutlineArcWidth());
        bundle.putInt(INSTANCE_COUNTER_CLOCKWISE_REACHED_BAR_COLOR, getCounterClockwiseReachedArcColor());
        bundle.putInt(INSTANCE_COUNTER_CLOCKWISE_OUTLINE_BAR_COLOR, getCounterClockwiseOutlineArcColor());
        bundle.putBoolean(INSTANCE_CIRCLE_FILL_ENABLED, isCircleFillEnabled());
        bundle.putInt(INSTANCE_CIRCLE_FILL_COLOR, getCircleFillColor());
        bundle.putInt(INSTANCE_MAX, getMax());
        bundle.putFloat(INSTANCE_PROGRESS, getProgress());
        bundle.putString(INSTANCE_SUFFIX, getSuffix());
        bundle.putString(INSTANCE_PREFIX, getPrefix());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mStartLineEnabled = bundle.getBoolean(INSTANCE_START_LINE_ENABLED);
            mClockwiseReachedArcWidth = bundle.getFloat(INSTANCE_CLOCKWISE_REACHED_BAR_HEIGHT);
            mClockwiseOutlineArcWidth = bundle.getFloat(INSTANCE_CLOCKWISE_OUTLINE_BAR_HEIGHT);
            mClockwiseArcColor = bundle.getInt(INSTANCE_CLOCKWISE_REACHED_BAR_COLOR);
            mClockwiseOutlineArcColor = bundle.getInt(INSTANCE_CLOCKWISE_OUTLINE_BAR_COLOR);
            mCounterClockwiseReachedArcWidth = bundle.getFloat(INSTANCE_COUNTER_CLOCKWISE_REACHED_BAR_HEIGHT);
            mCounterClockwiseOutlineArcWidth = bundle.getFloat(INSTANCE_COUNTER_CLOCKWISE_OUTLINE_BAR_HEIGHT);
            mCounterClockwiseArcColor = bundle.getInt(INSTANCE_COUNTER_CLOCKWISE_REACHED_BAR_COLOR);
            mCounterClockwiseOutlineArcColor = bundle.getInt(INSTANCE_COUNTER_CLOCKWISE_OUTLINE_BAR_COLOR);
            mCircleFillEnabled = bundle.getBoolean(INSTANCE_CIRCLE_FILL_ENABLED);
            mCircleFillColor = bundle.getInt(INSTANCE_CIRCLE_FILL_COLOR);
            mCircleFillMode = bundle.getInt(INSTANCE_CIRCLE_FILL_MODE);
            initializePainters();
            setMax(bundle.getInt(INSTANCE_MAX));
            setProgress(bundle.getFloat(INSTANCE_PROGRESS));
            setPrefix(bundle.getString(INSTANCE_PREFIX));
            setSuffix(bundle.getString(INSTANCE_SUFFIX));
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public void loadStyledAttributes(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            final TypedArray attributes = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularViewPager,
                    defStyleAttr, 0);

            mStartLineEnabled = attributes.getBoolean(R.styleable.CircularViewPager_progress_start_line_enabled, true);

            mClockwiseArcColor = attributes.getColor(R.styleable.CircularViewPager_progress_arc_clockwise_color, default_clockwise_reached_color);
            mCounterClockwiseArcColor = attributes.getColor(R.styleable.CircularViewPager_progress_arc_counter_clockwise_color, default_counter_clockwise_reached_color);
            mClockwiseOutlineArcColor = attributes.getColor(R.styleable.CircularViewPager_progress_arc_clockwise_outline_color, default_clockwise_outline_color);
            mCounterClockwiseOutlineArcColor = attributes.getColor(R.styleable.CircularViewPager_progress_arc_counter_clockwise_outline_color, default_counter_clockwise_outline_color);

            mClockwiseReachedArcWidth = attributes.getDimension(R.styleable.CircularViewPager_progress_arc_clockwise_width, default_reached_arc_width);
            mCounterClockwiseReachedArcWidth = attributes.getDimension(R.styleable.CircularViewPager_progress_arc_counter_clockwise_width, default_reached_arc_width);
            mClockwiseOutlineArcWidth = attributes.getDimension(R.styleable.CircularViewPager_progress_arc_clockwise_outline_width, default_outline_arc_width);
            mCounterClockwiseOutlineArcWidth = attributes.getDimension(R.styleable.CircularViewPager_progress_arc_counter_clockwise_outline_width, default_outline_arc_width);

            mCircleFillColor = attributes.getColor(R.styleable.CircularViewPager_progress_pager_fill_circle_color, default_circle_fill_color);
            mCircleFillMode = attributes.getInt(R.styleable.CircularViewPager_progress_pager_fill_mode, default_circle_fill_mode);
            cicleFillEnable(mCircleFillColor != default_circle_fill_color);

            setMax(attributes.getInt(R.styleable.CircularViewPager_progress_arc_max, 100));
            setProgress(attributes.getInt(R.styleable.CircularViewPager_arc_progress, 0));

            attributes.recycle();

            initializePainters();
        }
    }

    private int measure(int measureSpec, boolean isWidth) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            if (mode == MeasureSpec.AT_MOST) {
                if (isWidth) {
                    result = Math.max(result, size);
                } else {
                    result = Math.min(result, size);
                }
            }
        }
        return result;
    }

    private void calculateDrawRectF() {
        mFillCircleRectF = getArcRect(mClockwiseReachedArcWidth);
        mReachedArcRectF = getArcRect(mClockwiseReachedArcWidth / 2);
        mOutlineArcRectF = getArcRect(mClockwiseOutlineArcWidth / 2);
    }

    private RectF getArcRect(float offset) {
        RectF workingSurface = new RectF();
        workingSurface.left = getPaddingLeft() + offset;
        workingSurface.top = getPaddingTop() + offset;
        workingSurface.right = getWidth() - getPaddingRight() - offset;
        workingSurface.bottom = getHeight() - getPaddingBottom() - offset;

        float width = workingSurface.right - workingSurface.left;
        float height = workingSurface.bottom - workingSurface.top;

        this.mDiameter = Math.min(width, height);
        float radius = mDiameter / 2;
        float centerX = width / 2;
        float centerY = height / 2;

        return new RectF(centerX - radius + offset, centerY - radius + offset, centerX + radius + offset, centerY + radius + offset);
    }

    private void initializePainters() {
        mClockwiseReachedArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClockwiseReachedArcPaint.setColor(mClockwiseArcColor);
        mClockwiseReachedArcPaint.setAntiAlias(true);
        mClockwiseReachedArcPaint.setStrokeWidth(mClockwiseReachedArcWidth);
        mClockwiseReachedArcPaint.setStyle(Paint.Style.STROKE);

        mCounterClockwiseReachedArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCounterClockwiseReachedArcPaint.setColor(mCounterClockwiseArcColor);
        mCounterClockwiseReachedArcPaint.setAntiAlias(true);
        mCounterClockwiseReachedArcPaint.setStrokeWidth(mCounterClockwiseReachedArcWidth);
        mCounterClockwiseReachedArcPaint.setStyle(Paint.Style.STROKE);

        mClockwiseOutlineArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClockwiseOutlineArcPaint.setColor(mClockwiseOutlineArcColor);
        mClockwiseOutlineArcPaint.setAntiAlias(true);
        mClockwiseOutlineArcPaint.setStrokeWidth(mClockwiseOutlineArcWidth);
        mClockwiseOutlineArcPaint.setStyle(Paint.Style.STROKE);

        mCounterClockwiseOutlineArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCounterClockwiseOutlineArcPaint.setColor(mCounterClockwiseOutlineArcColor);
        mCounterClockwiseOutlineArcPaint.setAntiAlias(true);
        mCounterClockwiseOutlineArcPaint.setStrokeWidth(mCounterClockwiseOutlineArcWidth);
        mCounterClockwiseOutlineArcPaint.setStyle(Paint.Style.STROKE);

        mCircleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleFillPaint.setColor(mCircleFillColor);
        mCircleFillPaint.setAntiAlias(true);
        mCircleFillPaint.setStyle(Paint.Style.FILL);
        mReachedArcPaint = mClockwiseReachedArcPaint;
        mOutlineArcPaint = mClockwiseOutlineArcPaint;
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mProgressSweep.reachedSweep = Math.round(mProgressSweep.reachedSweep);
        mProgressSweep.outlineSweep = Math.round(mProgressSweep.outlineSweep);
        invalidate();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public void animateProgress(int start, int end, int duration) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(Glider.glide(Skill.QuadEaseInOut, duration, ObjectAnimator.ofFloat(this, "progress", start, end)));
        set.setDuration(duration);
        set = addListenersToSet(set);
        set.start();
    }

    protected AnimatorSet addListenersToSet(AnimatorSet set) {
        if (mListeners != null && set != null) {
            set.addListener(this);
            for (Animator.AnimatorListener listener : mListeners) {
                set.addListener(listener);
            }
        }
        return set;
    }

    public void addListener(Animator.AnimatorListener listener) {
        mListeners.add(listener);
    }

    public boolean removeListener(Animator.AnimatorListener listener) {
        return mListeners.remove(listener);
    }

    public void removeAllListeners() {
        mListeners = new ArrayList<>();
    }

    public String getSuffix() {
        return mSuffix;
    }

    public String getPrefix() {
        return mPrefix;
    }

    public boolean isStartLineEnabled() {
        return mStartLineEnabled;
    }

    public float getDiameter() {
        return mDiameter;
    }

    public int getClockwiseOutlineArcColor() {
        return mClockwiseOutlineArcColor;
    }

    public int getClockwiseReachedArcColor() {
        return mClockwiseArcColor;
    }

    public float getProgress() {
        return progress;
    }

    public int getMax() {
        return mMax;
    }

    public float getClockwiseReachedArcWidth() {
        return mClockwiseReachedArcWidth;
    }

    public float getClockwiseOutlineArcWidth() {
        return mClockwiseOutlineArcWidth;
    }

    public int getCounterClockwiseReachedArcColor() {
        return mCounterClockwiseArcColor;
    }

    public int getCounterClockwiseOutlineArcColor() {
        return mCounterClockwiseOutlineArcColor;
    }

    public int getCircleFillColor() {
        return mCircleFillColor;
    }

    public float getCounterClockwiseReachedArcWidth() {
        return mCounterClockwiseReachedArcWidth;
    }

    public float getCounterClockwiseOutlineArcWidth() {
        return mCounterClockwiseOutlineArcWidth;
    }

    public boolean isCircleFillEnabled() {
        return mCircleFillEnabled;
    }

    public void cicleFillEnable(boolean enable) {
        mCircleFillEnabled = enable;
    }

    public void setCounterClockwiseOutlineArcWidth(float width) {
        this.mCounterClockwiseOutlineArcWidth = width;
        invalidate();
    }

    public void setCounterClockwiseReachedArcWidth(float width) {
        this.mCounterClockwiseReachedArcWidth = width;
        invalidate();
    }

    public void setCounterClockwiseOutlineArcColor(int color) {
        this.mCounterClockwiseOutlineArcColor = color;
        initializePainters();
        invalidate();
    }

    public void setCircleFillColor(int color) {
        this.mCircleFillColor = color;
        cicleFillEnable(mCircleFillColor != default_circle_fill_color);
        initializePainters();
        invalidate();
    }
    public void setStartLineEnabled(boolean startLineEnabled) {
        this.mStartLineEnabled = startLineEnabled;
        invalidate();
    }

    public void setCounterClockwiseArcColor(int color) {
        this.mCounterClockwiseArcColor = color;
        initializePainters();
        invalidate();
    }

    public void setClockwiseReachedArcColor(int color) {
        this.mClockwiseArcColor = color;
        initializePainters();
        invalidate();
    }

    public void setClockwiseOutlineArcColor(int color) {
        this.mClockwiseOutlineArcColor = color;
        initializePainters();
        invalidate();
    }

    public void setClockwiseReachedArcWidth(float width) {
        mClockwiseReachedArcWidth = width;
        invalidate();
    }

    public void setClockwiseOutlineArcWidth(float width) {
        mClockwiseOutlineArcWidth = width;
        invalidate();
    }

    public void setMax(int max) {
        if (max > 0) {
            this.mMax = max;
            invalidate();
        }
    }

    public void setSuffix(String suffix) {
        if (suffix == null) {
            mSuffix = "";
        } else {
            mSuffix = suffix;
        }
    }

    public void setPrefix(String prefix) {
        if (prefix == null)
            mPrefix = "";
        else {
            mPrefix = prefix;
        }
    }

    public void setProgress(float newProgress) {
        if (mProgressSweep == null) {
            this.mProgressSweep = new ProgressSweep(newProgress);
        } else {
            mProgressSweep.enforceBounds(newProgress);
            mProgressSweep.updateAngles();
        }

        invalidate();
    }

    public float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public float sp2px(float sp) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    private class ProgressSweep {

        public static final float START_12 = 270f;

        public static final float START_3 = 0f;

        public static final float START_6 = 90f;

        public static final float START_9 = 180f;

        public float reachedStart = START_12;

        public float reachedSweep = 0f;

        public float outlineStart = reachedStart;

        public float outlineSweep = 360f;

        public ProgressSweep(float progress) {
            enforceBounds(progress);
            updateAngles();
        }

        public void enforceBounds(float newProgress) {
            if (Math.abs(newProgress) == Math.abs(mMax)) {
                return;
            }
            progress = newProgress % mMax;
        }

        public void updateAngles() {
            if (progress >= 0) {
                reachedStart = START_12;
                reachedSweep = progress / mMax * 360f;
                outlineStart = (START_12 + reachedSweep) % 360f;
                outlineSweep = 360f - reachedSweep;

                mReachedArcPaint = mClockwiseReachedArcPaint;
                mOutlineArcPaint = mClockwiseOutlineArcPaint;
            } else {
                reachedSweep = Math.abs(progress / mMax * 360f);
                reachedStart = START_12 - reachedSweep;
                outlineStart = START_12;
                outlineSweep = 360f - reachedSweep;

                mReachedArcPaint = mCounterClockwiseReachedArcPaint;
                mOutlineArcPaint = mCounterClockwiseOutlineArcPaint;
            }
        }
    }
}

