package com.example.teacher.services;

import com.example.teacher.entity.Studentback;
import com.example.teacher.entity.TeacherBack;
import com.example.teacher.entity.UserLogin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserLoginServicesI {
    public Studentback studentqueryById(String id);
    public TeacherBack teacherqueryByid(String id);
    public int addTeacher(String id,String name,String password);
    public int updateTeacher(String id,String password);

}
