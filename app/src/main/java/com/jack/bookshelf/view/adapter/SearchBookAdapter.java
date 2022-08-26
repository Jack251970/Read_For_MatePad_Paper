package com.jack.bookshelf.view.adapter;

import static com.jack.bookshelf.utils.StringUtils.isTrimEmpty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookKindBean;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.view.adapter.base.BaseListAdapter;
import com.jack.bookshelf.widget.image.CoverImageView;
import com.jack.bookshelf.widget.recycler.refresh.RefreshRecyclerViewAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Item Search Book Adapter
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class SearchBookAdapter extends RefreshRecyclerViewAdapter {
    private final WeakReference<Activity> activityRef;
    private List<SearchBookBean> searchBooks;
    private BaseListAdapter.OnItemClickListener itemClickListener;

    public SearchBookAdapter(Activity activity) {
        super(true);
        this.activityRef = new WeakReference<>(activity);
        searchBooks = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateIViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_book, parent, false));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindIViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Activity activity = activityRef.get();
        holder.itemView.setBackgroundColor(ThemeStore.backgroundColor(activity));
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.flContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onItemClick(v, position);
        });
        SearchBookBean book = searchBooks.get(position);
        if (!activity.isFinishing()) {
            myViewHolder.ivCover.load(book.getCoverUrl(), book.getName(), book.getAuthor());
        }
        // 名字与作者
        myViewHolder.tvName.setText(String.format("%s  (%s)", book.getName(), book.getAuthor()));
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
            myViewHolder.tvOrigin.setText(activity.getString(R.string.origin_format, book.getOrigin()));
        }
        // 最新章节
        myViewHolder.tvLasted.setVisibility(View.VISIBLE);
        if (isTrimEmpty(book.getLastChapter())) {
            myViewHolder.tvLasted.setText(activity.getString(R.string.book_search_last, activity.getString(R.string.unknown)));
        } else {
            myViewHolder.tvLasted.setText(activity.getString(R.string.book_search_last, book.getLastChapter()));
        }
        // 源数
        myViewHolder.tvOriginNum.setText(String.format("共%d个源", book.getOriginNum()));
    }

    @Override
    public int getIViewType(int position) {
        return 0;
    }

    @Override
    public int getICount() {
        return searchBooks.size();
    }

    public void setItemClickListener(BaseListAdapter.OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public synchronized void upData(DataAction action, List<SearchBookBean> newDataS) {
        switch (action) {
            case ADD:
                searchBooks = newDataS;
                notifyDataSetChanged();
                break;
            case CLEAR:
                if (!searchBooks.isEmpty()) {
                    try {
                        Glide.with(activityRef.get()).onDestroy();
                    } catch (Exception ignored) {
                    }
                    searchBooks.clear();
                    notifyDataSetChanged();
                }
                break;
        }
    }

    public synchronized void addAll(List<SearchBookBean> newDataS, String keyWord) {
        List<SearchBookBean> copyDataS = new ArrayList<>(searchBooks);
        if (newDataS != null && newDataS.size() > 0) {
            saveData(newDataS);
            List<SearchBookBean> searchBookBeansAdd = new ArrayList<>();
            if (copyDataS.size() == 0) {
                copyDataS.addAll(newDataS);
            } else {
                //存在
                for (SearchBookBean temp : newDataS) {
                    boolean hasSame = false;
                    for (int i = 0, size = copyDataS.size(); i < size; i++) {
                        SearchBookBean searchBook = copyDataS.get(i);
                        if (TextUtils.equals(temp.getName(), searchBook.getName())
                                && TextUtils.equals(temp.getAuthor(), searchBook.getAuthor())) {
                            hasSame = true;
                            searchBook.addOriginUrl(temp.getTag());
                            break;
                        }
                    }

                    if (!hasSame) {
                        searchBookBeansAdd.add(temp);
                    }
                }
                //添加
                for (SearchBookBean temp : searchBookBeansAdd) {
                    if (TextUtils.equals(keyWord, temp.getName())) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else if (TextUtils.equals(keyWord, temp.getAuthor())) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else {
                        copyDataS.add(temp);
                    }
                }
            }
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(() -> upData(DataAction.ADD, copyDataS));
            }
        }
    }

    private void saveData(List<SearchBookBean> data) {
        AsyncTask.execute(() -> DbHelper.getDaoSession().getSearchBookBeanDao().insertOrReplaceInTx(data));
    }

    public SearchBookBean getItemData(int pos) {
        return searchBooks.get(pos);
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
        TextView tvOriginNum;

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
            tvOriginNum = itemView.findViewById(R.id.tv_origin_num);
        }
    }

    public enum DataAction {
        ADD, CLEAR
    }
}