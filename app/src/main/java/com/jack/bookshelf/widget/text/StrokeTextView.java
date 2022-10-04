package com.jack.bookshelf.widget.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.Selector;
import com.jack.bookshelf.utils.screen.ScreenUtils;

/**
 * Stroke TextView
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class StrokeTextView extends AppCompatTextView {

    public StrokeTextView(Context context) {
        super(context);
        init(context, null);
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("Recycle")
    private void init(Context context, AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PaperStrokeTextView);
        setBackground(Selector.shapeBuild()
                .setCornerRadius(a.getDimensionPixelSize(R.styleable.PaperStrokeTextView_cornerRadius, 1))
                .setStrokeWidth(ScreenUtils.dpToPx(2))
                .setDefaultStrokeColor(Color.BLACK)
                .setDefaultBgColor(Color.WHITE)
                .create());
        setTextColor(Selector.colorBuild()
                .setDefaultColor(Color.BLACK)
                .create());
    }
}
