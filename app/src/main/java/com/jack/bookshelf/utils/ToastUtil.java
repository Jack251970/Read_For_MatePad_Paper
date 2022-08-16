package com.jack.bookshelf.utils;

import static com.jack.bookshelf.utils.StringUtils.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Toast Util
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ToastUtil {

    @SuppressLint("InflateParams")
    public static void toast(Context context, String msg, int length) {
        Toast toast = new Toast(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(com.monke.basemvplib.R.layout.dialog_toast,null);
        ((TextView)view.findViewById(com.monke.basemvplib.R.id.mpp_tv_toast)).setText(msg);
        toast.setView(view);
        toast.setDuration(length);
        toast.show();
    }

    @SuppressLint("InflateParams")
    public static void toast(Context context, int strId, int length) {
        Toast toast = new Toast(context);
        LayoutInflater inflater = LayoutInflater.from(context);
         View view = inflater.inflate(com.monke.basemvplib.R.layout.dialog_toast,null);
        ((TextView)view.findViewById(com.monke.basemvplib.R.id.mpp_tv_toast)).setText(getString(strId));
        toast.setView(view);
        toast.setDuration(length);
        toast.show();
    }
}
