package com.xiaoxiaoyi.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */

@Slf4j
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
public class GlobalExceptionHandle {

    /**
     * 异常处理方法
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandle(SQLIntegrityConstraintViolationException ex) {

        String message = ex.getMessage();
        // 日志打印异常信息
        log.info(message);

        // 判断异常信息
        if (message.contains("Duplicate entry")) {
            String[] strings = message.split(" ");
            return R.error(strings[2] + "已存在！");
        }

        // 返回未知错误
        return R.error("未知错误：" + message);
    }

    /**
     * 负责拦截业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandle(CustomException ex) {

        String message = ex.getMessage();
        // 日志打印异常信息
        log.error(message);

        // 返回未知错误
        return R.error("删除失败：" + message);
    }

}
