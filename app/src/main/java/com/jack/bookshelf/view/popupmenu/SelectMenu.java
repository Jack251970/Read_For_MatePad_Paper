package com.jack.bookshelf.view.popupmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;
import com.jack.bookshelf.view.adapter.SelectMenuAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * SelectMenu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack Ye
 */

public class SelectMenu extends PopupWindow{
    private final View view;
    private final ListView lv_Menu;
    private final int lastChoose;

    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables"})
    public SelectMenu(Context context, String title, String bottomButton, String[] menuList, int lastChoose,@NonNull OnItemClickListener itemClick) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.lastChoose = lastChoose;
        this.view = LayoutInflater.from(context).inflate(R.layout.select_menu, null);
        this.setContentView(view);
        // 初始化标题和底部按钮
        TextView tv_title,tv_bottom_button;
        tv_title = view.findViewById(R.id.mpp_tv_title_selectMenu);
        tv_bottom_button = view.findViewById(R.id.mpp_tv_bottom_button_select_menu);
        tv_title.setText(title);
        tv_bottom_button.setText(bottomButton);
        // 底部按钮事件
        view.findViewById(R.id.mpp_ll_cancel_main).setOnClickListener(v -> {
            dismiss();
            itemClick.forBottomButton();
        });
        // 初始化列表视图
        List<String> menuNameList = Arrays.asList(menuList);
        SelectMenuAdapter adapter = new SelectMenuAdapter(context, menuNameList, this.lastChoose);
        lv_Menu = view.findViewById(R.id.mpp_lv_arrange_rule_main);
        lv_Menu.setAdapter(adapter);
        lv_Menu.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            itemClick.changeArrangeRule(lastChoose, position);
        });
        setFocusable(true);
        setTouchable(true);
    }

    public void show(final View mainView) {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnItemClickListener {
        void forBottomButton();

        void changeArrangeRule(int lastChoose, int position);
    }
}
