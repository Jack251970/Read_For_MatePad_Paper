package com.jack.bookshelf.widget.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.StringUtils;

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

    public SelectMenu setTitle (@StringRes int strId) {
        return setTitle(StringUtils.getString(strId));
    }

    public SelectMenu setTitle (String title) {
        tvTitle.setText(title);
        return this;
    }

    public SelectMenu setBottomButton (@StringRes int strId) {
        return setBottomButton(StringUtils.getString(strId));
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
        View view = LayoutInflater.from(context).inflate(R.layout.menu_select, null);
        this.setContentView(view);
        bindView(view);
        setFocusable(true);
        setTouchable(true);
    }

    private void bindView(View view) {
        tvTitle = view.findViewById(R.id.tv_title_selectMenu);
        llBottomButton = view.findViewById(R.id.ll_bottom_button_select_menu);
        tvBottomButton = view.findViewById(R.id.tv_bottom_button_select_menu);
        lvMenu = view.findViewById(R.id.lv_arrange_rule_main);
    }

    public void show(View mainView) {
        showAtLocation(mainView, Gravity.CENTER, 0, 0);
    }

    public interface OnItemClickListener {
        void forBottomButton();

        void forListItem(int lastChoose, int position);
    }

    public static class SelectMenuAdapter extends BaseAdapter {

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
            holder.tv_name_select_menu_item = convertView.findViewById(R.id.tv_name_select_menu_item);
            holder.iv_indicator_select_menu_item = convertView.findViewById(R.id.iv_indicator_select_menu_item);
            String menuName = menuList.get(position);
            holder.tv_name_select_menu_item.setText(menuName);
            if (lastChoose == -1) {
                holder.iv_indicator_select_menu_item.setVisibility(View.GONE);
            } else if (position == lastChoose) {
                holder.iv_indicator_select_menu_item.setImageResource(R.drawable.ic_select_menu_selected);
            }
            /* Use ViewHolder
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_select_menu, null);
                holder.tv_name_select_menu_item = convertView.findViewById(R.id.mpp_tv_name_select_menu_item);
                holder.iv_indicator_select_menu_item = convertView.findViewById(R.id.mpp_iv_indicator_select_menu_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String menuName = menuList.get(position);
            holder.tv_name_select_menu_item.setText(menuName);*/
            return convertView;
        }

        private static final class ViewHolder {
            public TextView tv_name_select_menu_item;
            public ImageView iv_indicator_select_menu_item;
        }
    }
}