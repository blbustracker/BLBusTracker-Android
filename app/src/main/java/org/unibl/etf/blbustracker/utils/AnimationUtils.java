package org.unibl.etf.blbustracker.utils;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.unibl.etf.blbustracker.Constants;

/**
 * animation for collapsing/expanding RelativeLayout
 * SearchLayout in Mapfragment collaps/expands on map press
 */
public abstract class AnimationUtils
{
    private static boolean isExpanded = true;

    public static void expand(RelativeLayout collapsablelayout)
    {
        isExpanded = true;
        collapsablelayout.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        collapsablelayout.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(collapsablelayout, 0, collapsablelayout.getMeasuredHeight());
        mAnimator.start();
    }

    public static void collapse(RelativeLayout collapsablelayout)
    {
        isExpanded = false;
        int finalHeight = collapsablelayout.getHeight();

        ValueAnimator mAnimator = slideAnimator(collapsablelayout, finalHeight, 0);

        mAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationEnd(Animator animator)
            {
                //Height=0, but it set visibility to GONE
                collapsablelayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }

        });
        mAnimator.start();
    }

    private static ValueAnimator slideAnimator(RelativeLayout collapsablelayout, int start, int end)
    {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(valueAnimator ->
        {
            //Update Height
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = collapsablelayout.getLayoutParams();
            layoutParams.height = value;
            collapsablelayout.setLayoutParams(layoutParams);
        });
        animator.setDuration(Constants.LAYOUT_ANIMATION_DURATION);
        return animator;
    }

    public static boolean isIsExpanded()
    {
        return isExpanded;
    }
}
