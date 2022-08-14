package com.jack.bookshelf.widget.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;

import com.jack.bookshelf.utils.theme.ATH;
import com.jack.bookshelf.utils.theme.ThemeStore;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ATESeekBar extends AppCompatSeekBar {

    public ATESeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public ATESeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATESeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        ATH.setTint(this, ThemeStore.accentColor(context));
    }
}
