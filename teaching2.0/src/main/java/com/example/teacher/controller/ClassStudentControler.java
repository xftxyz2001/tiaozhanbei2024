package com.example.teacher.controller;

import com.alibaba.fastjson.JSON;
import com.example.teacher.entity.MyClass;
import com.example.teacher.services.ClassStudentManagerServicesImpl;
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
@RequestMapping(value="/ClassStuManager")
public class ClassStudentControler {
    @Autowired
    UserLoginServicesImpl userLoginServicesImpl;

    @Autowired
    ClassTaskManagerServicesImpl classTaskManagerServices;

    @Autowired
    ClassStudentManagerServicesImpl classStudentManagerServices;
    @RequestMapping("/getClassStu")
    public String getClassStu(HttpServletRequest req, HttpServletResponse resp, Integer classId){
        return JSON.toJSONString(classStudentManagerServices.queryClassStuByClassId(classId));
    }
    @RequestMapping("/changeClassName")
    public int changeClassName(HttpServletRequest req, HttpServletResponse resp,String classChangeId,String classNameNew ){
        return classStudentManagerServices.changeClassName(classChangeId,classNameNew);
    }
    @RequestMapping("/changeClassStuInformation")
    public int changeClassStuInformation(HttpServletRequest req, HttpServletResponse resp,String studentSnameChange,String studentSname, String studentName, String studentPassword ){
        return classStudentManagerServices.changeClassStuInformation(studentSnameChange,studentSname,studentName,studentPassword);
    }
    @RequestMapping("/addClassInformation")
    public int addClassInformation(HttpServletRequest req, HttpServletResponse resp,String ClassCreateName){
        List<MyClass> li=classTaskManagerServices.queryClasses();

            int max=li.get(li.size()-1).getClassId();
            int[] record=new int[max+1];
            int curr=0;
            for(int i=0;i<max;i++){
                record[li.get(i).getClassId()]=1;
            }
            for(int i=1;i<record.length;i++){
                if(record[i]==0){
                    curr=i;
                }
            }
            if(curr==0){
                curr=max+1;
            }

        System.out.println("addClassInformation检查出来的需要插入的classId:"+curr+",ClassCreateName:"+ClassCreateName+",teacherSname:"+req.getSession().getAttribute("id").toString());
        classStudentManagerServices.addClass(curr,ClassCreateName,req.getSession().getAttribute("id").toString());
        return curr;
    }
    @RequestMapping("/addClassStuInformation")
    public int addClassStuInformation(HttpServletRequest req, HttpServletResponse resp,String studentSname, String studentName, String studentPassword,Integer classId ){
        return classStudentManagerServices.addClassStu(studentSname,studentName,studentPassword,classId);
    }

    @RequestMapping("/DeleteClass")
    public int deleteClass(HttpServletRequest req, HttpServletResponse resp,String ClassDeleteCheck ){
        System.out.println("deleteClass"+ClassDeleteCheck);
        return classStudentManagerServices.deleteClass(Integer.parseInt(ClassDeleteCheck));
    }

    @RequestMapping("/deleteClassStu")
    public int deleteClassStu(HttpServletRequest req, HttpServletResponse resp,String ClassStuDeleteCheck ){
        System.out.println("deleteClassStu"+ClassStuDeleteCheck);
        return classStudentManagerServices.deleteClassStu(ClassStuDeleteCheck);
    }

    @RequestMapping("/createOtherClass")
    public void createOtherClass(HttpServletRequest req, HttpServletResponse resp,String ClassCreateName){
        List<MyClass> li=classTaskManagerServices.queryClassesByTeacher(req.getSession().getAttribute("id").toString());
        int max=li.get(li.size()-1).getClassId();
        int[] record=new int[max+1];
        int curr=0;
        for(int i=0;i<max;i++){
            record[li.get(i).getClassId()]=1;
        }
        for(int i=1;i<record.length;i++){
            if(record[i]==0){
                curr=i;
            }
        }
        if(curr==0){
            curr=max+1;
        }
        return ;
    }
}
