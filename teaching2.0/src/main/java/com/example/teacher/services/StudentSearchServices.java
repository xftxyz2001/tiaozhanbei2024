package com.example.teacher.services;

import com.example.teacher.entity.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StudentSearchServices {
    public List<classStudent> queryclassStudent();
    public List<classStudent> queryclassStudentbyTeacher(String teacherSname);
    public TaskOne queryClassTaskByClassTaskId(String classTaskId);
    public ClassTask queryClassTaskByStuTaskId(String stuTaskId);

    public studentTaskOne querystuTaskOneByStuTaskId(String stuTaskId);

    public List<StuTaskPiece> queryTaskPieceByStuId(studentTaskOne stuTaskOne);

    public List<StuTaskPiece> queryTaskPieceByStuTaskId(String StuTaskId);

    public List<studentTaskOne> getStuTasksByStuSname(String studentSname);

    public List<StuTaskPiece> getStuTaskPieceByStuSname(String studentSname);

    public int UpdateStuTaskFirstScore(int Score,String StuTaskId);

    public int UpdateStuTaskSecondScore(int Score,String StuTaskId);

    public int UpdateStuTaskThirdScore(int Score,String StuTaskId);
}
