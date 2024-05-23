package com.trening.timer.circularbar.library.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;

public class FadeViewPagerTransformer implements ViewPager.PageTransformer {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void transformPage(View view, float position) {
        position -= (float) ((ViewPager) view.getParent()).getPaddingRight() / (float) view.getWidth();
        if (position <= -1.0f || position >= 1.0f) {
            view.setAlpha(0);
            view.setTranslationX(0);
        } else if (position < 0.0001f && position > -0.0001f) {
            view.setAlpha(1);
            view.setTranslationX(1);
        } else if (position <= 0.0f || position <= 1.0f) {
            float pageMargin = -(float) ((ViewPager) view.getParent()).getPageMargin() / (float) view.getWidth();
            float alpha = position / (1.0f - pageMargin);
            alpha = (alpha <= 0) ? alpha + 1 : 1 - alpha;
            view.setAlpha(alpha);
            view.setTranslationX(-position * ((float) view.getWidth() / 1.5f));
        }
    }

}
