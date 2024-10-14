package com.example.teacher.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.teacher.entity.*;
import com.example.teacher.services.StudentSearchServicesImpl;
import com.example.teacher.services.TaskSubmitServicesImpl;


import okhttp3.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import java.io.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping(value="/submit")
public class SubmitController
{
    QuestionGet questionGet;
    @Autowired
    TaskSubmitServicesImpl taskSubmitServicesImpl;
    @Autowired
    StudentSearchServicesImpl studentSearchServicesImpl;

    public static final String API_KEY = "jkMIfuu1ClGgwOCS5GEyODzc";
    public static final String SECRET_KEY = "6yCY23LorP3GcKcCjAFkzlmmL4NpqxBZ";
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();
    public static String getId20(){
        Random random=new Random();
        long currentTime = System.currentTimeMillis() ;
        random.setSeed(currentTime);
        String id="";
        for(int i=0;i<20;i++){
            id=id+String.valueOf(random.nextInt(10));
        }
        return id;
    }
    public static String getId4(){
        Random random=new Random();
        long currentTime = System.currentTimeMillis() ;
        random.setSeed(currentTime);
        String id="";
        for(int i=0;i<4;i++){
            id=id+String.valueOf(random.nextInt(10));
        }
        return id;
    }
    @RequestMapping("/getAllStu")
    public String getAllStu(HttpServletRequest req, HttpServletResponse resp){
        System.out.println( JSON.toJSONString(taskSubmitServicesImpl.searchAllStudent()));
        return JSON.toJSONString(taskSubmitServicesImpl.searchAllStudent());
    }
    @RequestMapping("/get_push")
    public String LoginSuccess(HttpServletRequest req, HttpServletResponse resp, ClassTask classTask)  {
        System.out.println("get_push");
        System.out.println(classTask.getClassId());

        //先查询看该用户名是否存在

        Random random=new Random();
        long currentTime = System.currentTimeMillis() ;
        random.setSeed(currentTime);


        TaskOne task=new TaskOne();
        teacherClass teach=new teacherClass();

        task.setCompositionTitle(classTask.getCompositionTitle());
        task.setCompositionWord(classTask.getCompositionWord());
        task.setCompositionSentence(classTask.getCompositionSentence());
        task.setCompositionWordStep(classTask.getCompositionWordStep());
        task.setCompositionSentenceStep(classTask.getCompositionSentenceStep());

        task.setClassId(classTask.getClassId());

        task.setSceneTitle(classTask.getSceneTitle());
        task.setSceneWord(classTask.getSceneWord());
        task.setSceneSentence(classTask.getSceneSentence());
        task.setSceneSentenceStep(classTask.getSceneSentenceStep());
        task.setSceneWordStep(classTask.getSceneWordStep());

        task.setSceneFirstNum(classTask.getSceneFirstNum());
        task.setSceneFirstOrientation(classTask.getSceneFirstOrientation());
        task.setSceneSecondNum(classTask.getSceneSecondNum());
        task.setSceneThirdNum(classTask.getSceneThirdNum());
        task.setSceneThirdOrientation(classTask.getSceneThirdOrientation());
        task.setSceneSecondOrientation(classTask.getSceneSecondOrientation());
        task.setPicture(classTask.getPicture());
        System.out.println("基本设置完毕");
        int classtemp=0;

        teach.setId(String.valueOf(req.getSession().getAttribute("teachid")));
        System.out.println(req.getSession().getAttribute("teachid").toString());
        task.setClassTaskId(this.getId20());
        taskSubmitServicesImpl.Submit(task);
        List<classStudent> li=taskSubmitServicesImpl.searchstuByclassid(task.getClassId());
        studentTaskOne stuTask=new studentTaskOne();
        stuTask.setClassTaskId(task.getClassTaskId());
        stuTask.setFinishCheck(0);
        stuTask.setFirstScore(0);
        stuTask.setSecondScore(0);
        stuTask.setThirdScore(0);
        //设置获取问题。

        String question;
        StuTaskPiece piece;
        for(int i=0;i<li.size();i++){
            stuTask.setStudentSname(li.get(i).getStudentSname());
            String studenttaskid=task.getClassTaskId()+getId4();
            stuTask.setStudentTaskId(studenttaskid);
            question=GetQuestion(task,stuTask);
            taskSubmitServicesImpl.submitStudent(stuTask);
            //在每一个记录中添加第一条问题
            piece=new StuTaskPiece();
            piece.setStudentTaskId(stuTask.getStudentTaskId());
            piece.setTaskPiece_id(1);
            piece.setTaskPiece(question);
            System.out.println("第"+i+"个"+" "+piece.getStudentTaskId());
            taskSubmitServicesImpl.inTaskPiece(piece);
        }
        return "2";

    }
    @RequestMapping("/SubmitAnswer")
    public String SubmitAnswer(HttpServletRequest req, HttpServletResponse resp,StuTaskPiece stuTaskPiece){
        System.out.println("回答<"+stuTaskPiece.studentTaskId+">的内容为:"+stuTaskPiece.taskPiece);
        List<StuTaskPiece> li=studentSearchServicesImpl.queryTaskPieceByStuTaskId(req.getSession().getAttribute("StuTaskId").toString());
        TaskOne taskOne=studentSearchServicesImpl.queryClassTaskByClassTaskId(req.getSession().getAttribute("ClassTaskId").toString());
        //再写上一个评价
        String Evaluetion=GetEvaluation(taskOne,studentSearchServicesImpl.querystuTaskOneByStuTaskId(req.getSession().getAttribute("StuTaskId").toString()),stuTaskPiece,li);
        StuTaskPiece TaskPiece=new StuTaskPiece();
        TaskPiece.setTaskPiece_id(stuTaskPiece.getTaskPiece_id()+1);
        TaskPiece.setStudentTaskId(req.getSession().getAttribute("StuTaskId").toString());
        TaskPiece.setTaskPiece(Evaluetion);
        int evaluetionScore=0;
        int turn=(li.size()+1)/3;
        int first=Integer.parseInt(taskOne.getSceneFirstNum());
        int second=Integer.parseInt(taskOne.getSceneSecondNum());
        int third=Integer.parseInt(taskOne.getSceneThirdNum());

        if(turn==first+second+third+0){
            evaluetionScore=Integer.parseInt(Evaluetion.substring(0,1));
            studentSearchServicesImpl.UpdateStuTaskFirstScore(evaluetionScore,TaskPiece.getStudentTaskId());
        }else if(turn==first+second+third+1){
            evaluetionScore=Integer.parseInt(Evaluetion.substring(0,1));
            studentSearchServicesImpl.UpdateStuTaskSecondScore(evaluetionScore,TaskPiece.getStudentTaskId());
        }else if(turn==first+second+third+2){
            evaluetionScore=Integer.parseInt(Evaluetion.substring(0,1));
            studentSearchServicesImpl.UpdateStuTaskThirdScore(evaluetionScore,TaskPiece.getStudentTaskId());
        }
        taskSubmitServicesImpl.submitTaskPiece(stuTaskPiece);
        taskSubmitServicesImpl.submitTaskPiece(TaskPiece);
        return JSON.toJSONString(studentSearchServicesImpl.queryTaskPieceByStuTaskId(stuTaskPiece.studentTaskId));
    }
    @RequestMapping("/classstudentlist")
    public String classstudentlist(HttpServletRequest req, HttpServletResponse resp){

        return JSON.toJSONString(studentSearchServicesImpl.queryclassStudentbyTeacher(req.getSession().getAttribute("id").toString()));
    }
    @RequestMapping("/testList")
    public String testList(HttpServletRequest req, HttpServletResponse resp, testlist li){
        System.out.println(li);
        return "1";
    }

