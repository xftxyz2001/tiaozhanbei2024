package com.example.teacher.services;

import com.example.teacher.entity.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface TaskSubmitServices {
    public int Submit(TaskOne taskOne);
    public int submitStudent(studentTaskOne taskOne);
    public List<classStudent> searchstuByclassid(int id);
    public List<Studentfront> searchStubyclass(teacherClass teacherclass);
    public int submitTaskPiece(StuTaskPiece stuTaskPiece);
    public  List<TaskOne>  searchByid(String id);
    public int inTaskPiece(StuTaskPiece stuTaskPiece);
    public int startTask(TaskDetail taskDetail);
    public int checkTask(String id);
    public List<TaskDetail> searchDetail(String id);
    public int writeDetail(TaskDetail taskDetail);
    public int writeFinish(String StuTaskId);
    public List<TaskOne> searchfinishByid(String id);//搜索学生名下的任务
    public List<classStudent> searchAllStudent();
}
