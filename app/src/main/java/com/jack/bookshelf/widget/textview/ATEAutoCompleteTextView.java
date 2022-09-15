package com.jack.bookshelf.widget.textview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.jack.bookshelf.utils.Selector;
import com.jack.bookshelf.utils.theme.ThemeStore;

public class ATEAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public ATEAutoCompleteTextView(Context context) {
        super(context);
        init(context);
    }

    public ATEAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ATEAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundTintList(Selector.colorBuild()
                .setFocusedColor(ThemeStore.accentColor(context))
                .setDefaultColor(ThemeStore.textColorPrimary(context))
                .create());
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
