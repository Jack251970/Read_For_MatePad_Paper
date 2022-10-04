package com.jack.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class SearchBookshelfAdapter extends RecyclerView.Adapter<SearchBookshelfAdapter.MyViewHolder> {

    private final List<BookInfoBean> beans = new ArrayList<>();
    private final CallBack callBack;

    public SearchBookshelfAdapter(CallBack callBack) {
        this.callBack = callBack;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<BookInfoBean> beans) {
        this.beans.clear();
        this.beans.addAll(beans);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textView.setText(beans.get(position).getName());
        holder.itemView.setOnClickListener(v -> callBack.openBookInfo(beans.get(position)));
    }

    @Override
    public int getItemCount() {
        return beans.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_item_search_history);
        }
    }

    public interface CallBack {
        void openBookInfo(BookInfoBean bookInfoBean);
    }
}

