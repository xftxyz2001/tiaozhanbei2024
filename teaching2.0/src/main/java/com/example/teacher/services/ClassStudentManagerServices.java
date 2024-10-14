package com.example.teacher.services;

import com.example.teacher.entity.MyClass;

import java.util.List;

public interface ClassStudentManagerServices {
    public List<MyClass> queryClassStuByClassId(int ClassId);
    public int changeClassName(String classChangeId,String classNameNew);
    public int changeClassStuInformation(String studentSnameChange,String studentSname,String studentName,String studentPassword);
    public int addClass(int ClassCreateId,String ClassCreateName,String teacherSname);
    public int addClassStu(String studentSname,String studentName,String studentPassword,int  classId);
    public int deleteClass(int classId);
    public int deleteClassStu(String studentSname);
    public int updateNewOtherClass(String ClassCreateName,String TeacherSname,int classId);
}
