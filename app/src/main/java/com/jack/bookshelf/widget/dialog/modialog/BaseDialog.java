package com.jack.bookshelf.widget.dialog.modialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jack.bookshelf.utils.SoftInputUtil;

public class BaseDialog extends Dialog {

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void dismiss() {
        View view = getCurrentFocus();
        if (view instanceof TextView) {
            SoftInputUtil.hideIMM(view);
        }
        super.dismiss();
    }
}
