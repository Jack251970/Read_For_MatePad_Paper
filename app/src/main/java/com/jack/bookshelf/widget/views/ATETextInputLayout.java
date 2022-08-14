package com.jack.bookshelf.widget.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.jack.bookshelf.utils.Selector;
import com.jack.bookshelf.utils.theme.ThemeStore;

public class ATETextInputLayout extends TextInputLayout {
    public ATETextInputLayout(Context context) {
        super(context);
        init(context);
    }

    public ATETextInputLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ATETextInputLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setHintTextColor(Selector.colorBuild().setDefaultColor(ThemeStore.accentColor(context)).create());
    }

    @Override
    public void draw(Canvas canvas) {

        super.draw(canvas);
    }
}
