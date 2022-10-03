package com.xiaoxiaoyi.reggie.service;

/**
 * @author xiaoxiaoyi
 */
public interface MailService {

    /**
     * 发送普通邮件
     *
     * @param to 目标
     * @param subject 主题
     * @param content 内容
     */
    void sendSampleMail(String to, String subject, String content);

}
