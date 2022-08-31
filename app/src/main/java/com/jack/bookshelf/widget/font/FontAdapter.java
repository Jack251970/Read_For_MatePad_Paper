package com.jack.bookshelf.widget.font;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.FileDoc;

import java.io.File;
import java.io.FileDescriptor;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Item Font Selector Adapter
 * Edited by Jack251970
 */

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.MyViewHolder> {
    private final List<FileDoc> docList = new ArrayList<>();
    private final FontSelectorDialog.OnThisListener thisListener;
    private final Context context;
    private String selectName;

    FontAdapter(Context context, String selectPath, FontSelectorDialog.OnThisListener thisListener) {
        this.context = context;
        try {
            String[] x = URLDecoder.decode(selectPath, "utf-8")
                    .split(File.separator);
            this.selectName = x[x.length - 1];
        } catch (Exception e) {
            this.selectName = "";
        }
        this.thisListener = thisListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (docList.size() > 0) {
            FileDoc docItem = docList.get(position);
            try {
                Typeface typeface;
                if (docItem.isContentScheme()) {
                    FileDescriptor fd = context.getContentResolver().openFileDescriptor(docItem.getUri(), "r")
                            .getFileDescriptor();
                    typeface = new Typeface.Builder(fd).build();
                } else {
                    typeface = Typeface.createFromFile(docItem.getUri().toString());
                }
                holder.tvFont.setTypeface(typeface);
            } catch (Exception ignored) {

            }
            holder.tvFont.setText(docItem.getName());

            if (docItem.getName().equals(selectName)) {
                holder.ivChecked.setVisibility(View.VISIBLE);
            } else {
                holder.ivChecked.setVisibility(View.INVISIBLE);
            }
            holder.tvFont.setOnClickListener(view -> {
                if (thisListener != null) {
                    thisListener.setFontPath(docItem);
                }
            });
        } else {
            holder.tvFont.setText(R.string.fonts_folder);
        }
    }

    @Override
    public int getItemCount() {
        return docList.size() == 0 ? 1 : docList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    void upData(List<FileDoc> docItems) {
        if (docItems != null) {
            docList.clear();
            docList.addAll(docItems);
        }
        notifyDataSetChanged();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvFont;
        ImageView ivChecked;

        MyViewHolder(View itemView) {
            super(itemView);
            tvFont = itemView.findViewById(R.id.tv_font);
            ivChecked = itemView.findViewById(R.id.iv_checked);
        }
    }
}