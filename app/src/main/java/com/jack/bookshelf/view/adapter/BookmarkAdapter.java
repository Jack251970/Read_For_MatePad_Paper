package com.jack.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.BookmarkBean;
import com.jack.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Bookmark Item Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

@SuppressLint("NotifyDataSetChanged")
public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ThisViewHolder> {

    private final BookShelfBean bookShelfBean;
    private final OnItemClickListener itemClickListener;
    private List<BookmarkBean> allBookmark = new ArrayList<>();
    private final List<BookmarkBean> bookmarkBeans = new ArrayList<>();
    private boolean isSearch = false;

    public BookmarkAdapter(BookShelfBean bookShelfBean, @NonNull OnItemClickListener itemClickListener) {
        this.bookShelfBean = bookShelfBean;
        this.itemClickListener = itemClickListener;
    }

    public void setAllBookmark(List<BookmarkBean> allBookmark) {
        this.allBookmark = allBookmark;
        notifyDataSetChanged();
    }

    public void search(final String key) {
        bookmarkBeans.clear();
        if (Objects.equals(key, "")) {
            isSearch = false;
            notifyDataSetChanged();
        } else {
            Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                for (BookmarkBean bookmarkBean : allBookmark) {
                    if (bookmarkBean.getChapterName().contains(key)) {
                        bookmarkBeans.add(bookmarkBean);
                    } else if (bookmarkBean.getContent().contains(key)) {
                        bookmarkBeans.add(bookmarkBean);
                    }
                }
                emitter.onNext(true);
                emitter.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            isSearch = true;
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Throwable e) {}
                    });
        }
    }

    @NonNull
    @Override
    public ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThisViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, final int position) {}

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        int realPosition = holder.getLayoutPosition();
        if (realPosition == 0) {
            holder.lineTop.setVisibility(View.VISIBLE);
        } else if (realPosition == getItemCount() - 1) {
            holder.lineBottom.setVisibility(View.GONE);
        } else {
            holder.lineBottom.setVisibility(View.VISIBLE);
        }

        BookmarkBean bookmarkBean = isSearch ? bookmarkBeans.get(realPosition) : allBookmark.get(realPosition);
        holder.tvName.setText(StringUtils.isTrimEmpty(bookmarkBean.getContent()) ?
                bookmarkBean.getChapterName() : bookmarkBean.getContent());
        holder.llName.setOnClickListener(v ->
                itemClickListener.itemClick(bookmarkBean.getChapterIndex(), bookmarkBean.getPageIndex()));
        holder.llName.setOnLongClickListener(view -> {
            itemClickListener.itemLongClick(bookmarkBean);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        if (bookShelfBean == null)
            return 0;
        else {
            if (isSearch) {
                return bookmarkBeans.size();
            }
            return allBookmark.size();
        }
    }

    static class ThisViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final View lineTop;
        private final View lineBottom;
        private final View llName;

        ThisViewHolder(View itemView) {
            super(itemView);
            lineTop = itemView.findViewById(R.id.v_line_top);
            tvName = itemView.findViewById(R.id.tv_name);
            lineBottom = itemView.findViewById(R.id.v_line_bottom);
            llName = itemView.findViewById(R.id.ll_name);
        }
    }

    public interface OnItemClickListener {
        void itemClick(int index, int page);

        void itemLongClick(BookmarkBean bookmarkBean);
    }
}