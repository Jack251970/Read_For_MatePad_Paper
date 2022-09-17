package com.jack.bookshelf.widget.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.screen.view.PopupWindowUtil;

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
        int[] windowPos = PopupWindowUtil.calculatePopWindowPos(anchorView,view);
        showAtLocation(mainView, Gravity.TOP | Gravity.START, windowPos[0] - 30, windowPos[1] + 10);
    }

    public void showForChangeSourceDialog(final View mainView, final View anchorView) {
        int[] windowPos = PopupWindowUtil.calculatePopWindowPos(anchorView,view);
        showAtLocation(mainView, Gravity.TOP | Gravity.START, 30, windowPos[1] - 50);
    }

    public interface OnItemClickListener {
        void chooseMenuItem(int position);
    }

    public static class MoreSettingMenuAdapter extends BaseAdapter {

        private final Context mContext;
        private final List<String> menuList;
        private final TypedArray iconList;

        public MoreSettingMenuAdapter(Context context, List<String> menuList) {
            this.mContext = context;
            this.menuList = menuList;
            this.iconList = null;
        }

        public MoreSettingMenuAdapter(Context context, List<String> menuList, TypedArray iconList) {
            this.mContext = context;
            this.menuList = menuList;
            this.iconList = iconList;
        }

        public int getCount() {
            return menuList.size();
        }

        public Object getItem(int position) {
            return menuList.get(position);
        }

        public long getItemId(int id) {
            return id;
        }

        @SuppressLint({"InflateParams"})
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_more_setting_menu, null);
                if (iconList != null) {
                    holder.iv_item = convertView.findViewById(R.id.iv_item_more_setting_menu);
                }
                holder.tv_item = convertView.findViewById(R.id.tv_item_more_setting_menu);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_item.setText(menuList.get(position));
            if (iconList != null) {
                holder.iv_item.setVisibility(View.VISIBLE);
                holder.iv_item.setImageResource(iconList.getResourceId(position,0));
            }
            return convertView;
        }

        private static final class ViewHolder {
            public ImageView iv_item;
            public TextView tv_item;
        }
    }
}