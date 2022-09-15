package com.jack.bookshelf.widget.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.widget.dialog.modialog.BaseDialog;
import com.jack.bookshelf.widget.textview.ATEAutoCompleteTextView;

import java.util.List;
import java.util.Objects;

/**
 * Input Dialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class InputDialog extends BaseDialog {
    private final Context context;
    private boolean showDel = false;

    private TextView tvTitle;
    private ATEAutoCompleteTextView etInput;
    private TextView tvConfirm;
    private Callback callback = null;

    private TextView bindTextView = null;
    private String tvDefaultValue = null;
    private boolean ifSetDefaultValue = false;
    private boolean isPassWord = false;
    private String prefDefaultValue;
    private String oldPrefValue;
    private String preferenceKey = null;
    private final SharedPreferences prefer = MApplication.getConfigPreferences();

    public static final int WITHOUT_PREF = 0, PREF_WITH_BIND_TV = 1, PREF_WITHOUT_BIND_TV = 2;
    // WITHOUT_PREF一般需要setCallback
    // PREF_WITHOUT_BIND_TV必须setPreference
    // PREF_WITH_BIND_TV必须setPreference以及setBindTextView

    public static InputDialog builder(Context context) {
        return new InputDialog(context, WITHOUT_PREF);
    }

    public static InputDialog builder(Context context,int type) {
        return new InputDialog(context, type);
    }

    @SuppressLint("InflateParams")
    private InputDialog(Context context,int type) {
        super(context, R.style.PaperAlertDialogTheme);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        setContentView(view);
        bindView(view);
        bindEvent(type);
    }

    private void bindView(View view) {
        view.findViewById(R.id.ll_content).setOnClickListener(null);
        tvTitle = view.findViewById(R.id.tv_title_dialog_input);
        etInput = view.findViewById(R.id.atv_input_dialog_input);
        view.findViewById(R.id.tv_cancel_dialog_input).setOnClickListener(v -> dismiss());
        tvConfirm = view.findViewById(R.id.tv_confirm_dialog_input);
    }

    private void bindEvent(int type) {
        switch (type) {
            case PREF_WITH_BIND_TV:
                tvConfirm.setOnClickListener(view -> {
                    dismiss();
                    String value = etInput.getText().toString();
                    if (callback != null) {
                        callback.setInputText(value);
                    }
                    if (value.equals("")) {
                        bindTextView.setText(Objects.requireNonNullElse(tvDefaultValue, value));
                        prefer.edit().putString(preferenceKey, prefDefaultValue).apply();
                    } else if (!value.equals(oldPrefValue)) {
                        if (isPassWord) {
                            bindTextView.setText("************");
                        } else {
                            bindTextView.setText(value);
                        }
                        prefer.edit().putString(preferenceKey,value).apply();
                    }
                });
                break;
            case PREF_WITHOUT_BIND_TV:
                tvConfirm.setOnClickListener(view -> {
                    dismiss();
                    String value = etInput.getText().toString();
                    if (callback != null) {
                        callback.setInputText(value);
                    }
                    if (value.equals("")) {
                        prefer.edit().putString(preferenceKey, prefDefaultValue).apply();
                    } else if (!value.equals(oldPrefValue)) {
                        prefer.edit().putString(preferenceKey,value).apply();
                    }
                });
                break;
            case WITHOUT_PREF:
            default:
                tvConfirm.setOnClickListener(view -> {
                    dismiss();
                    String value = etInput.getText().toString();
                    callback.setInputText(value);
                });
                break;
        }
    }

    public InputDialog setBindTextView(View view, String tvDefaultValue, boolean isPassWord) {
        if (view instanceof TextView) {
            this.bindTextView = (TextView) view;
            bindTextView.setText(tvDefaultValue);
        }
        this.tvDefaultValue = tvDefaultValue;
        this.isPassWord = isPassWord;
        return this;
    }

    public InputDialog setPreferenceKey(String preferenceKey, String prefDefaultValue) {
        this.preferenceKey = preferenceKey;
        this.prefDefaultValue = prefDefaultValue;
        this.oldPrefValue = prefer.getString(preferenceKey, prefDefaultValue);
        if (oldPrefValue.equals(prefDefaultValue)) {
            if (bindTextView != null) {
                bindTextView.setText(Objects.requireNonNullElse(tvDefaultValue, ""));
            }
        } else {
            if (bindTextView != null) {
                if (isPassWord) {
                    bindTextView.setText("************");
                } else {
                    bindTextView.setText(oldPrefValue);
                }
            }
        }
        if (ifSetDefaultValue) {
            setDefaultValue(Objects.requireNonNullElseGet(oldPrefValue, () -> Objects.requireNonNullElse(prefDefaultValue, "")));
        }
        return this;
    }

    public InputDialog setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    public InputDialog setShowDel(boolean showDel) {
        this.showDel = showDel;
        return this;
    }

    public InputDialog setDefaultValue() {
        this.ifSetDefaultValue = true;
        return this;
    }

    public InputDialog setDefaultValue(String defaultValue) {
        if (defaultValue != null) {
            etInput.setTextSize(2, 15); // 2 --> sp
            etInput.setText(defaultValue);
            etInput.setSelectAllOnFocus(true);
        }
        return this;
    }

    public InputDialog setAdapterValues(List<String> adapterValues) {
        if (adapterValues != null) {
            MyAdapter mAdapter = new MyAdapter(context, adapterValues);
            etInput.setAdapter(mAdapter);
        }
        return this;
    }

    public InputDialog setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    class MyAdapter extends ArrayAdapter<String> {

        MyAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, R.layout.item_tip_text_input_dialog, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_tip_text_input_dialog, parent, false);
            } else {
                view = convertView;
            }
            TextView tv = view.findViewById(R.id.text);
            ImageView iv = view.findViewById(R.id.iv_del);
            if (showDel) {
                iv.setVisibility(View.VISIBLE);
            } else {
                iv.setVisibility(View.GONE);
            }
            String value = String.valueOf(getItem(position));
            tv.setText(value);
            iv.setOnClickListener(v -> {
                remove(value);
                if (callback != null) {
                    callback.delete(value);
                }
                etInput.showDropDown();
            });
            return view;
        }
    }

    public interface Callback {
        void setInputText(String inputText);

        void delete(String value);
    }
}