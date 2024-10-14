package com.example.teacher.controller;

import com.example.teacher.entity.ClassTask;
import com.example.teacher.mapper.StudentSearchMapper;
import com.example.teacher.services.TaskSubmitServicesImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class QuestionGet {
    //方法错误
    public String studentTaskId;
    public ClassTask classTask;
    public int sceneFirstNum ;
    public int sceneSecondNum;
    public int sceneThirdNum;
    public int t;
    public int n;
    public String sceneFirstOrientation;
    public String sceneSecondOrientation;
    public String sceneThirdOrientation;
    @Autowired
    TaskSubmitServicesImpl taskSubmitServicesImpl;
    @Autowired
    StudentSearchMapper studentSearchMapper;
    public String question(){
        //获取到问题文本
        return "测试ing";
    }
    public QuestionGet(String studentTaskId){
        //拿到班级任务设置
        this.studentTaskId=studentTaskId;
//        this.classTask=studentSearchMapper.queryClassTaskByStuId(studentTaskId);
//        taskSubmitServicesImpl.searchByid()
    }
    public QuestionGet(){

        //拿到班级任务设置
    }
    public void setStudentTaskId(){
        this.studentTaskId=studentTaskId;
    }
    public void setClassTask(String studentTaskId){
        this.studentTaskId=studentTaskId;
//        this.classTask=studentSearchMapper.queryClassTaskByStuId(studentTaskId);
    }
    public void setTN(int t,int n){
        this.t=t;
        this.n=n;
    }
    public String getQuestion(){
        return "测试";
    }

}