    @RequestMapping("/NextQuestionSubmit")
    public String NextQuestionSubmit(HttpServletRequest req, HttpServletResponse resp){
        System.out.println("NextQuestionSubmit");
        List<StuTaskPiece> li=studentSearchServicesImpl.queryTaskPieceByStuTaskId(req.getSession().getAttribute("StuTaskId").toString());
        String question=GetQuestion(studentSearchServicesImpl.queryClassTaskByClassTaskId(req.getSession().getAttribute("ClassTaskId").toString()),studentSearchServicesImpl.querystuTaskOneByStuTaskId(req.getSession().getAttribute("StuTaskId").toString()),li);
        StuTaskPiece newquestion=new StuTaskPiece();
        newquestion.setTaskPiece_id(li.size()+1);
        newquestion.setTaskPiece(question);
        newquestion.setStudentTaskId(req.getSession().getAttribute("StuTaskId").toString());
        taskSubmitServicesImpl.inTaskPiece(newquestion);
        return question;
    }

    @RequestMapping("/FinishTaskSubmit")
    public int  FinishTaskSubmit(HttpServletRequest req, HttpServletResponse resp){
        return taskSubmitServicesImpl.writeFinish(req.getSession().getAttribute("StuTaskId").toString());
    }

    @RequestMapping("/getPicture")
    public String getPicture(HttpServletRequest req, HttpServletResponse resp){
        System.out.println("getPicture  .");
        return JSON.toJSONString(studentSearchServicesImpl.queryClassTaskByStuTaskId(req.getSession().getAttribute("StuTaskId").toString()));
    }

