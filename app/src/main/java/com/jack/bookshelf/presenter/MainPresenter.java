package com.jack.bookshelf.presenter;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.jack.basemvplib.BasePresenterImpl;
import com.jack.basemvplib.impl.IView;
import com.jack.bookshelf.DbHelper;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.BookChapterBean;
import com.jack.bookshelf.bean.BookInfoBean;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.dao.BookSourceBeanDao;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.model.BookSourceManager;
import com.jack.bookshelf.model.WebBookModel;
import com.jack.bookshelf.presenter.contract.MainContract;
import com.jack.bookshelf.utils.RxUtils;
import com.jack.bookshelf.utils.StringUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Main Presenter
 * Copyright (c) 2017. 章钦豪. All rights reserved.
 * Edited by Jack251970
 */

public class MainPresenter extends BasePresenterImpl<MainContract.View> implements MainContract.Presenter {
    /**
     * @param bookUrls 如果不包含书源，一行为一本小说的地址。如果包含书源，只解析为一本数，以免url#{{书源}}中书源包含换行
     */
    @Override
    public void addBookUrl(String bookUrls) {
        bookUrls = bookUrls.trim();
        if (TextUtils.isEmpty(bookUrls)) return;

        String[] urls;
        if (bookUrls.matches("[^\n]+#\\{[\\s\\S]+")) {
            urls = new String[]{bookUrls};
        } else {
            urls = bookUrls.split("\\n");
        }

        Observable.fromArray(urls)
                .flatMap(this::addBookUrlO)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onNext(@NonNull BookShelfBean bookShelfBean) {
                        getBook(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast(e.getMessage());
                    }
                });
    }

    private Observable<BookShelfBean> addBookUrlO(String bookUrl) {
        return Observable.create(e -> {
            if (StringUtils.isTrimEmpty(bookUrl)) {
                e.onComplete();
                return;
            }

            String source = "";
            String url = bookUrl;
            if (url.replaceAll("(\\s|\n)*", "").matches("^.*(#\\{).*")) {
                String[] string = bookUrl.split("#\\{", 2);
                source = StringUtils.unCompressJson(string[1]);
                if (StringUtils.isJsonType(source))
                    url = string[0];
                else
                    source = "";
            }

            BookInfoBean temp = DbHelper.getDaoSession().getBookInfoBeanDao().load(url);
            if (temp != null) {
                e.onError(new Throwable(StringUtils.getString(R.string.already_in_bookshelf)));
                return;
            } else {
                String baseUrl = StringUtils.getBaseUrl(url);
                BookSourceBean bookSourceBean = DbHelper.getDaoSession().getBookSourceBeanDao().load(baseUrl);

                // RuleBookUrlPattern推定  考虑有书源规则不完善，需要排除RuleBookUrlPattern填写.*匹配全部url的情况
                if (bookSourceBean == null) {
                    List<BookSourceBean> sourceBeans = DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder()
                            .where(BookSourceBeanDao.Properties.RuleBookUrlPattern.isNotNull()
                                    , BookSourceBeanDao.Properties.RuleBookUrlPattern.notEq("")
                                    , BookSourceBeanDao.Properties.RuleBookUrlPattern.notEq(".*")
                            ).list();
                    for (BookSourceBean sourceBean : sourceBeans) {
                        if (url.matches(sourceBean.getRuleBookUrlPattern())) {
                            bookSourceBean = sourceBean;
                            break;
                        }
                    }
                }

                // BookSourceUrl推定  考虑有书源规则不完善，没有填写RuleBookUrlPattern的情况（但是通常会填写bookSourceUrl），因此需要做补充
                if (bookSourceBean == null) {
                    String siteUrl = url.replaceFirst("^(http://|https://)?(m\\.|www\\.|web\\.)?", "").replaceFirst("/.*$", "");
                    List<BookSourceBean> sourceBeans = DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder()
                            .where(BookSourceBeanDao.Properties.BookSourceUrl.like("%" + siteUrl + "%")).list();
                    for (BookSourceBean sourceBean : sourceBeans) {
                        // 由于RuleBookUrlPattern推定排除了RuleBookUrlPattern为空或者匹配所有字符的情况，因此需要做过杀推定
                        if (sourceBean.getRuleBookUrlPattern() == null) {
                            bookSourceBean = sourceBean;
                            break;
                        } else if (sourceBean.getRuleBookUrlPattern().replaceAll("\\s", "").length() == 0) {
                            bookSourceBean = sourceBean;
                            break;
                        }
                        if (url.matches(sourceBean.getRuleBookUrlPattern())) {
                            bookSourceBean = sourceBean;
                            break;
                        }
                    }
                }
                BookShelfBean bookShelfBean = new BookShelfBean();
                bookShelfBean.setNoteUrl(url);
                if (bookSourceBean != null) {
                    bookShelfBean.setTag(bookSourceBean.getBookSourceUrl());
                    bookShelfBean.setDurChapter(0);
                    bookShelfBean.setGroup(mView.getGroup() % 4);
                    bookShelfBean.setDurChapterPage(0);
                    bookShelfBean.setFinalDate(System.currentTimeMillis());
                    e.onNext(bookShelfBean);
                } else {
                    if (source.length() > 10) {
                        Observable<List<BookSourceBean>> observable = BookSourceManager.importSource(source);
                        if (observable != null) {
                            observable.subscribe(new MyObserver<>() {
                                @SuppressLint("DefaultLocale")
                                @Override
                                public void onNext(@NonNull List<BookSourceBean> bookSourceBeans) {
                                    if (bookSourceBeans.size() == 1) {
                                        BookSourceBean bean = (bookSourceBeans.get(0));
                                        bookShelfBean.setTag(bean.getBookSourceUrl());
                                        bookShelfBean.setDurChapter(0);
                                        bookShelfBean.setGroup(mView.getGroup() % 4);
                                        bookShelfBean.setDurChapterPage(0);
                                        bookShelfBean.setFinalDate(System.currentTimeMillis());
                                        getBook(bookShelfBean);
                                    } else {
                                        e.onError(new Throwable("未导入内嵌的书源-" + bookSourceBeans.size()));
                                    }
                                }
                            });
                        } else {
                            e.onError(new Throwable("未找到内嵌的书源"));
                        }
                    }

                    e.onError(new Throwable("未找到对应书源"));
                    return;
                }
            }
            e.onComplete();
        });
    }

    private void getBook(BookShelfBean bookShelfBean) {
        WebBookModel.getInstance()
                .getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModel.getInstance().getChapterList(bookShelfBean1))
                .flatMap(chapterBeanList -> saveBookToShelfO(bookShelfBean, chapterBeanList))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onNext(@NonNull BookShelfBean value) {
                        if (value.getBookInfoBean().getChapterUrl() == null) {
                            mView.toast(R.string.add_book_fail);
                        } else {
                            // 成功 发送RxBus
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelfBean);
                            mView.toast(R.string.add_book_success);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast(StringUtils.getString(R.string.add_book_fail) + e.getMessage());
                    }
                });
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean, List<BookChapterBean> chapterBeanList) {
        return Observable.create(e -> {
            BookshelfHelp.saveBookToShelf(bookShelfBean);
            DbHelper.getDaoSession().getBookChapterBeanDao().insertOrReplaceInTx(chapterBeanList);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.IMMERSION_CHANGE)})
    public void initImmersionBar(Boolean immersion) {
        mView.initImmersionBar();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.RECREATE)})
    public void recreate(Boolean recreate) {
        mView.recreate();
    }

}