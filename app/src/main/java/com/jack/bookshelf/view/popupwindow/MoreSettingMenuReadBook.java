package com.jack.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.popupwindow.PopupWindowsUtil;
import com.jack.bookshelf.widget.views.ATECheckBox;

/**
 * PopupMenu in Read Book Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class MoreSettingMenuReadBook extends PopupWindow {
    private final View view;
    private LinearLayout disableBookSourceOnline;
    private LinearLayout editBookSourceOnline;
    private LinearLayout replace;
    private LinearLayout editReplaceRule;
    private LinearLayout lightNovelParagraph;
    private LinearLayout updateChapter;
    private LinearLayout openBrowserOnline;

    private OnItemClickListener itemClick;

    public static MoreSettingMenuReadBook builder(Context context, boolean online) {
        return new MoreSettingMenuReadBook(context, online);
    }

    public MoreSettingMenuReadBook setOnclick(@NonNull OnItemClickListener itemClick) {
        this.itemClick = itemClick;
        return this;
    }

    public MoreSettingMenuReadBook setCheckBox(boolean enableReplaceRule, boolean enableLightParagraph) {
        ((ATECheckBox)view.findViewById(R.id.checkbox_replace)).setChecked(enableReplaceRule);
        ((ATECheckBox)view.findViewById(R.id.checkbox_light_novel_paragraph)).setChecked(enableLightParagraph);
        return this;
    }

    @SuppressLint("InflateParams")
    public MoreSettingMenuReadBook(Context context, boolean online) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.view = LayoutInflater.from(context).inflate(R.layout.menu_more_setting_book_read, null);
        this.setContentView(view);
        bindView(online);
        bindEvent(online);
        setFocusable(true);
        setTouchable(true);
    }

    private void bindView(boolean online) {
        replace = view.findViewById(R.id.ll_replace);
        editReplaceRule = view.findViewById(R.id.ll_edit_replace_rule);
        lightNovelParagraph = view.findViewById(R.id.ll_light_novel_paragraph);
        updateChapter = view.findViewById(R.id.ll_update_chapter);
        if (online) {
            view.findViewById(R.id.ll_more_setting_menu_book_read_online1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.ll_more_setting_menu_book_read_online2).setVisibility(View.VISIBLE);
            disableBookSourceOnline = view.findViewById(R.id.ll_disable_book_source_online);
            editBookSourceOnline = view.findViewById(R.id.ll_edit_book_source_online);
            openBrowserOnline = view.findViewById(R.id.ll_open_browser_online);
        }
    }

    private void bindEvent(boolean online) {
        replace.setOnClickListener(v -> {
            dismiss();
            itemClick.forLocalMenuItem(0);
        });
        editReplaceRule.setOnClickListener(v -> {
            dismiss();
            itemClick.forLocalMenuItem(1);
        });
        lightNovelParagraph.setOnClickListener(v -> {
            dismiss();
            itemClick.forLocalMenuItem(2);
        });
        updateChapter.setOnClickListener(v -> {
            dismiss();
            itemClick.forLocalMenuItem(3);
        });
        if (online) {
            disableBookSourceOnline.setOnClickListener(v -> {
                dismiss();
                itemClick.forOnlineMenuItem(0);
            });
            editBookSourceOnline.setOnClickListener(v -> {
                dismiss();
                itemClick.forOnlineMenuItem(1);
            });
            openBrowserOnline.setOnClickListener(v -> {
                dismiss();
                itemClick.forOnlineMenuItem(2);
            });
        }
    }

    public void show(final View mainView, final View anchorView) {
        int[] windowPos = PopupWindowsUtil.calculatePopWindowPos(anchorView,view);
        showAtLocation(mainView, Gravity.TOP | Gravity.START, windowPos[0] - 30, windowPos[1] + 75);
    }

    public interface OnItemClickListener {
        void forLocalMenuItem(int position);

        void forOnlineMenuItem(int position);
    }
}