    public static String AIAgantHttpPost(String str){
        try{
            MediaType mediaType = MediaType.parse("application/json");
//            String middleStr="只需要问题，请你对和‘小河’这个话题相关的曾经生活经历提出一个面向儿童的关于‘曾经怎么去’、‘曾经什么时候去’、‘曾经和谁去’的问题。";
//            String Mycontent="{\"messages\":[{\"role\":\"user\",\"content\":\"###\"}],\"temperature\":0.95,\"top_p\":0.7,\"penalty_score\":1}";
            String Mycontent = "{\"messages\":[{\"role\":\"user\",\"content\":\""+str+"\"}],\"temperature\":0.95,\"top_p\":0.7,\"penalty_score\":1,\"system\":\"teacher\"}";
            RequestBody body = RequestBody.create(mediaType, Mycontent);
            System.out.println("字符串替换后的问题为："+Mycontent);
            Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-8k-0205?access_token=" + getAccessToken())
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = HTTP_CLIENT.newCall(request).execute();
            System.out.println("给出的回答为：");

            String temp=response.body().string();
//            System.out.println("完整的response："+temp);
            temp=temp.substring(temp.indexOf("result")+9,temp.indexOf("is_truncated")-3);
            temp = temp.replace("\\n"," ").replace("\r"," ");
            System.out.println("修正后的完整的response："+temp);
            return temp;
        }catch (IOException e){
            System.out.println("传输数据的AIAgantHttpPost出错啦！！！！！！！！！！");
            return "传输数据的AIAgantHttpPost出错";
        }

    }
    public static String GetQuestion(TaskOne taskOne,studentTaskOne stuTask){


        String history="";
        String Ori;
        String result="";
        String question;

        Ori=taskOne.getSceneFirstOrientation().split("-")[0];
        result="只需要问题，请你对和‘"+taskOne.getSceneTitle()+"’这个话题相关的曾经生活经历提出一个面向儿童的关于‘"+Ori+"’的问题。";
//        question=AIAgantHttpPost(result);
        question="为什么你在雨中玩耍，是有什么特别的原因让你觉得在雨中很特别或者很有趣吗？";
        if(0==Integer.parseInt(taskOne.getSceneWordStep())-1){
            question=question+"####"+taskOne.getSceneWord();
        }
        if(0==Integer.parseInt(taskOne.getSceneSentenceStep())-1){
            question=question+"####"+taskOne.getSceneSentence();
        }

        return question;
    }
    public static String GetQuestion(TaskOne taskOne,studentTaskOne stuTask,List<StuTaskPiece> stuTaskPieces){
        int turn=(int)(stuTaskPieces.size())/3;
        System.out.println("turn:"+turn);
        int first=Integer.parseInt(taskOne.getSceneFirstNum());
        int second=Integer.parseInt(taskOne.getSceneSecondNum());
        int third=Integer.parseInt(taskOne.getSceneThirdNum());
        String history="";
        String Ori;
        String result="";
        String question;
        if(stuTaskPieces.size()==0){
            Ori=taskOne.getSceneFirstOrientation().split("-")[0];
            result="只需要一个问题，只需要一个问题，请你对和‘"+taskOne.getSceneTitle()+"’这个话题相关的曾经生活经历,用第二人称提出一个面向儿童的关于‘"+Ori+"’的问题。";
//            question=AIAgantHttpPost(result);
            question="为什么你在雨中玩耍，是有什么特别的原因让你觉得在雨中很特别或者很有趣吗？";
        }else if(turn<first){
            Ori=taskOne.getSceneFirstOrientation().split("-")[turn];
            result="只需要一个问题，只需要一个问题，请你对和‘"+taskOne.getSceneTitle()+"’这个话题相关的曾经生活经历,用第二人称提出一个面向儿童的关于‘"+Ori+"’的问题。";
//            question=AIAgantHttpPost(result);
            question="你之前有一次在小区里玩，因为没有带伞，所以决定快点跑回家。当时发生了什么让你决定要赶紧跑回家呢？";
        }else if(turn<first+second){
            Ori=taskOne.getSceneSecondOrientation().split("-")[turn-first];
            for(int i=0;i<first;i++){
                history=history+stuTaskPieces.get(i*3+2).getTaskPiece();
            }
            result="只需要一个问题，只需要一个问题，请你对和"+taskOne.getSceneTitle()+"这个话题和'"+history+"'的曾经生活经历,用第二人称提出一个面向儿童的关于‘"+Ori+"’的问题。";
//            question=AIAgantHttpPost(result);
            question="你之前有一次在小区里玩，因为没有带伞，所以决定快点跑回家。当时发生了什么让你决定要赶紧跑回家呢？";
        }else if(turn<first+second+third){
            Ori=taskOne.getSceneThirdOrientation().split("-")[turn-first-second];
            for(int i=0;i<first+second;i++){
                history=history=stuTaskPieces.get(i*3+2).getTaskPiece();
            }
            result="只需要一个问题，只需要一个问题，请你对和'"+taskOne.getSceneTitle()+"'这个话题和'"+history+"'的曾经生活经历,用第二人称提出一个面向儿童的关于‘"+Ori+"’的问题。";
//            question=AIAgantHttpPost(result);
            question="当你们在小区里玩得很开心的时候，突然下起了大雨，你们必须赶快往家里跑，那时候你的心情是怎样的？是觉得有些失望还是觉得很刺激和有趣呢？";
        }else if(turn<first+second+third+1){
            question="请小朋友仔细观察这幅图片，结合自己的生活经历，描述一下图片中故事的起因与背景环境吧，请注意使用优秀的字词、短语会更棒呦！";
        }else if(turn<first+second+third+2){
            question="请小朋友仔细观察这幅图片，结合自己的生活经历，描述一下图片中人物正在做什么？请注意使用优秀的字词、短语会更棒呦";
        }else{
            question="请小朋友仔细观察这幅图片，结合自己的生活经历，描述一下图片中人物会想什么？感受如何？以及故事后续内容？请注意使用优秀的字词、短语会更棒呦！";
        }
        if(turn==Integer.parseInt(taskOne.getSceneWordStep())-1){
            question=question+"####"+taskOne.getSceneWord();
        }
        if(turn==Integer.parseInt(taskOne.getSceneSentenceStep())-1){
            question=question+"####"+taskOne.getSceneSentence();
        }
        if(turn==Integer.parseInt(taskOne.getCompositionWordStep())-1){
            question=question+"####"+taskOne.getCompositionWord();
        }
        if(turn==Integer.parseInt(taskOne.getCompositionSentenceStep())-1){
            question=question+"####"+taskOne.getCompositionSentence();
        }
        return question;
    }


