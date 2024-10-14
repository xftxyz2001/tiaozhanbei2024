package com.example.teacher.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/hello")
public class TestController {
    @RequestMapping("/")
    public String hello(){
        return "forward:index.html";
    }
    @RequestMapping("/2")
    public String hello2(){
        return "hello";
    }
}