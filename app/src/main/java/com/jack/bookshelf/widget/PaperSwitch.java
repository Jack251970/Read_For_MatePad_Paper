package com.jack.bookshelf.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;

/**
 * Switch
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

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

    public PaperSwitch setPreferenceKey(int idPreferenceKey, boolean defaultValue) {
        return setPreferenceKey(MApplication.getAppResources().getString(idPreferenceKey), defaultValue);
    }

    public PaperSwitch setPreferenceKey(String preferenceKey, boolean defaultValue) {
        this.setChecked(prefer.getBoolean(preferenceKey, defaultValue));
        this.preferenceKey = preferenceKey;
        this.setOnClickListener(v -> setChecked(!checked));
        return this;
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

    public void setAddedListener(@NonNull OnItemClickListener itemClick) {
        this.setOnClickListener(v -> {
            setChecked(!checked);
            itemClick.forPositiveButton(checked);
        });
    }

    public boolean getChecked() {
        return this.checked;
    }

    public interface OnItemClickListener {
        void forPositiveButton(boolean checked);
    }
}