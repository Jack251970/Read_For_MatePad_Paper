package com.jack.bookshelf.view.popupwindow.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
