package com.jack.bookshelf.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.ReplaceRuleBean;
import com.jack.bookshelf.help.ItemTouchCallback;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.view.activity.ReplaceRuleActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by GKF on 2017/12/22.
 * 替换净化Adapter
 * Edited by Jack Ye
 */

public class ReplaceRuleAdapter extends RecyclerView.Adapter<ReplaceRuleAdapter.MyViewHolder> {
    private final List<ReplaceRuleBean> data;
    private final ReplaceRuleActivity activity;
    private final ItemTouchCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new ItemTouchCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(data, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            notifyItemChanged(srcPosition);
            notifyItemChanged(targetPosition);
            activity.saveDataS();
            return true;
        }
    };

    public ReplaceRuleAdapter(ReplaceRuleActivity activity) {
        this.activity = activity;
        data = new ArrayList<>();
    }

    public ItemTouchCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    public void resetDataS(List<ReplaceRuleBean> dataList) {
        this.data.clear();
        this.data.addAll(dataList);
        notifyDataSetChanged();
        activity.upDateSelectAll();
    }

    public List<ReplaceRuleBean> getData() {
        return data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_replace_rule, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.itemView.setBackgroundColor(ThemeStore.backgroundColor(activity));
        holder.checkBox.setText(data.get(position).getReplaceSummary());
        holder.checkBox.setChecked(data.get(position).getEnable());
        holder.checkBox.setOnClickListener((View view) -> {
            data.get(position).setEnable(holder.checkBox.isChecked());
            activity.upDateSelectAll();
            activity.saveDataS();
        });
        holder.editView.setOnClickListener(view -> activity.editReplaceRule(data.get(position)));
        holder.delView.setOnClickListener(view -> {
            activity.delData(data.get(position));
            data.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageView editView;
        ImageView delView;

        MyViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cb_replace_rule);
            editView = itemView.findViewById(R.id.iv_edit);
            delView = itemView.findViewById(R.id.iv_del);
        }
    }
}
