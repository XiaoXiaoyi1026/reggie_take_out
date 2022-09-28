package com.xiaoxiaoyi.reggie.controller;

import com.xiaoxiaoyi.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 通用控制器，用于管理文件的upload和download
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.img-path}")
    private String imgPath;

    /**
     * 上传图片文件
     * 参数名称必须等于提交表单的name属性
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestBody MultipartFile file) {
        // file是一个.tmp的临时文件，需要存储到本地，当方法执行完成会被删除
        log.info(file.toString());

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();

        // 获取文件后缀名
        assert originalFilename != null;
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 随机生成32位UUID
        String fileName = UUID.randomUUID() + suffix;

        // 判断imgPath目录是否存在，如果不存在则创建
        File path = new File(imgPath);
        if (!path.exists()) {
            // 不存在则创建该目录
            path.mkdir();
        }

        try {
            // 将文件转存到指定的位置
            file.transferTo(new File(imgPath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 返回文件名称，用于添加进数据库
        return R.success(fileName);
    }

    /**
     * 图片下载接口
     *
     * @param name 下载的目标图片名称
     * @param response 页面响应，用于获取输出流向页面输出图片信息
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            // 1. 从本地目录中读取目标图片
            FileInputStream inputStream = new FileInputStream(new File(imgPath + name));
            // 2. 使用response的输出流输出到页面
            response.setHeader("content-type", "image/jpg");
            ServletOutputStream outputStream = response.getOutputStream();
            int len = 0;
            // 每次读1M
            byte[] bytes = new byte[1024];
            // 当还没有读完时
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
