package com.cqust.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.SecureUtil;
import com.cqust.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file){
        // file是一个临时文件，需要转存到其他位置
        log.info(file.toString());
        // 原始文件名
        String originalFilename =  file.getOriginalFilename();
        // 文件类型
        //String suffix = StrUtil.DOT + FileUtil.extName(originalFilename);   // hutool工具类获取文件类型(没有.)需要+StrUtil.DOT
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 使用uuid重新生成文件名，防止文件名重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        // 创建目录文件
        File dir = new File(basePath);
        // 判断当前目录是否存在
        if(!dir.exists()){  // 目录不存在，需要创建
            dir.mkdir();
        }

        try {
            // 将临时文件转存到指定位置
            file.transferTo(new File(basePath  +fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName,"success");
    }

    /**
     * 文件下载
     * @param response
     * @param name
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response, String name){
        // 输入流，通过读取文件内容
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            // 输出流，通过输出流将文件回写浏览器，在浏览器显示图片
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            // hutool工具类可直接完成输出流
//            IoUtil.copy(fileInputStream, outputStream);

            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
//            关闭资源
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
