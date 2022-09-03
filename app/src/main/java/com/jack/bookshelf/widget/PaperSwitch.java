package com.jack.bookshelf.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;

public class PaperSwitch extends androidx.appcompat.widget.AppCompatImageView {
    private boolean checked = false;
    private String preferenceKey = null;
    private final SharedPreferences prefer = MApplication.getConfigPreferences();

    public PaperSwitch(@NonNull Context context) {
        super(context);
    }

    public PaperSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PaperSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initPreferenceKey(String preferenceKey, boolean defaultValue) {
        setChecked(prefer.getBoolean(preferenceKey, defaultValue));
        this.preferenceKey = preferenceKey;
    }

    public void setChecked(boolean checked) {
        if (checked) {
            this.checked = true;
            setImageResource(R.drawable.ic_switch_checked);
        } else {
            this.checked = false;
            setImageResource(R.drawable.ic_switch_unchecked);
        }
        if (preferenceKey != null) {
            prefer.edit().putBoolean(preferenceKey, checked).apply();
        }
    }

    public boolean getChecked() {
        return this.checked;
    }
}