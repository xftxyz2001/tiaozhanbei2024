package com.example.teacher.entity;

public class TaskDetail {
    private String id;
    private String QSsentence;
    private int turncount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQSsentence() {
        return QSsentence;
    }

    public void setQSsentence(String QSsentence) {
        this.QSsentence = QSsentence;
    }

    public int getTurncount() {
        return turncount;
    }

    public void setTurncount(int turncount) {
        this.turncount = turncount;
    }
}
