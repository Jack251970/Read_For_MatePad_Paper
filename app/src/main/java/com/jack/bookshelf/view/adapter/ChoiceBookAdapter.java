package com.jack.bookshelf.view.adapter;

import static com.jack.bookshelf.utils.StringUtils.isTrimEmpty;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookKindBean;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.widget.image.CoverImageView;
import com.jack.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Item Choice Book Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ChoiceBookAdapter extends RefreshRecyclerViewAdapter {
    private final Activity activity;
    private final ArrayList<SearchBookBean> searchBooks;
    private Callback callback;

    public ChoiceBookAdapter(Activity activity) {
        super(true);
        this.activity = activity;
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_book, parent, false));
    }

    @Override
    public void onBindIViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        if (position >= searchBooks.size()) return;
        SearchBookBean book = searchBooks.get(position);
        if (!activity.isFinishing()) {
            myViewHolder.ivCover.load(book.getCoverUrl(), book.getName(), book.getCoverUrl());
        }
        // 名字与作者
        String title = book.getName();
        String author = book.getAuthor();
        if (author != null && author.trim().length() > 0)
            title = String.format("%s  (%s)", title, author);
        myViewHolder.tvName.setText(title);
        BookKindBean bookKindBean = new BookKindBean(book.getKind());
        // 状态
        if (isTrimEmpty(bookKindBean.getState())) {
            myViewHolder.tvState.setVisibility(View.GONE);
        } else {
            myViewHolder.tvState.setVisibility(View.VISIBLE);
            myViewHolder.tvState.setText(bookKindBean.getState());
        }
        // 类别
        myViewHolder.tvKind.setVisibility(View.VISIBLE);
        if (isTrimEmpty(bookKindBean.getKind()) || bookKindBean.getKind().equals("[]")) {
            myViewHolder.tvKind.setText(activity.getString(R.string.no_kind));
        } else {
            myViewHolder.tvKind.setText(bookKindBean.getKind());
        }
        // 字数
        if (isTrimEmpty(bookKindBean.getWordsS())) {
            myViewHolder.tvWords.setVisibility(View.GONE);
        } else {
            myViewHolder.tvWords.setVisibility(View.VISIBLE);
            myViewHolder.tvWords.setText(bookKindBean.getWordsS());
        }
        // 来源
        myViewHolder.tvOrigin.setVisibility(View.VISIBLE);
        if (isTrimEmpty(book.getOrigin())) {
            myViewHolder.tvOrigin.setText(activity.getString(R.string.origin_format, activity.getString(R.string.loading)));
        } else {
            myViewHolder.tvOrigin.setText(activity.getString(R.string.origin_format, searchBooks.get(position).getOrigin()));
        }
        // 最新章节
        myViewHolder.tvLasted.setVisibility(View.VISIBLE);
        if (isTrimEmpty(book.getLastChapter())) {
            myViewHolder.tvLasted.setText(activity.getString(R.string.book_search_last, activity.getString(R.string.unknown)));
        } else {
            myViewHolder.tvLasted.setText(activity.getString(R.string.book_search_last, book.getLastChapter()));
        }
        myViewHolder.flContent.setOnClickListener(v -> {
            if (callback != null)
                callback.clickItem(myViewHolder.ivCover, position, book);
        });
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBooks.size();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void addAll(List<SearchBookBean> newData) {
        if (newData != null && newData.size() > 0) {
            int position = getICount();
            if (newData.size() > 0) {
                searchBooks.addAll(newData);
            }
            notifyItemInserted(position);
            notifyItemRangeChanged(position, newData.size());
        }
    }

    public void replaceAll(List<SearchBookBean> newData) {
        searchBooks.clear();
        if (newData != null && newData.size() > 0) {
            searchBooks.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public interface Callback {
        void clickItem(View animView, int position, SearchBookBean searchBookBean);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ViewGroup flContent;
        CoverImageView ivCover;
        TextView tvName;
        TextView tvState;
        TextView tvWords;
        TextView tvKind;
        TextView tvLasted;
        TextView tvOrigin;

        MyViewHolder(View itemView) {
            super(itemView);
            flContent = itemView.findViewById(R.id.fl_content);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvState = itemView.findViewById(R.id.tv_state);
            tvWords = itemView.findViewById(R.id.tv_words);
            tvLasted = itemView.findViewById(R.id.tv_lasted);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
        }
    }
}
