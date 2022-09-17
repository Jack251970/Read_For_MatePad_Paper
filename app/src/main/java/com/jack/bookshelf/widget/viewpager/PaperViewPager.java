package com.jack.bookshelf.widget.viewpager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Paper ViewPager
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
        // 取消滑动切换页面动作
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 取消滑动切换页面动作
        return false;
    }

    @Override
    public void setCurrentItem(int item) {
        // 取消平滑滑动动画
        super.setCurrentItem(item,false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        // 取消平滑滑动动画
        super.setCurrentItem(item,false);
    }
}
