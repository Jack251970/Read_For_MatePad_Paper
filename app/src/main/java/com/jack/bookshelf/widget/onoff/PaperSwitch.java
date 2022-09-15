package com.jack.bookshelf.widget.onoff;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;

/**
 * Paper Switch
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PaperSwitch extends AppCompatImageView {
    private boolean checked = false;
    private boolean enabled = true;
    private String preferenceKey = null;
    private final SharedPreferences prefer = MApplication.getConfigPreferences();
    private PaperSwitch bindSwitch = null;

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
        this.preferenceKey = preferenceKey;
        if (prefer.getBoolean(preferenceKey, defaultValue)) {
            this.checked = true;
            if (enabled) {
                setImageResource(R.drawable.ic_switch_checked);
            } else {
                setImageResource(R.drawable.ic_switch_checked_unabled);
            }
        } else {
            this.checked = false;
            if (enabled) {
                setImageResource(R.drawable.ic_switch_unchecked);
            } else {
                setImageResource(R.drawable.ic_switch_unchecked_unabled);
            }
        }
        setOnClickListener(v -> {
            if (enabled) {
                setChecked(!checked);
                if (bindSwitch != null) {
                    bindSwitch.setEnabled(checked);
                }
            }
        });
        return this;
    }

    public PaperSwitch setAddedListener(@NonNull OnItemClickListener itemClick) {
        setOnClickListener(v -> {
            if (enabled) {
                setChecked(!checked);
                itemClick.forPositiveButton(checked);
                if (bindSwitch != null) {
                    bindSwitch.setEnabled(checked);
                }
            }
        });
        return this;
    }

    public boolean getChecked() {
        return this.checked;
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

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            if (checked) {
                setImageResource(R.drawable.ic_switch_checked);
            } else {
                setImageResource(R.drawable.ic_switch_unchecked);
            }
        } else {
            if (checked) {
                setImageResource(R.drawable.ic_switch_checked_unabled);
            } else {
                setImageResource(R.drawable.ic_switch_unchecked_unabled);
            }
        }
        this.enabled = enabled;
    }

    public PaperSwitch setBindSwitch(PaperSwitch bindSwitch) {
        this.bindSwitch = bindSwitch;
        bindSwitch.setEnabled(checked);
        return this;
    }

    public interface OnItemClickListener {
        void forPositiveButton(boolean checked);
    }
}