package com.jack.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jack.bookshelf.R;

import java.util.List;

/**
 * Adapter of SelectMenu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

@SuppressLint("DefaultLocale")
public class SelectMenuAdapter extends BaseAdapter{
    private final Context mContext;
    private final List<String> menuList;
    private final int lastChoose;

    public SelectMenuAdapter(Context context, List<String> menuList) {
        this.mContext = context;
        this.menuList = menuList;
        this.lastChoose = -1;
    }

    public SelectMenuAdapter(Context context, List<String> menuList, int lastChoose) {
        this.mContext = context;
        this.menuList = menuList;
        this.lastChoose = lastChoose;
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

    @SuppressLint({"InflateParams", "ViewHolder"})
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        holder = new ViewHolder();
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_select_menu, null);
        holder.tv_name_select_menu_item = convertView.findViewById(R.id.mpp_tv_name_select_menu_item);
        holder.iv_indicator_select_menu_item = convertView.findViewById(R.id.mpp_iv_indicator_select_menu_item);
        String menuName = menuList.get(position);
        holder.tv_name_select_menu_item.setText(menuName);
        if (lastChoose == -1) {
            holder.iv_indicator_select_menu_item.setVisibility(View.GONE);
        } else if (position == lastChoose) {
            holder.iv_indicator_select_menu_item.setImageResource(R.drawable.ic_select_menu_selected);
        }
        return convertView;
    }

    private static final class ViewHolder {
        public TextView tv_name_select_menu_item;
        public ImageView iv_indicator_select_menu_item;
    }
}
