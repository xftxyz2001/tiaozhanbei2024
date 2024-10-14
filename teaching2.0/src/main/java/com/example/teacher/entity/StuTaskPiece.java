package com.example.teacher.entity;

public class StuTaskPiece {
    public int taskPiece_id;
    public String studentTaskId;
    public String taskPiece;

    public int getTaskPiece_id() {
        return taskPiece_id;
    }

    public void setTaskPiece_id(int taskPiece_id) {
        this.taskPiece_id = taskPiece_id;
    }

    public String getStudentTaskId() {
        return studentTaskId;
    }

    public void setStudentTaskId(String studentTaskId) {
        this.studentTaskId = studentTaskId;
    }

    public String getTaskPiece() {
        return taskPiece;
    }

    public void setTaskPiece(String taskPiece) {
        this.taskPiece = taskPiece;
    }
}
