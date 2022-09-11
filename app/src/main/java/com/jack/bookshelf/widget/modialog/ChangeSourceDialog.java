package com.jack.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.dao.SearchBookBeanDao;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.model.BookSourceManager;
import com.jack.bookshelf.model.SearchBookModel;
import com.jack.bookshelf.model.UpLastChapterModel;
import com.jack.bookshelf.utils.ScreenUtils;
import com.jack.bookshelf.utils.StringUtils;
import com.jack.bookshelf.utils.ToastsKt;
import com.jack.bookshelf.view.activity.SourceEditActivity;
import com.jack.bookshelf.view.dialog.adapter.ChangeSourceAdapter;
import com.jack.bookshelf.view.popupwindow.MoreSettingMenu;
import com.jack.bookshelf.widget.recycler.refresh.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Change Source Dialog
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class ChangeSourceDialog extends BaseDialog implements ChangeSourceAdapter.CallBack {
    private final Context context;
    private View llContent;
    private TextView atvTitle;
    private ImageButton ibtStop;
    private SearchView searchView;
    private RefreshRecyclerView rvSource;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ChangeSourceAdapter adapter;
    private SearchBookModel searchBookModel;
    private BookShelfBean book;
    private String bookTag;
    private String bookName;
    private String bookAuthor;
    private int shelfLastChapter;
    private CompositeDisposable compositeDisposable;
    private Callback callback;

    public static ChangeSourceDialog builder(Context context, BookShelfBean bookShelfBean) {
        return new ChangeSourceDialog(context, bookShelfBean);
    }

    private ChangeSourceDialog(@NonNull Context context, BookShelfBean bookShelfBean) {
        super(context, R.style.PaperAlertDialogTheme);
        this.context = context;
        init(bookShelfBean);
    }

    private void init(BookShelfBean bookShelf) {
        this.book = bookShelf;
        compositeDisposable = new CompositeDisposable();
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_change_source, null);
        bindView(view);
        bindEvent();
        setContentView(view);
        initData();
    }

    private void bindView(View view) {
        llContent = view.findViewById(R.id.ll_content_change_source_dialog);
        searchView = view.findViewById(R.id.searchView);
        initSearchView(searchView);
        atvTitle = view.findViewById(R.id.tv_title_change_source_dialog);
        ibtStop = view.findViewById(R.id.ibt_stop);
        rvSource = view.findViewById(R.id.rf_rv_change_source);
    }

    /**
     * 初始化搜索框
     */
    private void initSearchView(SearchView searchView) {
        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setImageResource(R.drawable.ic_close);
        searchView.onActionViewExpanded();
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (StringUtils.isTrimEmpty(newText)) {
                    List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                            .where(SearchBookBeanDao.Properties.Name.eq(bookName), SearchBookBeanDao.Properties.Author.eq(bookAuthor))
                            .build().list();
                    adapter.reSetSourceAdapter();
                    adapter.addAllSourceAdapter(searchBookBeans);
                } else {
                    List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                            .where(SearchBookBeanDao.Properties.Name.eq(bookName), SearchBookBeanDao.Properties.Author.eq(bookAuthor), SearchBookBeanDao.Properties.Origin.like("%" + searchView.getQuery().toString() + "%"))
                            .build().list();
                    adapter.reSetSourceAdapter();
                    adapter.addAllSourceAdapter(searchBookBeans);
                }
                return false;
            }
        });
    }

    private void bindEvent() {
        llContent.setOnClickListener(null);
        rvSource.addItemDecoration(new DividerItemDecoration(context, LinearLayout.VERTICAL));
        rvSource.setBaseRefreshListener(this::reSearchBook);
        ibtStop.setOnClickListener(v -> stopChangeSource());
    }

    @Override
    public void changeTo(SearchBookBean searchBookBean) {
        if (!Objects.equals(book.getNoteUrl(), searchBookBean.getNoteUrl())) {
            callback.changeSource(searchBookBean);
        }
        dismiss();
    }

    @Override
    public void showMenu(View anchorView, SearchBookBean searchBookBean) {
        final String url = searchBookBean.getTag();
        final BookSourceBean sourceBean = BookSourceManager.getBookSourceByUrl(url);
        MoreSettingMenu.builder(context)
                .setMenu(R.array.more_setting_menu_change_source_dialog, R.array.icon_more_setting_menu_change_source_dialog)
                .setOnclick(position -> {
                    switch (position) {
                        case 0:
                            Objects.requireNonNull(sourceBean).setEnable(false);
                            BookSourceManager.addBookSource(sourceBean);
                            adapter.removeData(searchBookBean);
                            ToastsKt.toast(context, context.getString(R.string.have_disabled,sourceBean.getBookSourceName()), Toast.LENGTH_SHORT);
                            break;
                        case 1:
                            BookSourceManager.removeBookSource(sourceBean);
                            adapter.removeData(searchBookBean);
                            ToastsKt.toast(context, context.getString(R.string.have_deleted, Objects.requireNonNull(sourceBean).getBookSourceName()), Toast.LENGTH_SHORT);
                            break;
                        case 2:
                            SourceEditActivity.startThis(context, Objects.requireNonNull(sourceBean));
                            break;
                    }
                })
                .showForChangeSourceDialog(llContent, anchorView);
    }

    @SuppressLint("InflateParams")
    private void initData() {
        adapter = new ChangeSourceAdapter(false);
        rvSource.setRefreshRecyclerViewAdapter(adapter, new LinearLayoutManager(context));
        adapter.setCallBack(this);
        View viewRefreshError = LayoutInflater.from(context).inflate(R.layout.view_refresh_error, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(v -> reSearchBook());
        rvSource.setNoDataAndRefreshErrorView(LayoutInflater.from(context).inflate(R.layout.view_refresh_no_data, null),
                viewRefreshError);
        SearchBookModel.OnSearchListener searchListener = new SearchBookModel.OnSearchListener() {
            @Override
            public void refreshSearchBook() {
                ibtStop.setVisibility(View.VISIBLE);
                adapter.reSetSourceAdapter();
            }

            @Override
            public void refreshFinish(Boolean value) {
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(true, true);
            }

            @Override
            public void loadMoreFinish(Boolean value) {
                ibtStop.setVisibility(View.INVISIBLE);
                rvSource.finishRefresh(true);
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                addSearchBook(value);
            }

            @Override
            public void searchBookError(Throwable throwable) {
                ibtStop.setVisibility(View.INVISIBLE);
                if (adapter.getICount() == 0) {
                    rvSource.refreshError();
                }
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        };
        searchBookModel = new SearchBookModel(searchListener);
        bookTag = book.getTag();
        bookName = book.getBookInfoBean().getName();
        bookAuthor = book.getBookInfoBean().getAuthor();
        shelfLastChapter = BookshelfHelp.guessChapterNum(book.getLastChapterName());
        atvTitle.setText(String.format("%s (%s)", bookName, bookAuthor));
        rvSource.startRefresh();
        getSearchBookInDb(book);
        RxBus.get().register(this);
        setOnDismissListener(dialog -> {
            RxBus.get().unregister(ChangeSourceDialog.this);
            compositeDisposable.dispose();
            if (searchBookModel != null) {
                searchBookModel.onDestroy();
            }
        });
    }

    public ChangeSourceDialog setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void show() {
        super.show();
        WindowManager.LayoutParams params = Objects.requireNonNull(getWindow()).getAttributes();
        params.height = ScreenUtils.getAppSize()[1] - 60;
        params.width = ScreenUtils.getAppSize()[0] - 60;
        getWindow().setAttributes(params);
    }

    private void getSearchBookInDb(BookShelfBean bookShelf) {
        Single.create((SingleOnSubscribe<List<SearchBookBean>>) e -> {
            List<SearchBookBean> searchBookBeans = DbHelper.getDaoSession().getSearchBookBeanDao().queryBuilder()
                    .where(SearchBookBeanDao.Properties.Name.eq(bookName), SearchBookBeanDao.Properties.Author.eq(bookAuthor)).build().list();
            if (searchBookBeans == null) searchBookBeans = new ArrayList<>();
            List<SearchBookBean> searchBookList = new ArrayList<>();
            List<BookSourceBean> bookSourceList = new ArrayList<>(BookSourceManager.getSelectedBookSource());
            if (bookSourceList.size() > 0) {
                for (BookSourceBean bookSourceBean : BookSourceManager.getSelectedBookSource()) {
                    boolean hasSource = false;
                    for (SearchBookBean searchBookBean : new ArrayList<>(searchBookBeans)) {
                        if (Objects.equals(searchBookBean.getTag(), bookSourceBean.getBookSourceUrl())) {
                            bookSourceList.remove(bookSourceBean);
                            searchBookList.add(searchBookBean);
                            hasSource = true;
                            break;
                        }
                    }
                    if (hasSource) {
                        bookSourceList.remove(bookSourceBean);
                    }
                }
                searchBookModel.searchReNew();
                searchBookModel.initSearchEngineS(bookSourceList);
                long startThisSearchTime = System.currentTimeMillis();
                searchBookModel.setSearchTime(startThisSearchTime);
                List<BookShelfBean> bookList = new ArrayList<>();
                bookList.add(book);
                searchBookModel.search(bookName, startThisSearchTime, bookList, false);
                UpLastChapterModel.getInstance().startUpdate(searchBookList);
            }
            if (searchBookList.size() > 0) {
                for (SearchBookBean searchBookBean : searchBookList) {
                    searchBookBean.setIsCurrentSource(searchBookBean.getTag().equals(bookShelf.getTag()));
                }
                searchBookList.sort(this::compareSearchBooks);
            }
            e.onSuccess(searchBookList);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<SearchBookBean> searchBookBeans) {
                        if (searchBookBeans.size() > 0) {
                            adapter.addAllSourceAdapter(searchBookBeans);
                            ibtStop.setVisibility(View.INVISIBLE);
                            rvSource.finishRefresh(true, true);
                        } else {
                            reSearchBook();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        reSearchBook();
                    }
                });
    }

    private void reSearchBook() {
        rvSource.startRefresh();
        searchBookModel.initSearchEngineS(BookSourceManager.getSelectedBookSource());
        searchBookModel.searchReNew();
        long startThisSearchTime = System.currentTimeMillis();
        searchBookModel.setSearchTime(startThisSearchTime);
        List<BookShelfBean> bookList = new ArrayList<>();
        bookList.add(book);
        searchBookModel.search(bookName, startThisSearchTime, bookList, false);
    }

    private synchronized void addSearchBook(List<SearchBookBean> value) {
        if (value.size() > 0) {
            value.sort(this::compareSearchBooks);
            for (SearchBookBean searchBookBean : value) {
                if (searchBookBean.getName().equals(bookName)
                        && (searchBookBean.getAuthor().equals(bookAuthor) || TextUtils.isEmpty(searchBookBean.getAuthor()) || TextUtils.isEmpty(bookAuthor))) {
                    searchBookBean.setIsCurrentSource(searchBookBean.getTag().equals(bookTag));
                    boolean saveBookSource = false;
                    BookSourceBean bookSourceBean = BookSourceManager.getBookSourceByUrl(searchBookBean.getTag());
                    if (searchBookBean.getSearchTime() < 60 && bookSourceBean != null) {
                        bookSourceBean.increaseWeight(100 / (10 + searchBookBean.getSearchTime()));
                        saveBookSource = true;
                    }
                    if (shelfLastChapter > 0 && bookSourceBean != null) {
                        int lastChapter = BookshelfHelp.guessChapterNum(searchBookBean.getLastChapter());
                        if (lastChapter > shelfLastChapter) {
                            bookSourceBean.increaseWeight(100);
                            saveBookSource = true;
                        }
                    }
                    if (saveBookSource) {
                        DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
                    }
                    DbHelper.getDaoSession().getSearchBookBeanDao().insertOrReplace(searchBookBean);
                    if (StringUtils.isTrimEmpty(searchView.getQuery().toString()) || searchBookBean.getOrigin().equals(searchView.getQuery().toString())) {
                        handler.post(() -> adapter.addSourceAdapter(searchBookBean));
                    }
                    break;
                }
            }
        }
    }

    private int compareSearchBooks(SearchBookBean s1, SearchBookBean s2) {
        boolean s1tag = s1.getTag().equals(bookTag);
        boolean s2tag = s2.getTag().equals(bookTag);
        if (s2tag && !s1tag)
            return 1;
        else if (s1tag && !s2tag)
            return -1;
        int result = Long.compare(s2.getAddTime(), s1.getAddTime());
        if (result != 0)
            return result;
        result = Integer.compare(s2.getLastChapterNum(), s1.getLastChapterNum());
        if (result != 0)
            return result;
        return Integer.compare(s2.getWeight(), s1.getWeight());
    }

    private void stopChangeSource() {
        compositeDisposable.dispose();
        if (searchBookModel != null) {
            searchBookModel.stopSearch();
        }
    }

    public interface Callback {
        void changeSource(SearchBookBean searchBookBean);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.UP_SEARCH_BOOK)})
    public void upSearchBook(SearchBookBean searchBookBean) {
        if (!Objects.equals(book.getBookInfoBean().getName(), searchBookBean.getName())
                || !Objects.equals(book.getBookInfoBean().getAuthor(), searchBookBean.getAuthor())) {
            return;
        }
        for (int i = 0; i < adapter.getSearchBookBeans().size(); i++) {
            if (adapter.getSearchBookBeans().get(i).getTag().equals(searchBookBean.getTag())
                    && !adapter.getSearchBookBeans().get(i).getLastChapter().equals(searchBookBean.getLastChapter())) {
                adapter.getSearchBookBeans().get(i).setLastChapter(searchBookBean.getLastChapter());
                adapter.notifyItemChanged(i);
            }
        }
    }
}