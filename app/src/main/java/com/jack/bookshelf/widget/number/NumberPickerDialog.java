package com.jack.bookshelf.widget.number;

import static com.jack.bookshelf.utils.StringUtils.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.SoftInputUtil;

/**
 * Number Picker Dialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class NumberPickerDialog extends PopupWindow {
    private final Context context;
    private TextView tvTitle;
    private NumberPicker numberPicker;
    private TextView tvPositiveButton;
    private TextView bindTextView = null;
    private int oldValue;
    private String preferenceKey = null;
    private final SharedPreferences prefer = MApplication.getConfigPreferences();

    public static NumberPickerDialog builder(Context context) { return new NumberPickerDialog(context); }

    @SuppressLint({"InflateParams"})
    public NumberPickerDialog(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_number_picker, null);
        this.setContentView(view);
        bindView(view);
        setFocusable(true);
        setTouchable(true);
    }

    private void bindView(View view) {
        tvTitle = view.findViewById(R.id.tv_title_dialog_number_picker);
        view.findViewById(R.id.tv_dialog_number_picker_negative_button).setOnClickListener(v -> dismiss());
        tvPositiveButton = view.findViewById(R.id.tv_dialog_number_picker_positive_button);
        numberPicker = view.findViewById(R.id.number_picker);
    }

    public NumberPickerDialog setBindTextView(View view) {
        if (view instanceof TextView) {
            this.bindTextView = (TextView) view;
        }
        return this;
    }

    public NumberPickerDialog setPreferenceKey(int idPreferenceKey, int defaultValue) {
        return setPreferenceKey(getString(idPreferenceKey),defaultValue);
    }

    public NumberPickerDialog setPreferenceKey(String preferenceKey, int defaultValue) {
        setValue(prefer.getInt(preferenceKey,defaultValue));
        tvPositiveButton.setOnClickListener(v -> {
            dismiss();
            numberPicker.clearFocus();
            SoftInputUtil.hideIMM(numberPicker);
            if (oldValue != numberPicker.getValue()) {
                if (bindTextView != null) {
                    bindTextView.setText(String.valueOf(numberPicker.getValue()));
                }
                prefer.edit().putInt(preferenceKey,numberPicker.getValue()).apply();
            }
        });
        this.preferenceKey = preferenceKey;
        return this;
    }

    public NumberPickerDialog setTitle(int strId) {
        return setTitle(getString(strId));
    }

    public NumberPickerDialog setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    public NumberPickerDialog setMaxValue(int value) {
        numberPicker.setMaxValue(value);
        return this;
    }

    public NumberPickerDialog setMinValue(int value) {
        numberPicker.setMinValue(value);
        return this;
    }

    public NumberPickerDialog setValue(int value) {
        numberPicker.setValue(value);
        this.oldValue = value;
        return this;
    }

    public NumberPickerDialog setAddedListener(@NonNull OnItemClickListener itemClick) {
        tvPositiveButton.setOnClickListener(v -> {
            dismiss();
            numberPicker.clearFocus();
            SoftInputUtil.hideIMM(numberPicker);
            itemClick.forPositiveButton(oldValue, numberPicker.getValue());
            if (oldValue != numberPicker.getValue()) {
                if (bindTextView != null) {
                    bindTextView.setText(String.valueOf(numberPicker.getValue()));
                }
                if (preferenceKey != null) {
                    prefer.edit().putInt(preferenceKey,numberPicker.getValue()).apply();
                }
            }
        });
        return this;
    }

    public void show(View mainView) {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnItemClickListener {
        void forPositiveButton(int oldValue, int value);
    }
}