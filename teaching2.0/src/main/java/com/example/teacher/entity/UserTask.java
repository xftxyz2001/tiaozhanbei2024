package com.example.teacher.entity;

public class UserTask {
    private String word;
    private String sentence;
    private String grammer;
    private String languageSentence;
    private String talkTitle;
    private String writeTitle;

    public void setWord(String word) {
        this.word = word;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public void setGrammer(String grammer) {
        this.grammer = grammer;
    }

    public void setCourtesyWord(String courtesyWord) {
        this.courtesyWord = courtesyWord;
    }

    public void setLanguageSentence(String languageSentence) {
        this.languageSentence = languageSentence;
    }

    public void setTalkTitle(String talkTitle) {
        this.talkTitle = talkTitle;
    }

    public void setWriteTitle(String writeTitle) {
        this.writeTitle = writeTitle;
    }

    private String courtesyWord;

    public String getWord() {
        return word;
    }

    public String getSentence() {
        return sentence;
    }

    public String getGrammer() {
        return grammer;
    }

    public String getCourtesyWord() {
        return courtesyWord;
    }

    public String getLanguageSentence() {
        return languageSentence;
    }

    public String getTalkTitle() {
        return talkTitle;
    }

    public String getWriteTitle() {
        return writeTitle;
    }




}
