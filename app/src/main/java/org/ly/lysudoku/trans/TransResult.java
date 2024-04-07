package org.ly.lysudoku.trans;

import java.io.Serializable;

public class TransResult implements Serializable {
    private String from;
    private String to;
    private TRItem[] trans_result;
    private String error_code;
    private  String error_msg;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public TRItem[] getTrans_result() {
        return trans_result;
    }

    public void setTrans_result(TRItem[] trans_result) {
        this.trans_result = trans_result;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }
}