    //给出一条评价
    public static String GetEvaluation(TaskOne taskOne,studentTaskOne stuTaskOne,StuTaskPiece stuTaskPiece,List<StuTaskPiece> OldStuTaskPiece){
        int turn=(OldStuTaskPiece.size()-1)/3;
        int first=Integer.parseInt(taskOne.getSceneFirstNum());
        int second=Integer.parseInt(taskOne.getSceneSecondNum());
        int third=Integer.parseInt(taskOne.getSceneThirdNum());
        String question="";
        String middle="";
        String evaluation="";
        if(turn<first){
            middle = "请用第二人称对故事起因问题"+OldStuTaskPiece.get(OldStuTaskPiece.size()-1).getTaskPiece()+"和儿童的回答,'"+stuTaskPiece.getTaskPiece()+"'给出评价，只需要评价。";
            evaluation=AIAgantHttpPost(middle);

        }else if(turn<first+second){
            middle = "请用第二人称对故事描述问题"+OldStuTaskPiece.get(OldStuTaskPiece.size()-1).getTaskPiece()+"和儿童的回答,'"+stuTaskPiece.getTaskPiece()+"'给出评价，只需要评价。";
            evaluation=AIAgantHttpPost(middle);
        }else if(turn<first+second+third){
            middle = "请用第二人称对故事发散问题"+OldStuTaskPiece.get(OldStuTaskPiece.size()-1).getTaskPiece()+"和儿童的回答,'"+stuTaskPiece.getTaskPiece()+"'给出评价，只需要评价。";
            evaluation=AIAgantHttpPost(middle);
        }else if(turn<first+second+third+1){
            String text="";
            for(int i=0;i<turn;i++){
                text=text+OldStuTaskPiece.get(i*3+1).getTaskPiece();
                if(i==turn-1){
                    question=OldStuTaskPiece.get(i*3+3).getTaskPiece();
                }
            }
            evaluation=ModelHttpRequestion(question,stuTaskPiece.getTaskPiece(),text,taskOne.getPicture());
        }else if(turn<first+second+third+2){
            String text="";
//            1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
//            0 1 2 3 4 5 6 7 8 9  10 11 12 13 14
//            0 0 0 1 1 1 2 2 2 3  3  3  4  4  4
            for(int i=0;i<turn-1;i++){
                text=text+OldStuTaskPiece.get(i*3+1).getTaskPiece();
                if(i==turn-2){
                    question=OldStuTaskPiece.get(i*3+6).getTaskPiece();
                }
            }
            evaluation=ModelHttpRequestion(question,stuTaskPiece.getTaskPiece(),text,taskOne.getPicture());
        }else{
            String text="";
//            1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
//            0 1 2 3 4 5 6 7 8 9  10 11 12 13 14
//            0 0 0 1 1 1 2 2 2 3  3  3  4  4  4
            for(int i=0;i<turn-2;i++){
                text=text+OldStuTaskPiece.get(i*3+1).getTaskPiece();
                if(i==turn-3){
                    question=OldStuTaskPiece.get(i*3+9).getTaskPiece();
                }
            }
            evaluation=ModelHttpRequestion(question,stuTaskPiece.getTaskPiece(),text,taskOne.getPicture())+"$$$$";
        }
        return evaluation;
    }

