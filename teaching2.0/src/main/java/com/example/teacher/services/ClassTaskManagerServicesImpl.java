package com.example.teacher.services;

import com.example.teacher.entity.*;
import com.example.teacher.mapper.ClassStudentManagerMapper;
import com.example.teacher.mapper.ClassTaskManagerMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassTaskManagerServicesImpl implements ClassTaskManagerServices {
    @Autowired
    ClassTaskManagerMapper classTaskManagerMapper;
    @Autowired
    ClassStudentManagerMapper classStudentManagerMapper;
    @Override
    public List<MyClass> queryClassesByTeacher(String TeacherSname) {
        return classTaskManagerMapper.queryClassesByTeacher(TeacherSname);
    }

    @Override
    public List<TaskOne> queryTaskOneByClassId(int ClassId) {
        return classTaskManagerMapper.queryTaskOneByClassId(ClassId);
    }

    @Override
    public List<studentTaskOne> queryStTaskOneByClassTask(String classTaskId) {
        return classTaskManagerMapper.queryStTaskOneByClassTask(classTaskId);
    }

    @Override
    public int deleteClassTask(String classTaskId) {
        List<studentTaskOne> li=classStudentManagerMapper.getClassStuTaskByClassTaskId(classTaskId);
        for(int i=0;i<li.size();i++){
            //删除 taskpiece
            classStudentManagerMapper.deleteClassfromClassStuTaskPiece(li.get(i).getStudentTaskId());
        }
        //删除studentTask
        classStudentManagerMapper.deleteStuTaskfromstudentTask(classTaskId);
        //删除classTask
        classStudentManagerMapper.deleteClassfromClassTask(classTaskId);

        return 1;
    }

    @Override
    public int changeStuTaskScore(studentTaskOne studentTaskOne) {
        return classTaskManagerMapper.changeStuTaskScore(studentTaskOne);
    }

    @Override
    public int deleteStuTaskByStuTaskId(String studentTaskId) {
        System.out.println("deleteStuTaskByStuTaskId"+studentTaskId);
        classTaskManagerMapper.deleteStuTaskPieceByStudentTaskId(studentTaskId);
        classTaskManagerMapper.deleteStuTaskByStuTaskId(studentTaskId);
        return 1;

    }

    @Override
    public List<StuTaskPiece> getStuTaskPiecese(String studentTaskId) {
        return classTaskManagerMapper.getTaskPieceByStuTaskId(studentTaskId);
    }

    @Override
    public TaskOne getTaskOneByStuTaskId(String StuTaskId) {
        return classTaskManagerMapper.getTaskOneByStuTaskId(StuTaskId);
    }

    @Override
    public List<MyClass> queryClasses() {
        return classTaskManagerMapper.queryClasses();
    }


}
