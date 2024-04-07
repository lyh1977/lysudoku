package org.ly.lysudoku.trans;

/**
 * 自定义回调
 * 用于处理结果
 */
public interface ResponseCallBack {
    void success(String json) ;

    void error(String json);
}
