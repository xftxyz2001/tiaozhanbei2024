package com.example.teacher.services;

import com.example.teacher.entity.Studentback;
import com.example.teacher.entity.TeacherBack;
import com.example.teacher.mapper.UserLoginMapper;
import com.example.teacher.entity.UserLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserLoginServicesImpl implements UserLoginServicesI {

    @Autowired
    UserLoginMapper userLoginMapper;
    @Override
    public List<UserLogin> queryAll() {
        return userLoginMapper.queryAll();
    }

    @Override
    public Studentback studentqueryById(String id) {
        return userLoginMapper.studentqueryById(id);
    }

    @Override
    public TeacherBack teacherqueryByid(String id) {
        return userLoginMapper.teacherqueryByid(id);

    }


    @Override
    public int add(UserLogin userLogin) {
        return userLoginMapper.add(userLogin);
    }

    @Override
    public UserLogin queryByName(String username) {
        return userLoginMapper.queryByName(username);
    }

    @Override
    public int addTeacher(String id, String name, String password) {
        return userLoginMapper.addTeacher(id,name,password);
    }

    @Override
    public int updateTeacher(String id, String password) {

        return userLoginMapper.updateTeacher(id,password);
    }
}

