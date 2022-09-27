package com.xiaoxiaoyi.reggie.common;

/**
 * 基于ThreadLocal封装的工具类，基于每次请求的所有方法共用一个线程实现
 * 用于保存每次请求的一些公共变量，可以在一次请求的所有方法间共享
 * 作用域在单个线程内，多个线程之间互不影响
 */
public class BaseContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 保存变量
     *
     * @param id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取变量
     *
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
