package com.example.teacher.controller;

import com.example.teacher.entity.*;
import com.example.teacher.services.UserLoginServicesImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import javax.net.ssl.HandshakeCompletedEvent;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping(value="/login")
public class LoginController {
    @Autowired
    UserLoginServicesImpl userLoginServicesImpl;

    @RequestMapping("/LoginStudent")
    public String LoginStudent(HttpServletRequest req, HttpServletResponse resp, Studentfront studentfront){
        //先查询看该用户名是否存在
        System.out.println(studentfront.getId()+" "+studentfront.getPassword());
        Studentback userLogin1 = userLoginServicesImpl.studentqueryById(studentfront.getId());

        System.out.println(userLogin1.getStudentSname());
        if(userLogin1 != null){ //  如果查询的用户不为空
            System.out.println("LoginSuccess");
            req.getSession().setAttribute("name", userLogin1.getStudentName());
            req.getSession().setAttribute("id", userLogin1.getStudentSname());
            req.getSession().setAttribute("class", userLogin1.getClassid());
//           model.addAttribute("name",userLogin1.getName());
//           System.out.println("getName"+model.getAttribute("name").toString());
            System.out.println(userLogin1.getStudentPassword()+" "+studentfront.getPassword());
           if (userLogin1.getStudentPassword().equals(studentfront.getPassword())){
               return "2";
           }else{
               return "error" ;
           }
        }
        else{
            //返回到登录页面
            req.getSession().setAttribute("data","该用户不存在，请先注册");
            return "retrun";
        }
    }
    @RequestMapping("/LoginTeacher")
    public String LoginTeacher(HttpServletRequest req, HttpServletResponse resp, TeacherFront teacherFront){
        //先查询看该用户名是否存在
        System.out.println(teacherFront.getId());
        TeacherBack userLogin1 = userLoginServicesImpl.teacherqueryByid(teacherFront.getId());

        System.out.println(userLogin1.getTeachSname());
        if(userLogin1 != null){ //  如果查询的用户不为空
            System.out.println("LoginSuccess");
            req.getSession().setAttribute("name", userLogin1.getTeachName());
            req.getSession().setAttribute("id", userLogin1.getTeachSname());
            req.getSession().setAttribute("teachid", userLogin1.getTeacherPassword());
//           model.addAttribute("name",userLogin1.getName());
//           System.out.println("getName"+model.getAttribute("name").toString());
            System.out.println(userLogin1.getTeacherPassword()+" "+teacherFront.getPassword());
            if (userLogin1.getTeacherPassword().equals(teacherFront.getPassword())){
                return "2";
            }else{
                return "error" ;
            }
        }
        else{
            //返回到登录页面
            req.getSession().setAttribute("data","该用户不存在，请先注册");
            return "retrun";
        }
    }

    @RequestMapping("/getName")
    public String getName(Model model,HttpServletRequest req, HttpServletResponse resp, UserFront userfront){
        //先查询看该用户名是否存在

//        System.out.println(req.getSession().getAttribute("name"));
//        System.out.println("getName"+model.getAttribute("name").toString());
        return req.getSession().getAttribute("name").toString();

    }

    @RequestMapping("/getSname")
    public String getSname(HttpServletRequest req, HttpServletResponse resp){
        return req.getSession().getAttribute("id").toString();
    }

    @RequestMapping("/Register")
    public int Register(HttpServletRequest req, HttpServletResponse resp,String id,String name,String password){
        System.out.println("Register:id"+id+" ,name:"+name);
        req.getSession().setAttribute("id",id);
        req.getSession().setAttribute("name",name);
        userLoginServicesImpl.addTeacher(id,name,password);

        return 1;
    }
    @RequestMapping("/Register_teacher")
    public int Register_teacher(HttpServletRequest req, HttpServletResponse resp,TeacherBack teacherBack){
        System.out.println("Register:id"+teacherBack.getTeachSname()+" ,name:"+teacherBack.getTeachName());
        req.getSession().setAttribute("id",teacherBack.getTeachSname());
        req.getSession().setAttribute("name",teacherBack.teachName);
        userLoginServicesImpl.addTeacher(teacherBack.teachSname,teacherBack.teachName,teacherBack.teacherPassword);
        System.out.println("Register_teacher: "+teacherBack.getTeachSname());
        return 1;
    }
    @RequestMapping("/Register_reset")
    public int Register_reset(HttpServletRequest req, HttpServletResponse resp ,Studentfront studentfront){
        System.out.println("Register:id"+studentfront.getId());
        TeacherBack teacherBack = userLoginServicesImpl.teacherqueryByid(studentfront.getId());
        req.getSession().setAttribute("id",teacherBack.getTeachSname());
        req.getSession().setAttribute("name",teacherBack.getTeachName());
        userLoginServicesImpl.updateTeacher(studentfront.getId(),studentfront.getPassword());
        System.out.println("Register_reset："+studentfront.getId());
        return 1;
    }

}
