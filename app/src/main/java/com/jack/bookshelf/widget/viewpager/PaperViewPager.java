package com.jack.bookshelf.widget.viewpager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * PaperViewPager Without SmoothScroll Animation and Scroll Function
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PaperViewPager extends androidx.viewpager.widget.ViewPager {

    public PaperViewPager(@NonNull Context context) {
        super(context);
    }

    public PaperViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {super(context, attrs);}

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item,false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item,false);
    }
}
