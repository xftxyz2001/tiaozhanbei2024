package com.example.teacher.entity;

public class studentTaskOne {
    private String studentTaskId ;
    private String classTaskId ;
    private int finishCheck;
    private String studentSname ;
    private int firstScore ;
    private int secondScore ;
    private int thirdScore ;

    public String getStudentTaskId() {
        return studentTaskId;
    }

    public void setStudentTaskId(String studentTaskId) {
        this.studentTaskId = studentTaskId;
    }

    public String getClassTaskId() {
        return classTaskId;
    }

    public void setClassTaskId(String classTaskId) {
        this.classTaskId = classTaskId;
    }

    public int getFinishCheck() {
        return finishCheck;
    }

    public void setFinishCheck(int finishCheck) {
        this.finishCheck = finishCheck;
    }

    public String getStudentSname() {
        return studentSname;
    }

    public void setStudentSname(String studentSname) {
        this.studentSname = studentSname;
    }

    public int getFirstScore() {
        return firstScore;
    }

    public void setFirstScore(int firstScore) {
        this.firstScore = firstScore;
    }

    public int getSecondScore() {
        return secondScore;
    }

    public void setSecondScore(int secondScore) {
        this.secondScore = secondScore;
    }

    public int getThirdScore() {
        return thirdScore;
    }

    public void setThirdScore(int thirdScore) {
        this.thirdScore = thirdScore;
    }
}
