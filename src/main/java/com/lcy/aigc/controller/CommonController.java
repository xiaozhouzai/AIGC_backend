package com.lcy.aigc.controller;

import com.lcy.aigc.common.BaseResponse;
import com.lcy.aigc.common.ResultUtils;
import com.lcy.aigc.model.dto.file.FileUploadRequest;
import com.lcy.aigc.utils.AliyunOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {
    @Autowired
    private AliyunOSSUtils aliyunOSSUtils;

    @PostMapping("/upload")
    public BaseResponse<String> upload(@RequestParam("photo") MultipartFile file) throws IOException {
        String fileName= aliyunOSSUtils.upload(file);
        return ResultUtils.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            URL url=new URL(name);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            response.setContentType("image/jpeg");
            InputStream in = connection.getInputStream();
            OutputStream out = response.getOutputStream();
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = in.read(bytes)) != -1){
                out.write(bytes,0,len);
                out.flush();
            }
            out.close();
            in.close();
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }
}