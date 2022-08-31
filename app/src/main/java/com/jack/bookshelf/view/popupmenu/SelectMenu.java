package com.jack.bookshelf.view.popupmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
 * Edited by Jack251970
 */

public class SelectMenu extends PopupWindow{
    private final Context context;
    private TextView tvTitle;
    private LinearLayout llBottomButton;
    private TextView tvBottomButton;
    private ListView lvMenu;
    private OnItemClickListener itemClick;

    public static SelectMenu builder(Context context) {
        return new SelectMenu(context);
    }

    public SelectMenu setTitle (String title) {
        tvTitle.setText(title);
        return this;
    }

    public SelectMenu setBottomButton (String text) {
        tvBottomButton.setText(text);
        llBottomButton.setOnClickListener(v -> {
            dismiss();
            itemClick.forBottomButton();
        });
        return this;
    }

    public SelectMenu setMenu (List<String> menuNameList) {
        SelectMenuAdapter adapter = new SelectMenuAdapter(context, menuNameList);
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            itemClick.forListItem(-1, position);
        });
        return this;
    }

    public SelectMenu setMenu (String[] menuList) {
        List<String> menuNameList = Arrays.asList(menuList);
        SelectMenuAdapter adapter = new SelectMenuAdapter(context, menuNameList);
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            itemClick.forListItem(-1, position);
        });
        return this;
    }

    public SelectMenu setMenu (List<String> menuNameList, int lastChoose) {
        SelectMenuAdapter adapter = new SelectMenuAdapter(context, menuNameList, lastChoose);
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            itemClick.forListItem(lastChoose, position);
        });
        return this;
    }

    public SelectMenu setMenu (String[] menuList, int lastChoose) {
        List<String> menuNameList = Arrays.asList(menuList);
        SelectMenuAdapter adapter = new SelectMenuAdapter(context, menuNameList, lastChoose);
        lvMenu.setAdapter(adapter);
        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            itemClick.forListItem(lastChoose, position);
        });
        return this;
    }

    public SelectMenu setListener(@NonNull OnItemClickListener itemClick) {
        this.itemClick = itemClick;
        return this;
    }

    @SuppressLint({"InflateParams"})
    public SelectMenu(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.select_menu, null);
        this.setContentView(view);
        bindView(view);
        setFocusable(true);
        setTouchable(true);
    }

    private void bindView(View view) {
        tvTitle = view.findViewById(R.id.mpp_tv_title_selectMenu);
        llBottomButton = view.findViewById(R.id.mpp_ll_bottom_button_select_menu);
        tvBottomButton = view.findViewById(R.id.mpp_tv_bottom_button_select_menu);
        lvMenu = view.findViewById(R.id.mpp_lv_arrange_rule_main);
    }

    public void show(View mainView) {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnItemClickListener {
        void forBottomButton();

        void forListItem(int lastChoose, int position);
    }
}