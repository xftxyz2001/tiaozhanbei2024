package com.example.teacher.mapper;

import com.example.teacher.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.scheduling.config.Task;

import java.util.List;

@Mapper
public interface ClassTaskManagerMapper {
    public List<MyClass> queryClassesByTeacher(String TeacherSname);
    public List<MyClass> queryClasses();
    public List<TaskOne> queryTaskOneByClassId(int ClassId);

    public List<studentTaskOne> queryStTaskOneByClassTask(String classTaskId);
    public int changeStuTaskScore(studentTaskOne studentTaskOne);
    public int deleteStuTaskByStuTaskId(String studentTaskId);
    public int deleteStuTaskPieceByStudentTaskId(String studentTaskId);
    public List<StuTaskPiece> getTaskPieceByStuTaskId(String StuTaskId);
    public TaskOne getTaskOneByStuTaskId(String StuTaskId);
}
