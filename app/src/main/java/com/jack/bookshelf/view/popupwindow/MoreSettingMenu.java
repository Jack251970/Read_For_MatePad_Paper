package com.jack.bookshelf.view.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.popupwindow.PopupWindowsUtil;
import com.jack.bookshelf.view.popupwindow.adapter.MoreSettingMenuAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * More Setting Menu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class MoreSettingMenu extends PopupWindow {
    private final Context context;
    private final View view;
    private ListView lvMenu;
    private OnItemClickListener itemClick;

    public static MoreSettingMenu builder(Context context) {
        return new MoreSettingMenu(context);
    }

    public MoreSettingMenu setMenu (String[] menuList, TypedArray iconList) {
        List<String> menuNameList = Arrays.asList(menuList);
        MoreSettingMenuAdapter adapter = new MoreSettingMenuAdapter(context, menuNameList, iconList);
        view.findViewById(R.id.lv_more_setting_menu_with_icon).setVisibility(View.VISIBLE);
        lvMenu = view.findViewById(R.id.lv_more_setting_menu_with_icon);
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            itemClick.chooseMenuItem(position);
        });
        return this;
    }

    public MoreSettingMenu setMenu (int arrayId, TypedArray iconList) {
        return setMenu(context.getResources().getStringArray(arrayId), iconList);
    }

    public MoreSettingMenu setMenu (int arrayId, int iconListId) {
        return setMenu(context.getResources().getStringArray(arrayId), context.getResources().obtainTypedArray(iconListId));
    }

    public MoreSettingMenu setMenu (String[] menuList) {
        List<String> menuNameList = Arrays.asList(menuList);
        MoreSettingMenuAdapter adapter = new MoreSettingMenuAdapter(context, menuNameList);
        view.findViewById(R.id.lv_more_setting_menu_without_icon).setVisibility(View.VISIBLE);
        lvMenu = view.findViewById(R.id.lv_more_setting_menu_without_icon);
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            itemClick.chooseMenuItem(position);
        });
        return this;
    }

    public MoreSettingMenu setMenu (int arrayId) {
        return setMenu(context.getResources().getStringArray(arrayId));
    }

    public MoreSettingMenu setOnclick(@NonNull OnItemClickListener itemClick) {
        this.itemClick = itemClick;
        return this;
    }

    @SuppressLint({"InflateParams"})
    public MoreSettingMenu(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.menu_more_setting, null);
        this.setContentView(view);
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