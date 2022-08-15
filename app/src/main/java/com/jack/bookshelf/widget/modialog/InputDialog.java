package com.jack.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jack.bookshelf.R;
import com.jack.bookshelf.widget.views.ATEAutoCompleteTextView;

import java.util.List;

/**
 * InputDialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class InputDialog extends BaseDialog {
    private boolean showDel = false;
    private TextView tvTitle;
    private ATEAutoCompleteTextView etInput;
    private TextView tvOk;
    private Callback callback = null;
    private final Context context;

    public static InputDialog builder(Context context) {
        return new InputDialog(context);
    }

    public InputDialog setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    @SuppressLint("InflateParams")
    private InputDialog(Context context) {
        super(context, R.style.alertDialogTheme);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        setContentView(view);
        bindView(view);
    }

    public InputDialog setShowDel(boolean showDel) {
        this.showDel = showDel;
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

    private void bindView(View view) {
        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        tvTitle = view.findViewById(R.id.tv_title);
        etInput = view.findViewById(R.id.et_input);
        tvOk = view.findViewById(R.id.tv_ok);
    }

    public InputDialog setCallback(Callback callback) {
        this.callback = callback;
        tvOk.setOnClickListener(view -> {
            callback.setInputText(etInput.getText().toString());
            dismiss();
        });
        return this;
    }

    class MyAdapter extends ArrayAdapter<String> {

        MyAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, R.layout.item_1line_text_input_dialog, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_1line_text_input_dialog, parent, false);
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
