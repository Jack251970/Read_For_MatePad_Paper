package com.jack.bookshelf.widget.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;

import com.jack.bookshelf.R;

/**
 * Paper SeekBar
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PaperSeekBar extends AppCompatSeekBar {

    public PaperSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public PaperSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PaperSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init(Context context, AttributeSet attrs) {
        // 自定义Thumb
        setThumb(context.getDrawable(R.drawable.shape_seekbar_thumb));
        // 去除Thumb点按效果
        setDuplicateParentStateEnabled(true);
        // 设置进度条
        setProgressDrawable(context.getDrawable(R.drawable.shape_seekbar_progress_background));
    }
}
