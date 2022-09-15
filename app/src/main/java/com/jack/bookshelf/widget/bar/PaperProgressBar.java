package com.jack.bookshelf.widget.bar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.jack.bookshelf.R;

/**
 * Paper Progress Bar
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PaperProgressBar extends ProgressBar {

    public PaperProgressBar(Context context) {
        super(context);
        init(context);
    }

    public PaperProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PaperProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init(Context context) {
        // 设置进度条
        setProgressDrawable(context.getDrawable(R.drawable.shape_progressbar_background));
    }
}