    public static String ModelHttpRequestion(String question,String context,String text,String img_id){
        try{
//            MediaType mediaType = MediaType.parse("application/json");
//            String middleStr="只需要问题，请你对和‘小河’这个话题相关的曾经生活经历提出一个面向儿童的关于‘曾经怎么去’、‘曾经什么时候去’、‘曾经和谁去’的问题。";
//            String Mycontent="{\"messages\":[{\"role\":\"user\",\"content\":\"###\"}],\"temperature\":0.95,\"top_p\":0.7,\"penalty_score\":1}";
//            String Mycontent = "{\"question\":\""+question+"\",\"context\":\""+context+"\",\"text\":\""+text+"\",\"img_id\":\""+img_id+"\"}";
//            RequestBody body = RequestBody.create(mediaType, Mycontent);
            System.out.println(question+";"+context+";"+text+";"+img_id);
            RequestBody  body=new FormBody.Builder().add("question", question).add("context", context).add("text", text).add("img_id", img_id).build();
//            System.out.println("字符串替换后的问题为："+Mycontent);
            Request request = new Request.Builder()
                    .url("http://0.0.0.0:6006/GetEvaluation")
                    .post(body)
                    .build();
            Response response = HTTP_CLIENT.newCall(request).execute();
            String result=response.body().string();
            System.out.println("ModelHttpRequestion给出的回答为：");
            System.out.println(result);
            return result.substring(1,result.length()-1);
        }catch (Exception e){
            System.out.println("ModelHttpRequestion出错啦！！！！！");
            return "0分，良好";
        }



    }

    static String getAccessToken() throws IOException {
//        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
//        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
//                + "&client_secret=" + SECRET_KEY);
//        Request request = new Request.Builder()
//                .url("https://aip.baidubce.com/oauth/2.0/token")
//                .method("POST", body)
//                .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                .build();
//        Response response = HTTP_CLIENT.newCall(request).execute();
//        System.out.println(response.body());
        return "24.7d20f43583888e8106f6c2c8413a7861.2592000.1717297021.282335-66138548";
    }
    public void initTask(String id){
        PictureFirstSentence pictureFirstSentence=new PictureFirstSentence();
            TaskDetail detail=new TaskDetail();
            Random random=new Random();
            List<String> li=pictureFirstSentence.getVCG1();
            detail.setQSsentence(li.get(random.nextInt(li.size())));
            detail.setId(id);
            detail.setTurncount(1);
            System.out.println("TaskOne start"+id);
//            req.getSession().setAttribute("task",taskone);
            taskSubmitServicesImpl.writeDetail(detail);
    }



}
