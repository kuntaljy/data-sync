package com.dist.datasync.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 拦截所有异常，返回统一结构
 * @author lijy
 */
@ControllerAdvice
public class ExceptionProcessor {
    private final static Logger logger = LoggerFactory.getLogger(ExceptionProcessor.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result handle(Exception e){
        logger.error("[系统异常]",e);
        return Result.error(e.getMessage());
    }
}
