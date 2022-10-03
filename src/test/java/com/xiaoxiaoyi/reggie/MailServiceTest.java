package com.xiaoxiaoyi.reggie;

import com.xiaoxiaoyi.reggie.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Test
    public void testSimpleMail() throws Exception {
        mailService.sendSampleMail("2060924350@qq.com"
                , "test simple mail"
                , "hello, this is a test simple mail!");
    }

}
