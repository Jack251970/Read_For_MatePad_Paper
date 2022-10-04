package com.jack.bookshelf.model.content;

import static android.text.TextUtils.isEmpty;

import android.text.TextUtils;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.BookShelfBean;
import com.jack.bookshelf.bean.BookSourceBean;
import com.jack.bookshelf.bean.SearchBookBean;
import com.jack.bookshelf.model.analyzeRule.AnalyzeByRegex;
import com.jack.bookshelf.model.analyzeRule.AnalyzeRule;
import com.jack.bookshelf.utils.NetworkUtils;
import com.jack.bookshelf.utils.StringUtils;

import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import retrofit2.Response;

class BookList {
    private final String tag;
    private final String sourceName;
    private final BookSourceBean bookSourceBean;
    private final boolean isFind;
    //规则
    private String ruleList;
    private String ruleName;
    private String ruleAuthor;
    private String ruleKind;
    private String ruleIntroduce;
    private String ruleLastChapter;
    private String ruleCoverUrl;
    private String ruleNoteUrl;

    BookList(String tag, String sourceName, BookSourceBean bookSourceBean, boolean isFind) {
        this.tag = tag;
        this.sourceName = sourceName;
        this.bookSourceBean = bookSourceBean;
        this.isFind = isFind;
    }

    Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            String baseUrl;
            baseUrl = NetworkUtils.getUrl(response);
            if (TextUtils.isEmpty(response.body())) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_web_content_error, baseUrl)));
                return;
            } else {
                Debug.printLog(tag, StringUtils.getString(R.string.get_search_result_success));
                Debug.printLog(tag, "└" + baseUrl);
            }
            String body = response.body();
            List<SearchBookBean> books = new ArrayList<>();
            AnalyzeRule analyzer = new AnalyzeRule(null, bookSourceBean);
            analyzer.setContent(body, baseUrl);
            //如果符合详情页url规则
            if (!isEmpty(bookSourceBean.getRuleBookUrlPattern())
                    && baseUrl.matches(bookSourceBean.getRuleBookUrlPattern())) {
                Debug.printLog(tag, StringUtils.getString(R.string.search_result_is_info_page));
                SearchBookBean item = getItem(analyzer, baseUrl);
                if (item != null) {
                    item.setBookInfoHtml(body);
                    books.add(item);
                }
            } else {
                initRule();
                List<Object> collections;
                boolean reverse = false;
                boolean allInOne = false;
                if (ruleList.startsWith("-")) {
                    reverse = true;
                    ruleList = ruleList.substring(1);
                }
                // 仅使用java正则表达式提取书籍列表
                if (ruleList.startsWith(":")) {
                    ruleList = ruleList.substring(1);
                    Debug.printLog(tag, StringUtils.getString(R.string.parse_search_list));
                    getBooksOfRegex(body, ruleList.split("&&"), 0, analyzer, books);
                } else {
                    if (ruleList.startsWith("+")) {
                        allInOne = true;
                        ruleList = ruleList.substring(1);
                    }
                    //获取列表
                    Debug.printLog(tag, StringUtils.getString(R.string.parse_search_list));
                    collections = analyzer.getElements(ruleList);
                    if (collections.size() == 0 && isEmpty(bookSourceBean.getRuleBookUrlPattern())) {
                        Debug.printLog(tag, StringUtils.getString(R.string.search_list_empty_process_as_info_page));
                        SearchBookBean item = getItem(analyzer, baseUrl);
                        if (item != null) {
                            item.setBookInfoHtml(body);
                            books.add(item);
                        }
                    } else {
                        Debug.printLog(tag, String.format(StringUtils.getString(R.string.find_matching_result), collections.size()));
                        if (allInOne) {
                            for (int i = 0; i < collections.size(); i++) {
                                Object object = collections.get(i);
                                SearchBookBean item = getItemAllInOne(analyzer, object, baseUrl, i == 0);
                                if (item != null) {
                                    //如果网址相同则缓存
                                    if (baseUrl.equals(item.getNoteUrl())) {
                                        item.setBookInfoHtml(body);
                                    }
                                    books.add(item);
                                }
                            }
                        } else {
                            for (int i = 0; i < collections.size(); i++) {
                                Object object = collections.get(i);
                                analyzer.setContent(object, baseUrl);
                                SearchBookBean item = getItemInList(analyzer, baseUrl, i == 0);
                                if (item != null) {
                                    // 如果网址相同则缓存
                                    if (baseUrl.equals(item.getNoteUrl())) {
                                        item.setBookInfoHtml(body);
                                    }
                                    books.add(item);
                                }
                            }
                        }
                    }
                }
                if (books.size() > 1 && reverse) {
                    Collections.reverse(books);
                }
            }
            if (books.isEmpty()) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.no_book_name)));
                return;
            }
            Debug.printLog(tag, StringUtils.getString(R.string.finish_parse_book_list));
            e.onNext(books);
            e.onComplete();
        });
    }

    private void initRule() {
        if (isFind && !TextUtils.isEmpty(bookSourceBean.getRuleFindList())) {
            ruleList = bookSourceBean.getRuleFindList();
            ruleName = bookSourceBean.getRuleFindName();
            ruleAuthor = bookSourceBean.getRuleFindAuthor();
            ruleKind = bookSourceBean.getRuleFindKind();
            ruleIntroduce = bookSourceBean.getRuleFindIntroduce();
            ruleCoverUrl = bookSourceBean.getRuleFindCoverUrl();
            ruleLastChapter = bookSourceBean.getRuleFindLastChapter();
            ruleNoteUrl = bookSourceBean.getRuleFindNoteUrl();
        } else {
            ruleList = bookSourceBean.getRuleSearchList();
            ruleName = bookSourceBean.getRuleSearchName();
            ruleAuthor = bookSourceBean.getRuleSearchAuthor();
            ruleKind = bookSourceBean.getRuleSearchKind();
            ruleIntroduce = bookSourceBean.getRuleSearchIntroduce();
            ruleCoverUrl = bookSourceBean.getRuleSearchCoverUrl();
            ruleLastChapter = bookSourceBean.getRuleSearchLastChapter();
            ruleNoteUrl = bookSourceBean.getRuleSearchNoteUrl();
        }
    }

    /**
     * 详情页
     */
    private SearchBookBean getItem(AnalyzeRule analyzer, String baseUrl) throws Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        item.setTag(tag);
        item.setOrigin(sourceName);
        item.setNoteUrl(baseUrl);
        // 获取详情页预处理规则
        String ruleInfoInit = bookSourceBean.getRuleBookInfoInit();
        if (!isEmpty(ruleInfoInit)) {
            // 仅使用java正则表达式提取书籍详情
            if (ruleInfoInit.startsWith(":")) {
                ruleInfoInit = ruleInfoInit.substring(1);
                Debug.printLog(tag, StringUtils.getString(R.string.preprocess_info));
                BookShelfBean bookShelfBean = new BookShelfBean();
                bookShelfBean.setTag(tag);
                bookShelfBean.setNoteUrl(baseUrl);
                AnalyzeByRegex.getInfoOfRegex(String.valueOf(analyzer.getContent()), ruleInfoInit.split("&&"), 0, bookShelfBean, analyzer, bookSourceBean, tag);
                if (isEmpty(bookShelfBean.getBookInfoBean().getName())) return null;
                item.setName(bookShelfBean.getBookInfoBean().getName());
                item.setAuthor(bookShelfBean.getBookInfoBean().getAuthor());
                item.setCoverUrl(bookShelfBean.getBookInfoBean().getCoverUrl());
                item.setLastChapter(bookShelfBean.getLastChapterName());
                item.setIntroduce(bookShelfBean.getBookInfoBean().getIntroduce());
                return item;
            } else {
                Object object = analyzer.getElement(ruleInfoInit);
                if (object != null) {
                    analyzer.setContent(object);
                }
            }
        }
        Debug.printLog(tag, StringUtils.getString(R.string.book_url) + baseUrl);
        Debug.printLog(tag, StringUtils.getString(R.string.get_book_name));
        String bookName = StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookName()));
        Debug.printLog(tag, "└" + bookName);
        if (!TextUtils.isEmpty(bookName)) {
            item.setName(bookName);
            Debug.printLog(tag, StringUtils.getString(R.string.get_book_author));
            item.setAuthor(StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookAuthor())));
            Debug.printLog(tag, "└" + item.getAuthor());
            Debug.printLog(tag, StringUtils.getString(R.string.get_book_kind));
            item.setKind(analyzer.getString(bookSourceBean.getRuleBookKind()));
            Debug.printLog(tag, 111, "└" + item.getKind());
            Debug.printLog(tag, StringUtils.getString(R.string.get_latest_chapter));
            item.setLastChapter(analyzer.getString(bookSourceBean.getRuleBookLastChapter()));
            Debug.printLog(tag, "└" + item.getLastChapter());
            Debug.printLog(tag, StringUtils.getString(R.string.get_book_introduction));
            item.setIntroduce(analyzer.getString(bookSourceBean.getRuleIntroduce()));
            Debug.printLog(tag, 1, "└" + item.getIntroduce(), true, true);
            Debug.printLog(tag, StringUtils.getString(R.string.get_book_cover));
            item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleCoverUrl(), true));
            Debug.printLog(tag, "└" + item.getCoverUrl());
            return item;
        }
        return null;
    }

    private SearchBookBean getItemAllInOne(AnalyzeRule analyzer, Object object, String baseUrl, boolean printLog) {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        NativeObject nativeObject = (NativeObject) object;
        Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_name), printLog);
        String bookName = StringUtils.formatHtml(String.valueOf(nativeObject.get(ruleName)));
        Debug.printLog(tag, 1, "└" + bookName, printLog);
        if (!isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(sourceName);
            item.setName(bookName);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_author), printLog);
            item.setAuthor(StringUtils.formatHtml(String.valueOf(nativeObject.get(ruleAuthor))));
            Debug.printLog(tag, 1, "└" + item.getAuthor(), printLog);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_kind), printLog);
            item.setKind(String.valueOf(nativeObject.get(ruleKind)));
            Debug.printLog(tag, 111, "└" + item.getKind(), printLog);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_latest_chapter), printLog);
            item.setLastChapter(String.valueOf(nativeObject.get(ruleLastChapter)));
            Debug.printLog(tag, 1, "└" + item.getLastChapter(), printLog);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_introduction), printLog);
            item.setIntroduce(String.valueOf(nativeObject.get(ruleIntroduce)));
            Debug.printLog(tag, 1, "└" + item.getIntroduce(), printLog, true);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_cover), printLog);
            if (!isEmpty(ruleCoverUrl))
                item.setCoverUrl(NetworkUtils.getAbsoluteURL(baseUrl, String.valueOf(nativeObject.get(ruleCoverUrl))));
            Debug.printLog(tag, 1, "└" + item.getCoverUrl(), printLog);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_url), printLog);
            String resultUrl = String.valueOf(nativeObject.get(ruleNoteUrl));
            if (isEmpty(resultUrl)) resultUrl = baseUrl;
            item.setNoteUrl(resultUrl);
            Debug.printLog(tag, 1, "└" + item.getNoteUrl(), printLog);
            return item;
        }
        return null;
    }

    private SearchBookBean getItemInList(AnalyzeRule analyzer, String baseUrl, boolean printLog) throws
            Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_name), printLog);
        String bookName = StringUtils.formatHtml(analyzer.getString(ruleName));
        Debug.printLog(tag, 1, "└" + bookName, printLog);
        if (!TextUtils.isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(sourceName);
            item.setName(bookName);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_author), printLog);
            item.setAuthor(StringUtils.formatHtml(analyzer.getString(ruleAuthor)));
            Debug.printLog(tag, 1, "└" + item.getAuthor(), printLog);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_kind), printLog);
            item.setKind(analyzer.getString(ruleKind));
            Debug.printLog(tag, 111, "└" + item.getKind(), printLog);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_latest_chapter), printLog);
            item.setLastChapter(analyzer.getString(ruleLastChapter));
            Debug.printLog(tag, 1, "└" + item.getLastChapter(), printLog);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_introduction), printLog);
            item.setIntroduce(analyzer.getString(ruleIntroduce));
            Debug.printLog(tag, 1, "└" + item.getIntroduce(), printLog, true);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_cover), printLog);
            item.setCoverUrl(analyzer.getString(ruleCoverUrl, true));
            Debug.printLog(tag, 1, "└" + item.getCoverUrl(), printLog);
            Debug.printLog(tag, 1, StringUtils.getString(R.string.get_book_url), printLog);
            String resultUrl = analyzer.getString(ruleNoteUrl, true);
            if (isEmpty(resultUrl)) resultUrl = baseUrl;
            item.setNoteUrl(resultUrl);
            Debug.printLog(tag, 1, "└" + item.getNoteUrl(), printLog);
            return item;
        }
        return null;
    }

    // 纯java模式正则表达式获取书籍列表
    private void getBooksOfRegex(String res, String[] regs,
                                 int index, AnalyzeRule analyzer, final List<SearchBookBean> books) throws Exception {
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        String baseUrl = analyzer.getBaseUrl();
        // 判断规则是否有效,当搜索列表规则无效时当作详情页处理
        if (!resM.find()) {
            books.add(getItem(analyzer, baseUrl));
            return;
        }
        // 判断索引的规则是最后一个规则
        if (index + 1 == regs.length) {
            // 获取规则列表
            HashMap<String, String> ruleMap = new HashMap<>();
            ruleMap.put("ruleName", ruleName);
            ruleMap.put("ruleAuthor", ruleAuthor);
            ruleMap.put("ruleKind", ruleKind);
            ruleMap.put("ruleLastChapter", ruleLastChapter);
            ruleMap.put("ruleIntroduce", ruleIntroduce);
            ruleMap.put("ruleCoverUrl", ruleCoverUrl);
            ruleMap.put("ruleNoteUrl", ruleNoteUrl);
            // 分离规则参数
            List<String> ruleName = new ArrayList<>();
            List<List<String>> ruleParams = new ArrayList<>();  // 创建规则参数容器
            List<List<Integer>> ruleTypes = new ArrayList<>();  // 创建规则类型容器
            List<Boolean> hasVarParams = new ArrayList<>();     // 创建put&get标志容器
            for (String key : ruleMap.keySet()) {
                String val = ruleMap.get(key);
                ruleName.add(key);
                hasVarParams.add(!TextUtils.isEmpty(val) && (Objects.requireNonNull(val).contains("@put") || val.contains("@get")));
                List<String> ruleParam = new ArrayList<>();
                List<Integer> ruleType = new ArrayList<>();
                AnalyzeByRegex.splitRegexRule(val, ruleParam, ruleType);
                ruleParams.add(ruleParam);
                ruleTypes.add(ruleType);
            }
            // 提取书籍列表
            do {
                // 新建书籍容器
                SearchBookBean item = new SearchBookBean(tag, sourceName);
                analyzer.setBook(item);
                // 提取规则内容
                HashMap<String, String> ruleVal = new HashMap<>();
                StringBuilder infoVal = new StringBuilder();
                for (int i = ruleParams.size(); i-- > 0; ) {
                    List<String> ruleParam = ruleParams.get(i);
                    List<Integer> ruleType = ruleTypes.get(i);
                    infoVal.setLength(0);
                    for (int j = ruleParam.size(); j-- > 0; ) {
                        int regType = ruleType.get(j);
                        if (regType > 0) {
                            infoVal.insert(0, resM.group(regType));
                        } else if (regType < 0) {
                            infoVal.insert(0, resM.group(ruleParam.get(j)));
                        } else {
                            infoVal.insert(0, ruleParam.get(j));
                        }
                    }
                    ruleVal.put(ruleName.get(i), hasVarParams.get(i) ? AnalyzeByRegex.checkKeys(infoVal.toString(), analyzer) : infoVal.toString());
                }
                // 保存当前节点的书籍信息
                item.setSearchInfo(
                        StringUtils.formatHtml(ruleVal.get("ruleName")),        // 保存书名
                        StringUtils.formatHtml(ruleVal.get("ruleAuthor")),      // 保存作者
                        ruleVal.get("ruleKind"),        // 保存分类
                        ruleVal.get("ruleLastChapter"), // 保存终章
                        ruleVal.get("ruleIntroduce"),   // 保存简介
                        ruleVal.get("ruleCoverUrl"),    // 保存封面
                        NetworkUtils.getAbsoluteURL(baseUrl, ruleVal.get("ruleNoteUrl"))       // 保存详情
                );
                books.add(item);
                // 判断搜索结果是否为详情页
                if (books.size() == 1 && (isEmpty(ruleVal.get("ruleNoteUrl")) || Objects.equals(ruleVal.get("ruleNoteUrl"), baseUrl))) {
                    books.get(0).setNoteUrl(baseUrl);
                    books.get(0).setBookInfoHtml(res);
                    return;
                }
            } while (resM.find());
            // 输出调试信息
            Debug.printLog(tag, String.format(StringUtils.getString(R.string.find_matching_result), books.size()));
            Debug.printLog(tag, StringUtils.getString(R.string.get_book_name));
            Debug.printLog(tag, "└" + books.get(0).getName());
            Debug.printLog(tag, StringUtils.getString(R.string.get_book_author));
            Debug.printLog(tag, "└" + books.get(0).getAuthor());
            Debug.printLog(tag, StringUtils.getString(R.string.get_book_kind));
            Debug.printLog(tag, 111, "└" + books.get(0).getKind());
            Debug.printLog(tag, StringUtils.getString(R.string.get_latest_chapter));
            Debug.printLog(tag, "└" + books.get(0).getLastChapter());
            Debug.printLog(tag, StringUtils.getString(R.string.get_book_introduction));
            Debug.printLog(tag, 1, "└" + books.get(0).getIntroduce(), true, true);
            Debug.printLog(tag, StringUtils.getString(R.string.get_book_cover));
            Debug.printLog(tag, "└" + books.get(0).getCoverUrl());
            Debug.printLog(tag, StringUtils.getString(R.string.get_book));
            Debug.printLog(tag, "└" + books.get(0).getNoteUrl());
        } else {
            StringBuilder result = new StringBuilder();
            do {
                result.append(resM.group());
            } while (resM.find());
            getBooksOfRegex(result.toString(), regs, ++index, analyzer, books);
        }
    }
}