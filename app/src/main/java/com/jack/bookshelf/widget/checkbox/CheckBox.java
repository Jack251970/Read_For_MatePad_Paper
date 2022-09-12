package com.jack.bookshelf.widget.checkbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;

/**
 * Check Box
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class CheckBox extends AppCompatImageView {
    private boolean checked = false;
    private String preferenceKey = null;
    private final SharedPreferences prefer = MApplication.getConfigPreferences();

    public CheckBox(@NonNull Context context) {
        super(context);
    }

    public CheckBox(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckBox(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CheckBox setPreferenceKey(int idPreferenceKey, boolean defaultValue) {
        return setPreferenceKey(MApplication.getAppResources().getString(idPreferenceKey), defaultValue);
    }

    public CheckBox setPreferenceKey(String preferenceKey, boolean defaultValue) {
        this.preferenceKey = preferenceKey;
        if (prefer.getBoolean(preferenceKey, defaultValue)) {
            this.checked = true;
            setImageResource(R.drawable.ic_checkbox_checked);
        } else {
            this.checked = false;
            setImageResource(R.drawable.ic_checkbox_unchecked);
        }
        setOnClickListener(v -> setChecked(!checked));
        return this;
    }

    public CheckBox setAddedListener(@NonNull OnItemClickListener itemClick) {
        setOnClickListener(v -> {
            setChecked(!checked);
            itemClick.forPositiveButton(checked);
        });
        return this;
    }

    public boolean getChecked() {
        return this.checked;
    }

    public void setChecked(boolean checked) {
        if (checked) {
            this.checked = true;
            setImageResource(R.drawable.ic_checkbox_checked);
        } else {
            this.checked = false;
            setImageResource(R.drawable.ic_checkbox_unchecked);
        }
        if (preferenceKey != null) {
            prefer.edit().putBoolean(preferenceKey, checked).apply();
        }
    }

    public interface OnItemClickListener {
        void forPositiveButton(boolean checked);
    }
}