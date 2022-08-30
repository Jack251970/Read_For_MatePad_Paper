package com.jack.bookshelf.view.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.BookChapterBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.utils.theme.ThemeStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Chapter List Item Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ThisViewHolder> {

    private final BookShelfBean bookShelfBean;
    private final OnItemClickListener itemClickListener;
    private final List<BookChapterBean> allChapter;
    private final List<BookChapterBean> bookChapterBeans = new ArrayList<>();
    private int index = 0;
    private boolean isSearch = false;
    private int normalColor;
    private final int highlightColor;

    public ChapterListAdapter(BookShelfBean bookShelfBean, List<BookChapterBean> allChapter, @NonNull OnItemClickListener itemClickListener) {
        this.bookShelfBean = bookShelfBean;
        this.allChapter = allChapter;
        this.itemClickListener = itemClickListener;
        highlightColor = Color.BLACK;
    }

    public void upChapter(int index) {
        if (allChapter.size() > index) {
            notifyItemChanged(index, 0);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void search(final String key) {
        bookChapterBeans.clear();
        if (Objects.equals(key, "")) {
            isSearch = false;
            notifyDataSetChanged();
        } else {
            Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                for (BookChapterBean bookChapterBean : allChapter) {
                    if (bookChapterBean.getDurChapterName().contains(key)) {
                        bookChapterBeans.add(bookChapterBean);
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
        normalColor = ThemeStore.textColorSecondary(parent.getContext());
        return new ThisViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter_and_bookmark, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, final int position) {}

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        int realPosition = holder.getLayoutPosition();
        if (realPosition == 0) {
            holder.lineTop.setVisibility(View.VISIBLE);
        } else {
            holder.lineBottom.setVisibility(View.VISIBLE);
        }
        /*if (payloads.size() > 0) {
            holder.tvName.setSelected(true);
            holder.tvName.getPaint().setFakeBoldText(true);
            return;
        }*/
        BookChapterBean bookChapterBean = isSearch ? bookChapterBeans.get(realPosition) : allChapter.get(realPosition);
        // 当前位置章节的字体颜色为黑色；其余为深黑色
        if (bookChapterBean.getDurChapterIndex() == index) {
            holder.tvName.setTextColor(highlightColor);
        } else {
            holder.tvName.setTextColor(normalColor);
        }
        // 设置章节名称
        holder.tvName.setText(bookChapterBean.getDisplayTitle(holder.tvName.getContext()));
        // 缓存章节的字体为粗体；其余为细体
        if (Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG) || bookChapterBean.getHasCache(bookShelfBean.getBookInfoBean())) {
            holder.tvName.setSelected(true);
            holder.tvName.getPaint().setFakeBoldText(true);
        } else {
            holder.tvName.setSelected(false);
            holder.tvName.getPaint().setFakeBoldText(false);
        }
        // 点击跳转到对应章节
        holder.llName.setOnClickListener(v -> {
            setIndex(realPosition);
            itemClickListener.itemClick(bookChapterBean.getDurChapterIndex(), 0);
        });
    }

    @Override
    public int getItemCount() {
        if (bookShelfBean == null || allChapter == null)
            return 0;
        else {
            if (isSearch) {
                return bookChapterBeans.size();
            }
            return allChapter.size();
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyItemChanged(this.index, 0);
    }

    static class ThisViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final View lineTop;
        private final View lineBottom;
        private final View llName;

        ThisViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            lineTop = itemView.findViewById(R.id.v_line_top);
            lineBottom = itemView.findViewById(R.id.v_line_bottom);
            llName = itemView.findViewById(R.id.ll_name);
        }
    }

    public interface OnItemClickListener {
        void itemClick(int index, int page);
    }
}