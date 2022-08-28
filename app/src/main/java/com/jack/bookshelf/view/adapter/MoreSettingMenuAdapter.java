package com.jack.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jack.bookshelf.R;

import java.util.List;

/**
 * Adapter of MoreSetting Menu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

@SuppressLint("DefaultLocale")
public class MoreSettingMenuAdapter extends BaseAdapter{

    private final Context mContext;
    private final List<String> menuList;

    public MoreSettingMenuAdapter(Context context, List<String> menuList) {
        this.mContext = context;
        this.menuList = menuList;
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
            holder.tv_item = convertView.findViewById(R.id.tv_item_more_setting_menu);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String menuName = menuList.get(position);
        holder.tv_item.setText(menuName);
        return convertView;
    }

    private static final class ViewHolder {
        public TextView tv_item;
    }
}
