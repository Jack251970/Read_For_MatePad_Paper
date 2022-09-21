package com.jack.bookshelf.widget.dialog.adapter;

import static android.text.TextUtils.isEmpty;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Change Source Item Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

@SuppressLint("NotifyDataSetChanged")
public class ChangeSourceAdapter extends RefreshRecyclerViewAdapter {
    private final List<SearchBookBean> allBookBeans;
    private CallBack callBack;

    public ChangeSourceAdapter(Boolean needLoadMore) {
        super(needLoadMore);
        allBookBeans = new ArrayList<>();
    }

    public void addSourceAdapter(SearchBookBean value) {
        allBookBeans.add(value);
        notifyDataSetChanged();
    }

    public void addAllSourceAdapter(List<SearchBookBean> value) {
        allBookBeans.addAll(value);
        notifyDataSetChanged();
    }

    public void reSetSourceAdapter() {
        allBookBeans.clear();
        notifyDataSetChanged();
    }

    public void removeData(SearchBookBean searchBookBean) {
        DbHelper.getDaoSession().getSearchBookBeanDao().delete(searchBookBean);
        allBookBeans.remove(searchBookBean);
        notifyDataSetChanged();
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public List<SearchBookBean> getSearchBookBeans() {
        return allBookBeans;
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_source, parent, false));
    }

    @Override
    public void onBindIViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.bind(allBookBeans.get(position), callBack);
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return allBookBeans.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llContent;
        TextView tvBookSource;
        TextView tvLastChapter;
        ImageView ivChecked;

        MyViewHolder(View itemView) {
            super(itemView);
            llContent = itemView.findViewById(R.id.cl_content);
            tvBookSource = itemView.findViewById(R.id.tv_source_name);
            tvLastChapter = itemView.findViewById(R.id.tv_lastChapter);
            ivChecked = itemView.findViewById(R.id.iv_checked);
        }

        public void bind(SearchBookBean searchBookBean, CallBack callBack) {
            tvBookSource.setText(searchBookBean.getOrigin());
            if (isEmpty(searchBookBean.getLastChapter())) {
                tvLastChapter.setText(R.string.no_last_chapter);
            } else {
                tvLastChapter.setText(searchBookBean.getLastChapter());
            }
            if (searchBookBean.getIsCurrentSource()) {
                ivChecked.setVisibility(View.VISIBLE);
            } else {
                ivChecked.setVisibility(View.INVISIBLE);
            }
            llContent.setOnClickListener(view -> {
                if (callBack != null) {
                    callBack.changeTo(searchBookBean);
                }
            });
            llContent.setOnLongClickListener(view -> {
                if (callBack != null) {
                    callBack.showMenu(view, searchBookBean);
                }
                return true;
            });
        }
    }

    public interface CallBack {
        void changeTo(SearchBookBean searchBookBean);

        void showMenu(View view, SearchBookBean searchBookBean);
    }
}