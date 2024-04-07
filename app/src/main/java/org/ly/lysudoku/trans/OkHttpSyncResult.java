package org.ly.lysudoku.trans;

public class OkHttpSyncResult {
    String respBody;
    boolean success;
    int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getRespBody() {
        return respBody;
    }

    public void setRespBody(String respBody) {
        this.respBody = respBody;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
