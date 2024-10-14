package com.example.teacher.controller;
import com.alibaba.fastjson.JSON;
import com.example.teacher.entity.*;
import com.example.teacher.services.StudentSearchServicesImpl;
import com.example.teacher.services.TaskSubmitServicesImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping(value="/getTask")
public class GetTask {
    @Autowired
    TaskSubmitServicesImpl taskSubmitServicesImpl;
    @Autowired
    StudentSearchServicesImpl studentSearchServices;

    @RequestMapping("/getAll")
    public List<TaskOne> LoginSuccess(HttpServletRequest req, HttpServletResponse resp){
//            System.out.println("GetAll"+  i);
            System.out.println("gettaskAll");
            String id= (String) req.getSession().getAttribute("id");
//            id="onePrice";
            System.out.println("一共找出来"+taskSubmitServicesImpl.searchByid(id).size()+"个");
            return taskSubmitServicesImpl.searchByid(id);
    }

    @RequestMapping("/getAllfinish")
    public List<TaskOne> getAllfinish(HttpServletRequest req, HttpServletResponse resp){
//            System.out.println("GetAll"+  i);
        System.out.println("gettaskAll");
        String id= (String) req.getSession().getAttribute("id");
//            id="onePrice";
        System.out.println("一共找出来"+taskSubmitServicesImpl.searchfinishByid(id).size()+"个");
        return taskSubmitServicesImpl.searchfinishByid(id);
    }

    @RequestMapping("/getStuTasks")
    public List<studentTaskOne> getStuTasks(HttpServletRequest req, HttpServletResponse resp,String studentSname){
        String stuSname=req.getSession().getAttribute("id").toString();
        return studentSearchServices.getStuTasksByStuSname(stuSname);
    }

    @RequestMapping("/getStuTasksPieces")
    public List<StuTaskPiece> getStuTasksPieces(HttpServletRequest req, HttpServletResponse resp,String studentSname){
        String stuSname=req.getSession().getAttribute("id").toString();
        return studentSearchServices.getStuTaskPieceByStuSname(stuSname);
    }

    @RequestMapping("/startTask")
    public String startTask(HttpServletRequest req, HttpServletResponse resp, studentTaskOne stuTaskOne){
//        String id= (String) req.getSession().getAttribute("id");
        System.out.println("startTask ClassTask:"+stuTaskOne.getClassTaskId()+" ,studentTaskOne:"+stuTaskOne.getStudentTaskId()+"用户名"+stuTaskOne.getStudentSname());
        List<StuTaskPiece> li=studentSearchServices.queryTaskPieceByStuId(stuTaskOne);
        System.out.println("li.size():"+li.size()+" stuTaskId:"+li.get(0).getStudentTaskId());
        req.getSession().setAttribute("ClassTaskId",stuTaskOne.getClassTaskId());
        req.getSession().setAttribute("StuTaskId",li.get(0).getStudentTaskId());

//        req.getSession().setAttribute("TaskPiece",li);
//              List<TaskDetail> li=taskSubmitServicesImpl.searchDetail(taskone);
            return JSON.toJSONString(li);
    }
    @RequestMapping("/startTaskTwice")
    public String startTask(HttpServletRequest req, HttpServletResponse resp){
//        String id= (String) req.getSession().getAttribute("id");
        System.out.println("req.getSession().getAttribute(\"StuTaskId\").toString():"+req.getSession().getAttribute("StuTaskId").toString());
        List<StuTaskPiece> li=studentSearchServices.queryTaskPieceByStuTaskId(req.getSession().getAttribute("StuTaskId").toString());
        System.out.println(JSON.toJSONString(li));
        return JSON.toJSONString(li);
    }
    @RequestMapping("/initTask")
    public List<TaskDetail> initTask(HttpServletRequest req, HttpServletResponse resp, TaskDetail detail){
//          System.out.println()
        taskSubmitServicesImpl.searchDetail(detail.getId());
            return null;
    }
    @RequestMapping("/ingTask")
    public TaskOne ingTask(HttpServletRequest req, HttpServletResponse resp, TaskDetail detail){
//          System.out.println()

        return null;
    }
}
