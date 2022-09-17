package com.jack.bookshelf.widget.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.jack.bookshelf.utils.Selector;

/**
 * Auto Complete TextView
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class AutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public AutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    public AutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundTintList(Selector.colorBuild()
                .setFocusedColor(Color.WHITE)
                .setDefaultColor(Color.WHITE).create());
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            showDropDown();
        }
        return super.onTouchEvent(event);
    }
}
