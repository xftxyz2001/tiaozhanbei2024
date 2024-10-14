package com.example.teacher.entity;

import java.util.ArrayList;
import java.util.List;

public class PictureFirstSentence {

    private List<String> VCG1=new ArrayList<>();
    private List<String> VCG2=new ArrayList<>();
    private List<String> VCG3=new ArrayList<>();

    public List<String> getVCG1() {
        return VCG1;
    }

    public void setVCG1(List<String> VCG1) {
        this.VCG1 = VCG1;
    }

    public List<String> getVCG2() {
        return VCG2;
    }

    public void setVCG2(List<String> VCG2) {
        this.VCG2 = VCG2;
    }

    public List<String> getVCG3() {
        return VCG3;
    }

    public void setVCG3(List<String> VCG3) {
        this.VCG3 = VCG3;
    }

    public List<String> getVCG4() {
        return VCG4;
    }

    public void setVCG4(List<String> VCG4) {
        this.VCG4 = VCG4;
    }

    private List<String> VCG4;
    public PictureFirstSentence(){
        this.VCG1.add("请问小朋友之前见过小溪、小河吗？");
        this.VCG1.add("请问小朋友在小溪边玩耍过吗？");
        this.VCG1.add("请问小朋友都见过什么样的小溪吗？");

    }

}
