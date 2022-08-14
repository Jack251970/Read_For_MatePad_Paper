//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.jack.bookshelf.bean;

import android.content.Context;

import com.google.gson.Gson;
import com.jack.bookshelf.R;
import com.jack.bookshelf.help.BookshelfHelp;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Objects;

/**
 * 章节列表
 */
@Entity
public class BookChapterBean implements Cloneable, BaseChapterBean {
    private String tag;
    private String noteUrl; //对应BookInfoBean noteUrl;

    private int durChapterIndex;  //当前章节数
    @Id
    private String durChapterUrl;  //当前章节对应的文章地址
    private String durChapterName;  //当前章节名称
    private boolean isVip;
    private boolean isPay;
    //章节内容在文章中的起始位置(本地)
    private Long start;
    //章节内容在文章中的终止位置(本地)
    private Long end;

    public BookChapterBean() {
    }

    @Generated(hash = 922679906)
    public BookChapterBean(String tag, String noteUrl, int durChapterIndex, String durChapterUrl, String durChapterName,
                           boolean isVip, boolean isPay, Long start, Long end) {
        this.tag = tag;
        this.noteUrl = noteUrl;
        this.durChapterIndex = durChapterIndex;
        this.durChapterUrl = durChapterUrl;
        this.durChapterName = durChapterName;
        this.isVip = isVip;
        this.isPay = isPay;
        this.start = start;
        this.end = end;
    }

    public BookChapterBean(String tag, String durChapterName, String durChapterUrl) {
        this.tag = tag;
        this.durChapterName = durChapterName;
        this.durChapterUrl = durChapterUrl;
    }

    public BookChapterBean(String tag, String durChapterName, String durChapterUrl, boolean isVip, boolean isPay) {
        this.tag = tag;
        this.durChapterName = durChapterName;
        this.durChapterUrl = durChapterUrl;
        this.isVip = isVip;
        this.isPay = isPay;
    }

    @Override
    protected Object clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, BookChapterBean.class);
        } catch (Exception ignored) {
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BookChapterBean) {
            BookChapterBean bookChapterBean = (BookChapterBean) obj;
            return Objects.equals(bookChapterBean.durChapterUrl, durChapterUrl);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (durChapterUrl == null) {
            return 0;
        }
        return durChapterUrl.hashCode();
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String getDurChapterName() {
        return this.durChapterName;
    }

    public void setDurChapterName(String durChapterName) {
        this.durChapterName = durChapterName;
    }

    @Override
    public String getDurChapterUrl() {
        return this.durChapterUrl;
    }

    public void setDurChapterUrl(String durChapterUrl) {
        this.durChapterUrl = durChapterUrl;
    }

    @Override
    public int getDurChapterIndex() {
        return this.durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    @Override
    public String getNoteUrl() {
        return this.noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public Long getStart() {
        return this.start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return this.end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Boolean getHasCache(BookInfoBean bookInfoBean) {
        return BookshelfHelp.isChapterCached(bookInfoBean.getName(), tag, this, bookInfoBean.isAudio());
    }

    public boolean getIsVip() {
        return this.isVip;
    }

    public void setIsVip(boolean isVip) {
        this.isVip = isVip;
    }

    public boolean getIsPay() {
        return this.isPay;
    }

    public void setIsPay(boolean isPay) {
        this.isPay = isPay;
    }

    public String getDisplayTitle(Context context) {
        if (!isVip) {
            return durChapterName;
        }
        if (isPay) {
            return context.getString(R.string.payed_title, durChapterName);
        }
        return context.getString(R.string.vip_title, durChapterName);
    }

}
