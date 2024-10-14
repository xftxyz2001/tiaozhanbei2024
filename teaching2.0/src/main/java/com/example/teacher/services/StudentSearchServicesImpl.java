package com.example.teacher.services;


import com.example.teacher.entity.*;
import com.example.teacher.mapper.StudentSearchMapper;
import com.example.teacher.mapper.TaskSubmitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentSearchServicesImpl implements StudentSearchServices {
    @Autowired
    TaskSubmitMapper taskSubmitMapper;
    @Autowired
    StudentSearchMapper studentSearchMapper;

    @Override
    public List<classStudent> queryclassStudent() {
        return studentSearchMapper.queryclassStudent();
    }

    @Override
    public List<classStudent> queryclassStudentbyTeacher (String teacherSname){
        return studentSearchMapper.queryclassStudentbyTeacher(teacherSname);
    }

    @Override
    public TaskOne queryClassTaskByClassTaskId(String classTaskId) {
        return studentSearchMapper.queryClassTaskByClassTaskId(classTaskId);
    }

    @Override
    public ClassTask queryClassTaskByStuTaskId(String stuTaskId) {
        return studentSearchMapper.queryClassTaskByStuTaskId(stuTaskId);
    }

    @Override
    public studentTaskOne querystuTaskOneByStuTaskId(String stuTaskId) {
        return studentSearchMapper.querystuTaskOneByStuTaskId(stuTaskId);
    }

    @Override
    public List<StuTaskPiece> queryTaskPieceByStuId(studentTaskOne stuTaskOne) {
        return studentSearchMapper.queryTaskPieceByStuId( stuTaskOne);
    }

    @Override
    public List<StuTaskPiece> queryTaskPieceByStuTaskId(String StuTaskId) {
        return studentSearchMapper.queryTaskPieceByStuTaskId(StuTaskId);
    }

    @Override
    public List<studentTaskOne> getStuTasksByStuSname(String studentSname){
        return studentSearchMapper.getStuTasksByStuSname(studentSname);
    }

    @Override
    public List<StuTaskPiece> getStuTaskPieceByStuSname(String studentSname) {
        return studentSearchMapper.getStuTaskPieceByStuSname(studentSname);
    }

    @Override
    public int UpdateStuTaskFirstScore(int Score, String StuTaskId) {
        return studentSearchMapper.UpdateStuTaskFirstScore(Score,StuTaskId);
    }

    @Override
    public int UpdateStuTaskSecondScore(int Score, String StuTaskId) {
        return studentSearchMapper.UpdateStuTaskSecondScore(Score,StuTaskId);
    }

    @Override
    public int UpdateStuTaskThirdScore(int Score, String StuTaskId) {
        return studentSearchMapper.UpdateStuTaskThirdScore(Score,StuTaskId);
    }
}
