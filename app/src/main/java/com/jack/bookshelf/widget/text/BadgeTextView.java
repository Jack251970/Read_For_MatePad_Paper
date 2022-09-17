package com.jack.bookshelf.widget.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

import androidx.appcompat.widget.AppCompatTextView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.ScreenUtils;

/**
 * Badge View for Progress Showing
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class BadgeTextView extends AppCompatTextView {
    private boolean mHideOnNull = true;

    public BadgeTextView(Context context) {
        this(context, null);
    }

    public BadgeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public BadgeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!(getLayoutParams() instanceof LayoutParams)) { setLayoutParams(
                new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));}
        // 设置字体
        setTextColor(Color.BLACK);
        setTypeface(Typeface.DEFAULT_BOLD);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        // 设置边距
        setPadding(ScreenUtils.dpToPx(5), ScreenUtils.dpToPx(1), ScreenUtils.dpToPx(5), ScreenUtils.dpToPx(1));
        // 设置默认背景
        setBackground();
        // 设置对齐方式
        setGravity(Gravity.CENTER);
        // 设置其余参数
        setHideOnNull(true);
        setBadgeProgress("0%");
        setMinWidth(ScreenUtils.dpToPx(16));
        setMinHeight(ScreenUtils.dpToPx(16));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setBackground() {
        setBackground(getResources().getDrawable(R.drawable.shape_badge_view));
    }

    /**
     * @return Returns true if view is hidden on badge value 0 or null;
     */
    public boolean isHideOnNull() {
        return mHideOnNull;
    }

    /**
     * @param hideOnNull the hideOnNull to set
     */
    public void setHideOnNull(boolean hideOnNull) {
        mHideOnNull = hideOnNull;
        setText(getText());
    }

    /**
     * @see android.widget.TextView#setText(java.lang.CharSequence, android.widget.TextView.BufferType)
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        if (isHideOnNull() && TextUtils.isEmpty(text)) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
        super.setText(text, type);
    }

    public void setBadgeProgress(String progress) {setText(progress);}
}
