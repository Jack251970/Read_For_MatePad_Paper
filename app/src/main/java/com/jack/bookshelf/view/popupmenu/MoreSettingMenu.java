package com.jack.bookshelf.view.popupmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.popupwindow.PopupWindowsUtil;
import com.jack.bookshelf.view.adapter.MoreSettingMenuAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * MoreSetting Menu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class MoreSettingMenu extends PopupWindow {
    private final Context context;
    private View view = null;
    private final ListView lvMenu;
    private OnItemClickListener itemClick;

    public static MoreSettingMenu builder(Context context) {
        return new MoreSettingMenu(context);
    }

    public MoreSettingMenu setMenu (String[] menuList) {
        List<String> menuNameList = Arrays.asList(menuList);
        MoreSettingMenuAdapter adapter = new MoreSettingMenuAdapter(context, menuNameList);
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            itemClick.chooseMenuItem(position);
        });
        return this;
    }

    public MoreSettingMenu setMenu (int arrayId) {
        List<String> menuNameList = Arrays.asList(context.getResources().getStringArray(arrayId));
        MoreSettingMenuAdapter adapter = new MoreSettingMenuAdapter(context, menuNameList);
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            itemClick.chooseMenuItem(position);
        });
        return this;
    }

    public MoreSettingMenu setOnclick(@NonNull OnItemClickListener itemClick) {
        this.itemClick = itemClick;
        return this;
    }

    @SuppressLint({"InflateParams"})
    public MoreSettingMenu(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.more_setting_menu, null);
        this.setContentView(view);
        lvMenu = view.findViewById(R.id.lv_more_setting_menu);
        setFocusable(true);
        setTouchable(true);
    }

    public void show(final View mainView, final View anchorView) {
        int[] windowPos = PopupWindowsUtil.calculatePopWindowPos(anchorView,view);
        showAtLocation(mainView, Gravity.TOP | Gravity.START, windowPos[0] - 30, windowPos[1] + 10);
    }

    public void showForChangeSourceDialog(final View mainView, final View anchorView) {
        int[] windowPos = PopupWindowsUtil.calculatePopWindowPos(anchorView,view);
        showAtLocation(mainView, Gravity.TOP | Gravity.START, 30, windowPos[1] - 50);
    }

    public interface OnItemClickListener {
        void chooseMenuItem(int position);
    }
}
