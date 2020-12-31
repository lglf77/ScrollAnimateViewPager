package com.lglf77.library.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SameHeightLinearLayout extends LinearLayout {

    public SameHeightLinearLayout(final Context context) {
        this(context, null);
    }

    public SameHeightLinearLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public SameHeightLinearLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        this(context, attrs);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int parentHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int parentWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        final int childHeightSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY);
        // final int childWidthSpec = MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.getMode(widthMeasureSpec));
        int childWidthSpec;

        int measuredHeight = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() != GONE) {
                measureChild(view, widthMeasureSpec, childHeightSpec);
                childWidthSpec = getChildMeasureSpec(MeasureSpec.AT_MOST, 0, view.getMeasuredWidth());
                view.measure(childWidthSpec, childHeightSpec);
                measuredHeight += parentHeight;
            }
        }
        setMeasuredDimension(parentWidth, measuredHeight);
    }
}
