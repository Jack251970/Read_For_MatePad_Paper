package com.jack.bookshelf.widget.filepicker.adapter;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.R;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Item Path Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class PathAdapter extends RecyclerView.Adapter<PathAdapter.MyViewHolder> {
    private static final String ROOT_HINT = "SD";
    private final LinkedList<String> paths = new LinkedList<>();
    private Drawable arrowIcon = null;
    private final String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
    private CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public String getItem(int position) {
        StringBuilder tmp = new StringBuilder(sdCardDirectory + "/");
        //忽略根目录
        if (position == 0) {
            return tmp.toString();
        }
        for (int i = 1; i <= position; i++) {
            tmp.append(paths.get(i)).append("/");
        }
        return tmp.toString();
    }

    public void setArrowIcon(Drawable arrowIcon) {
        this.arrowIcon = arrowIcon;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePath(String path) {
        path = path.replace(sdCardDirectory, "");
        paths.clear();
        if (!path.equals("/") && !path.equals("")) {
            String[] temp = path.substring(path.indexOf("/") + 1).split("/");
            Collections.addAll(paths, temp);
        }
        paths.addFirst(ROOT_HINT);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_path_file_picker, parent, false));
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.textView.setText(paths.get(position));
        if (arrowIcon != null) {
            holder.imageView.setImageDrawable(arrowIcon);
        }
        holder.itemView.setOnClickListener(v -> {
            if (callBack != null) {
                callBack.onPathClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_item_path_file_picker);
            textView = itemView.findViewById(R.id.tv_item_path_file_picker);
        }
    }

    public interface CallBack {
        void onPathClick(int position);
    }
}
