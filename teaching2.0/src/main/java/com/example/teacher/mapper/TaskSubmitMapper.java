package com.example.teacher.mapper;
import com.example.teacher.entity.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TaskSubmitMapper {
    public int Submit(TaskOne taskOne);
    public int submitStudent(studentTaskOne stutask);
    public List<classStudent> searchstuByclassid(int id);
    public List<Studentfront> searchStubyclass(teacherClass teacherclass);
    public int submitTaskPiece(StuTaskPiece stuTaskPiece);
    public List<TaskOne> searchByid(String id);//搜索学生名下的任务
    public TaskOne searchStuTask(String id);//搜索学生某一个任务的班级任务结果
    public int inTaskPiece(StuTaskPiece stuTaskPiece);
    public int startTask(TaskDetail taskDetail);
    public int checkTask(String id);
    public List<TaskDetail> searchDetail(String id);
    public int writeDetail(TaskDetail taskDetail);
    public int writeFinish(String StuTaskId);
    public List<TaskOne> searchfinishByid(String id);//搜索学生名下的任务
    public List<classStudent> searchAllStudent();
    //查询
//    public List<UserLogin> queryAll();
//
//    public UserLogin queryById(String id);
//    //添加数据
//    public int add(UserLogin userLogin);
//    //根据用户名查询数据
//    public UserLogin queryByName(String username);
}
