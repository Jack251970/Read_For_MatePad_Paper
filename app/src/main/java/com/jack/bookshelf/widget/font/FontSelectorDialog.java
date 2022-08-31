package com.jack.bookshelf.widget.font;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.FileDoc;
import com.jack.bookshelf.utils.theme.ATH;

import java.util.List;

import kotlin.text.Regex;

/**
 * Font Selector Dialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class FontSelectorDialog extends PopupWindow {
    private final Context context;
    private RecyclerView recyclerView;
    private OnThisListener thisListener;
    public static Regex fontRegex = new Regex("(?i).*\\.[ot]tf");

    @SuppressLint("InflateParams")
    public FontSelectorDialog(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_font_selector, null);
        this.setContentView(view);
        bindView(view);
        setFocusable(true);
        setTouchable(true);
    }

    private void bindView(View view) {
        view.findViewById(R.id.tv_reset_default_font).setOnClickListener(v -> {
            dismiss();
            thisListener.setDefault();
        });
        recyclerView = view.findViewById(R.id.recycler_view_font_select);
    }

    public FontSelectorDialog setFile(String selectPath, List<FileDoc> docItems){
        FontAdapter adapter = new FontAdapter(context, selectPath, new OnThisListener() {
            @Override
            public void setDefault() {
                if (thisListener != null) {
                    thisListener.setDefault();
                }
                dismiss();
            }

            @Override
            public void setFontPath(FileDoc fileDoc) {
                if (thisListener != null) {
                    thisListener.setFontPath(fileDoc);
                }
                dismiss();
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter.upData(docItems);
        return this;
    }

    public FontSelectorDialog setListener(OnThisListener thisListener) {
        this.thisListener = thisListener;
        return this;
    }

    public void show(View mainView) {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnThisListener {
        void setDefault();

        void setFontPath(FileDoc fileDoc);
    }
}