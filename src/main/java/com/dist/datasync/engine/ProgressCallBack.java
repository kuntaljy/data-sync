package com.dist.datasync.engine;

/**
 * 同步进度回调接口
 * @author lijy
 */
public interface ProgressCallBack {

    /***
     * 设置新的进度信息
     * @param progress
     */
    void setProgress(Float progress);
}
