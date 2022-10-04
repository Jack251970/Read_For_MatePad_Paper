package com.jack.bookshelf.web.utils;


import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.StringUtils;

public class ReturnData {

    private boolean isSuccess;

    private int errorCode;

    private String errorMsg;

    private Object data;

    public ReturnData() {
        this.isSuccess = false;
        this.errorMsg = StringUtils.getString(R.string.unknown_error_please_contact_developer);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public ReturnData setErrorMsg(String errorMsg) {
        this.isSuccess = false;
        this.errorMsg = errorMsg;
        return this;
    }

    public Object getData() {
        return data;
    }

    public ReturnData setData(Object data) {
        this.isSuccess = true;
        this.errorMsg = "";
        this.data = data;
        return this;
    }
}
