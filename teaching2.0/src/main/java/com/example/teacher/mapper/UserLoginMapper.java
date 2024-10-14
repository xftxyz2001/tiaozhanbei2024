package com.example.teacher.mapper;


import com.example.teacher.entity.Studentback;
import com.example.teacher.entity.TeacherBack;
import com.example.teacher.entity.UserLogin;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface  UserLoginMapper  {
    //查询
    public List<UserLogin> queryAll();

    public Studentback studentqueryById(String id);
    public TeacherBack teacherqueryByid(String id);
    //添加数据
    public int add(UserLogin userLogin);
    //根据用户名查询数据
    public UserLogin queryByName(String username);
    public int addTeacher(String id,String name,String password);
    public int updateTeacher(String id,String password);
}
