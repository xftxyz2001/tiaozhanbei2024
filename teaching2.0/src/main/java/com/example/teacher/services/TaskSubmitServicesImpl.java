package com.example.teacher.services;


import com.example.teacher.entity.*;
import com.example.teacher.mapper.TaskSubmitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskSubmitServicesImpl implements TaskSubmitServices {
    @Autowired
    TaskSubmitMapper taskSubmitMapper;

    @Override
    public int Submit(TaskOne taskOne) {
        return taskSubmitMapper.Submit(taskOne);
    }

    @Override
    public int submitStudent(studentTaskOne taskOne) {
        return taskSubmitMapper.submitStudent(taskOne);
    }

    @Override
    public List<classStudent> searchstuByclassid(int id) {
        return taskSubmitMapper.searchstuByclassid(id);
    }


    @Override
    public List<Studentfront> searchStubyclass(teacherClass teacherclass) {
        return taskSubmitMapper.searchStubyclass(teacherclass);
    }

    @Override
    public int submitTaskPiece(StuTaskPiece stuTaskPiece) {
        return taskSubmitMapper.submitTaskPiece(stuTaskPiece);
    }

    @Override
    public  List<TaskOne>  searchByid(String id) {
        return taskSubmitMapper.searchByid(id);
    }

    @Override
    public int inTaskPiece(StuTaskPiece stuTaskPiece) {
        return taskSubmitMapper.inTaskPiece(stuTaskPiece);
    }

    @Override
    public int startTask(TaskDetail taskDetail) {
        return taskSubmitMapper.startTask(taskDetail);
    }

    @Override
    public int checkTask(String id) {
        return taskSubmitMapper.checkTask(id);
    }

    @Override
    public List<TaskDetail> searchDetail(String id) {
        return taskSubmitMapper.searchDetail(id);
    }

    @Override
    public int writeDetail(TaskDetail taskDetail) {
        return taskSubmitMapper.writeDetail(taskDetail);
    }

    @Override
    public int writeFinish(String StuTaskId) {
        return taskSubmitMapper.writeFinish(StuTaskId);
    }

    @Override
    public List<TaskOne> searchfinishByid(String id) {
        return taskSubmitMapper.searchfinishByid(id);
    }

    @Override
    public List<classStudent> searchAllStudent() {
        return taskSubmitMapper.searchAllStudent();
    }


}
