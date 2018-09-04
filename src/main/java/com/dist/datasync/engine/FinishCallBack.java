package com.dist.datasync.engine;

/**
 * 同步结束回调接口
 * @author lijy
 */
public interface FinishCallBack {
    void finish(Integer exceptionCount, Boolean success, String message);
}
