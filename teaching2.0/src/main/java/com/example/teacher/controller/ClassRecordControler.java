package com.example.teacher.controller;

import com.alibaba.fastjson.JSON;
import com.example.teacher.entity.MyClass;
import com.example.teacher.entity.Studentback;
import com.example.teacher.entity.Studentfront;
import com.example.teacher.entity.studentTaskOne;
import com.example.teacher.services.ClassTaskManagerServicesImpl;
import com.example.teacher.services.UserLoginServicesImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping(value="/ClassRecord")
public class ClassRecordControler {
    @Autowired
    UserLoginServicesImpl userLoginServicesImpl;

    @Autowired
    ClassTaskManagerServicesImpl classTaskManagerServices;

    @RequestMapping("/getClasses")
    public String getClasses(HttpServletRequest req, HttpServletResponse resp){
        return JSON.toJSONString(classTaskManagerServices.queryClassesByTeacher(req.getSession().getAttribute("id").toString()));
    }

    @RequestMapping("/getClassTask")
    public String getClassTask(HttpServletRequest req, HttpServletResponse resp, Integer ClassId){
        System.out.println("getClassTask查看班级："+ClassId+"下的任务");
        return JSON.toJSONString(classTaskManagerServices.queryTaskOneByClassId(ClassId));
    }

    @RequestMapping("/getStuTask")
    public String getStuTask(HttpServletRequest req, HttpServletResponse resp, String ClassTaskId){
        System.out.println("getStuTask查看班级任务下："+ClassTaskId+"下的学生任务"+JSON.toJSONString(classTaskManagerServices.queryStTaskOneByClassTask(ClassTaskId)));
        return JSON.toJSONString(classTaskManagerServices.queryStTaskOneByClassTask(ClassTaskId));
//        return null;
    }
    @RequestMapping("/deleteClassTask")
    public int deleteClassTask(HttpServletRequest req, HttpServletResponse resp, String ClassTaskId){
        System.out.println("deleteClassTask");
        //删掉classtask
        return classTaskManagerServices.deleteClassTask(ClassTaskId);
    }
    @RequestMapping("/changeTaskScore")
    public int changeTaskScore(HttpServletRequest req, HttpServletResponse resp, studentTaskOne studenttaskone){
        return classTaskManagerServices.changeStuTaskScore(studenttaskone);
    }
    @RequestMapping("/deleteStuTaskByTaskId")
    public int deleteStuTaskByTaskId(HttpServletRequest req, HttpServletResponse resp, String studentTaskId){
        return classTaskManagerServices.deleteStuTaskByStuTaskId(studentTaskId);
    }
    @RequestMapping("/LookStuTaskOne")
    public int LookStuTaskOne(HttpServletRequest req, HttpServletResponse resp, String stuTaskId){
        System.out.println("设置session中的stuTaskId为"+stuTaskId);
        req.getSession().setAttribute("stuTaskId",stuTaskId);
        return 1;
    }
    @RequestMapping("/getStuTaskPiecese")
    public String getStuTaskPiecese(HttpServletRequest req, HttpServletResponse resp){

        return JSON.toJSONString(classTaskManagerServices.getStuTaskPiecese(req.getSession().getAttribute("stuTaskId").toString()));
    }

    @RequestMapping("/getTaskOneByStuTaskId")
    public String getTaskOneByStuTaskId(HttpServletRequest req, HttpServletResponse resp){
        return JSON.toJSONString(classTaskManagerServices.getTaskOneByStuTaskId(req.getSession().getAttribute("stuTaskId").toString()));
    }


}
