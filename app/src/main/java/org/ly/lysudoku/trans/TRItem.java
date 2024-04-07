package org.ly.lysudoku.trans;

import java.io.Serializable;

public class TRItem implements Serializable {
    private String src;
    private String dst;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }
}
