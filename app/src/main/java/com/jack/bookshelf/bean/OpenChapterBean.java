package com.jack.bookshelf.bean;

public class OpenChapterBean {
    private final int chapterIndex;
    private final int pageIndex;

    public OpenChapterBean(int chapterIndex, int pageIndex) {
        this.chapterIndex = chapterIndex;
        this.pageIndex = pageIndex;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }
}
