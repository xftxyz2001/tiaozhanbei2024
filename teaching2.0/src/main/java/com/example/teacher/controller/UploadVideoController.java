package com.example.teacher.controller;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.core.io.FileTypeUtil;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;

@RestController
@RequestMapping("/UploadVideo")
public class UploadVideoController {
    public static final String API_KEY = "GoZImi1uvLjPXp4F9VrmtbWF";
    public static final String SECRET_KEY = "TBi1sSRN6PkZjinztWoWKOiMFrDaKRIM";

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();
    @RequestMapping("/UploadVideo")
    public String UploadVideoServlet(HttpServletRequest req, HttpServletResponse resp, @RequestBody MultipartFile audio) throws IOException {
        System.out.println( audio);
//        MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
//        String mimeType = fileTypeMap.getContentType(file.getName());



        String tmpFileDir = null;
        String fileName = null;
        File dirFile = null;
        File localFile = null;
        tmpFileDir = "D:/tmp/";
        dirFile = new File(tmpFileDir);
        if(!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //MultipartFile转成File对象
        fileName = tmpFileDir+"/"+audio.getOriginalFilename();
        localFile = new File(fileName);
        audio.transferTo(localFile);

        String base64 = null;
        InputStream in = null;
        String result="";
        try {
            File file = new File(fileName);
            in = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            in.read(bytes);
            base64 = new String(Base64.encodeBase64(bytes),"UTF-8");
            System.out.println("将文件["+fileName+"]转base64字符串长度:"+(int) file.length());
            System.out.println(base64);
            MediaType mediaType = MediaType.parse("application/json");
            // speech 可以通过 getFileContentAsBase64("C:\fakepath\blob.wav") 方法获取,如果Content-Type是application/x-www-form-urlencoded时,第二个参数传true
            String content="{\"format\":\"wav\",\"rate\":16000,\"channel\":1,\"cuid\":\"6fash4e4C8EnPVLakXi1ZyVYLJh3rjFg\",\"token\":\"\",\"speech\":\""+base64+"\",\"len\":"+(int) file.length()+",\"token\":\"" + getAccessToken() + "\"}";
            System.out.println("content:");
            System.out.println(content);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, content);
            Request request = new Request.Builder()
                    .url("https://vop.baidu.com/server_api")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();
            Response response = HTTP_CLIENT.newCall(request).execute();
            result=response.body().string();
            System.out.println("给出的完整回答为"+result);
            result=result.substring(result.indexOf("result")+10,result.indexOf("sn")-4);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;




//        String baseStr = null;
//
//        baseStr= Base64.getEncoder().encodeToString(audio.getBytes());
////        baseStr = baseStr.replaceAll("\r\n", "");
//        System.out.println(baseStr);
//        System.out.println(getBase64Len((long)baseStr.length()));
//        return "baseStr";
    }
    static String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();

        String temp=response.body().string();
        temp=temp.substring(temp.indexOf("access_token")+15,temp.indexOf("scope")-3);
        System.out.println("getAccessToken:String:"+temp);
        return temp;
    }
}
