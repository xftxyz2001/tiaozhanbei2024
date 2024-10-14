package com.example.teacher.mapper;

import com.example.teacher.entity.MyClass;
import com.example.teacher.entity.StuTaskPiece;
import com.example.teacher.entity.TaskOne;
import com.example.teacher.entity.studentTaskOne;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClassStudentManagerMapper {
    public List<MyClass> queryClassStuByClassId(int ClassId);
    public int changeClassName(String classChangeId,String classNameNew);
    public int changeClassStuInformation(String studentSnameChange,String studentSname,String studentName,String studentPassword);
    public int addClass(int ClassCreateId,String ClassCreateName,String teacherSname);
    public int addClassStu(String studentSname,String studentName,String studentPassword,int  classId);
    public int deleteClass(int classId);
    public List<TaskOne> getClassTaskIdByClassId(int classId);
    public List<studentTaskOne> getClassStuTaskByClassTaskId(String ClassTaskId);
    public int deleteClassfromClassStuTaskPiece(String StudentTaskId);
    public int deleteClassfromClassStuTask(String StudentTaskId);
    public int deleteClassfromClassTask(String ClassTaskId);
    public int deleteClassfromStudent(int ClassId);
    public int deleteClassfromClass(int ClassId);
    public List<studentTaskOne> getStuTaskByStuSname(String StudentSname);
    public int deleteClassStufromStudent(String studentSname);
    public int deleteStuTaskfromstudentTask(String classTaskId);
    public int deleteClassTaskFrom(String ClassTaskId);
    public int searchCourseTeach(String TeacherSname);
    public int addClassCourse(int ClassId,int  CourseTeachId );

}
