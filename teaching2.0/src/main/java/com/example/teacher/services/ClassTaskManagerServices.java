package com.example.teacher.services;

import com.example.teacher.entity.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClassTaskManagerServices {
    public List<MyClass> queryClassesByTeacher(String TeacherSname);
    public List<TaskOne> queryTaskOneByClassId(int ClassId);
    public List<studentTaskOne> queryStTaskOneByClassTask(String classTaskId);
    public int deleteClassTask(String classTaskId);
    public int changeStuTaskScore(studentTaskOne studentTaskOne);
    public int deleteStuTaskByStuTaskId(String studentTaskId);
    public List<StuTaskPiece> getStuTaskPiecese(String studentTaskId);
    public TaskOne getTaskOneByStuTaskId(String StuTaskId);
    public List<MyClass> queryClasses();
}
