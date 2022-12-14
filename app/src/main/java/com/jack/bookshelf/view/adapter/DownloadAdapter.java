package com.jack.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.DownloadBookBean;
import com.jack.bookshelf.service.DownloadService;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.view.activity.DownloadActivity;
import com.jack.bookshelf.widget.cover.CoverImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Download Item Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.MyViewHolder> {
    private final DownloadActivity activity;
    private final List<DownloadBookBean> data;
    private final Object mLock = new Object();

    public DownloadAdapter(DownloadActivity activity) {
        this.activity = activity;
        data = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void upDataS(List<DownloadBookBean> dataS) {
        synchronized (mLock) {
            this.data.clear();
            if (dataS != null) {
                this.data.addAll(dataS);
                Collections.sort(this.data);
            }
        }
        if (dataS != null) {
            notifyDataSetChanged();
        }
    }

    public void upData(DownloadBookBean data) {
        int index = -1;
        synchronized (mLock) {
            if (data != null && !this.data.isEmpty()) {
                index = this.data.indexOf(data);
                if (index >= 0) {
                    this.data.set(index, data);
                }
            }
        }
        if (index >= 0) {
            notifyItemChanged(index, data.getWaitingCount());
        }
    }

    public void removeData(DownloadBookBean data) {
        int index = -1;
        synchronized (mLock) {
            if (data != null && !this.data.isEmpty()) {
                index = this.data.indexOf(data);
                if (index >= 0) {
                    this.data.remove(index);
                }
            }
        }
        if (index >= 0) {
            notifyItemRemoved(index);
        }
    }

    public void addData(DownloadBookBean data) {
        synchronized (mLock) {
            if (data != null) {
                this.data.add(data);
            }
        }
        if (data != null) {
            notifyItemInserted(this.data.size() - 1);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {}


    @SuppressLint("StringFormatMatches")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull List<Object> payloads) {
        final DownloadBookBean item = data.get(holder.getLayoutPosition());
        if (!payloads.isEmpty()) {
            String text = item.getName() + "(" + StringUtils.getString(R.string.is_downloading) + ")";
            holder.tvName.setText(text);
            holder.tvDownload.setText(activity.getString(R.string.un_download, payloads.get(0)));
        } else {
            holder.ivDel.getDrawable().mutate();
            holder.ivCover.load(item.getCoverUrl(), item.getName(), null);
            if (item.getSuccessCount() > 0) {
                String text = item.getName() + "(" + StringUtils.getString(R.string.is_downloading) + ")";
                holder.tvName.setText(text);
            } else {
                String text = item.getName() + "(" + StringUtils.getString(R.string.wait_download) + ")";
                holder.tvName.setText(text);
            }
            holder.tvDownload.setText(activity.getString(R.string.un_download, item.getDownloadCount() - item.getSuccessCount()));
            holder.ivDel.setOnClickListener(view -> DownloadService.removeDownload(activity, item.getNoteUrl()));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CoverImageView ivCover;
        TextView tvName;
        TextView tvDownload;
        ImageView ivDel;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDownload = itemView.findViewById(R.id.tv_download);
            ivDel = itemView.findViewById(R.id.iv_delete);
        }
    }
}
