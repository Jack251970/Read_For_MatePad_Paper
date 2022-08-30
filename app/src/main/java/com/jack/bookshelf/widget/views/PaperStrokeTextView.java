package com.jack.bookshelf.widget.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.ScreenUtils;
import com.jack.bookshelf.utils.Selector;
import com.jack.bookshelf.utils.theme.ThemeStore;

/**
 * Paper Stroke TextView
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PaperStrokeTextView extends AppCompatTextView {
    public PaperStrokeTextView(Context context) {
        super(context);
        init(context, null);
    }

    public PaperStrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PaperStrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("Recycle")
    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PaperStrokeTextView);
        setBackground(Selector.shapeBuild()
                .setCornerRadius(a.getDimensionPixelSize(R.styleable.PaperStrokeTextView_cornerRadius, 1))
                .setStrokeWidth(ScreenUtils.dpToPx(2))
                .setDefaultStrokeColor(ThemeStore.textColorPrimary(context))
                .setDefaultBgColor(ThemeStore.backgroundColor(context))
                .create());
        setTextColor(Selector.colorBuild()
                .setDefaultColor(ThemeStore.textColorPrimary(context))
                .create());
    }
}
