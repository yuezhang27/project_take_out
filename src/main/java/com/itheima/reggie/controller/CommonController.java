package com.itheima.reggie.controller;

import com.itheima.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;
    @PostMapping("/upload")
    public Result<String> uploadFile(MultipartFile file){
        log.info(file.toString());
        File dir=new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        String originalFileName=file.getOriginalFilename();
        String suffix=originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName=UUID.randomUUID().toString()+suffix;
        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.success(fileName);
    }
    @GetMapping("/download")
    public void downloadFile(String name, HttpServletResponse response){
        try {
            File file=new File(basePath+name);
            FileInputStream fileInputStream=new FileInputStream(file);
            ServletOutputStream outputStream= response.getOutputStream();
            response.setContentType("image/jpeg");
            int len;
            byte[] b=new byte[1024];
            while((len=fileInputStream.read(b))!=-1){
                outputStream.write(b,0,len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
