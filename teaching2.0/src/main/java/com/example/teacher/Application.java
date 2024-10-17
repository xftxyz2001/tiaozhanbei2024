package com.example.teacher;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        // 打开 http://localhost:8080/login.html
        String url = "http://localhost:8080/login.html";
        
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("win")) { // Windows
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (osName.contains("mac")) { // macOS
                Runtime.getRuntime().exec("open " + url);
            } else { // Linux and Unix
                Runtime.getRuntime().exec("xdg-open " + url);
            }
        } catch (IOException e) {
            System.err.println("Error opening browser: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
