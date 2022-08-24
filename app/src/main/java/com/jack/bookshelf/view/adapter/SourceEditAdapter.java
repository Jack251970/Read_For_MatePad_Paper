package com.jack.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.google.android.material.textfield.TextInputLayout;
import com.jack.bookshelf.R;
import com.jack.bookshelf.view.activity.SourceEditActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Book Source Item Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class SourceEditAdapter extends Adapter<SourceEditAdapter.MyViewHolder> {
    private final Context context;
    private List<SourceEditActivity.SourceEdit> data = new ArrayList<>();

    public SourceEditAdapter(Context context) {
        this.context = context;
    }

    public void reSetData(List<SourceEditActivity.SourceEdit> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source_edit, parent, false));
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (holder.editText.getTag(R.id.tag1) == null) {
            View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    holder.editText.setCursorVisible(false);
                    holder.editText.setCursorVisible(true);
                    holder.editText.setFocusable(true);
                    holder.editText.setFocusableInTouchMode(true);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                }
            };
            holder.editText.addOnAttachStateChangeListener(listener);
            holder.editText.setTag(R.id.tag1, listener);
        }
        if (holder.editText.getTag(R.id.tag2) != null && holder.editText.getTag(R.id.tag2) instanceof TextWatcher) {
            holder.editText.removeTextChangedListener((TextWatcher) holder.editText.getTag(R.id.tag2));
        }
        holder.editText.setText(data.get(position).getValue());
        holder.textInputLayout.setHint(context.getString(data.get(position).getHint()));
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.get(position).setValue(s == null ? null : s.toString());
            }
        };
        holder.editText.addTextChangedListener(textWatcher);
        holder.editText.setTag(R.id.tag2, textWatcher);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextInputLayout textInputLayout;
        AppCompatEditText editText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textInputLayout = itemView.findViewById(R.id.textInputLayout);
            editText = itemView.findViewById(R.id.editText);
        }
    }
}

