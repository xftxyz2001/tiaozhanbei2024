package com.example.teacher.services;

import com.example.teacher.entity.MyClass;
import com.example.teacher.entity.TaskOne;
import com.example.teacher.entity.studentTaskOne;
import com.example.teacher.mapper.ClassStudentManagerMapper;
import com.example.teacher.mapper.ClassTaskManagerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassStudentManagerServicesImpl implements ClassStudentManagerServices {
    @Autowired
    ClassStudentManagerMapper classStudentManagerMapper;


    @Override
    public List<MyClass> queryClassStuByClassId(int ClassId) {
        return classStudentManagerMapper.queryClassStuByClassId(ClassId);
    }

    @Override
    public int changeClassName(String classChangeId, String classNameNew) {
        return classStudentManagerMapper.changeClassName( classChangeId,classNameNew);
    }

    @Override
    public int changeClassStuInformation(String studentSnameChange,String studentSname, String studentName, String studentPassword) {
        return classStudentManagerMapper.changeClassStuInformation(studentSnameChange,studentSname,studentName,studentPassword);
    }

    @Override
    public int addClass(int ClassCreateId,String ClassCreateName, String teacherSname) {
        return classStudentManagerMapper.addClass(ClassCreateId,ClassCreateName,teacherSname);
    }

    @Override
    public int addClassStu(String studentSname, String studentName, String studentPassword, int classId) {
        return classStudentManagerMapper.addClassStu(studentSname,studentName,studentPassword,classId);
    }

    @Override
    public int deleteClass(int classId) {
        List<TaskOne> li=classStudentManagerMapper.getClassTaskIdByClassId(classId);
        String classTaskId;

        for(int i=0;i<li.size();i++){
            classTaskId=li.get(i).getClassTaskId();
            List<studentTaskOne> liStuTask=classStudentManagerMapper.getClassStuTaskByClassTaskId(classTaskId);
            for(int n=0;n<liStuTask.size();n++){
                //删除taskPiece
                classStudentManagerMapper.deleteClassfromClassStuTaskPiece(liStuTask.get(n).getStudentTaskId());
            }
            //删除stutask
            classStudentManagerMapper.deleteStuTaskfromstudentTask(classTaskId);
            //删除classtask
            classStudentManagerMapper.deleteClassfromClassTask(classTaskId);
        }
        //删除班级下的班级学生
        classStudentManagerMapper.deleteClassfromStudent(classId);
        //删除班级
        classStudentManagerMapper.deleteClassfromClass(classId);
        return 1;
    }

    @Override
    public int deleteClassStu(String studentSname) {
        List<studentTaskOne> li=classStudentManagerMapper.getStuTaskByStuSname(studentSname);
        for(int i=0;i<li.size();i++){
            classStudentManagerMapper.deleteClassfromClassStuTaskPiece(li.get(i).getStudentTaskId());
        }

        classStudentManagerMapper.deleteClassStufromStudent(studentSname);
        return 1;
    }

    @Override
    public int updateNewOtherClass(String ClassCreateName,String TeacherSname,int classId) {
        int TeachCourseId=classStudentManagerMapper.searchCourseTeach(TeacherSname);
        classStudentManagerMapper.addClassCourse(classId,TeachCourseId);
        return 0;
    }
}
