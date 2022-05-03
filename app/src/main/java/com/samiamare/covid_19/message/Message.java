package com.samiamare.covid_19.message;

public class Message {
    String message = null;
    String uid = null;

    public Message(String message, String uid){
        this.message = message;
        this.uid = uid;
    }
    public String getMessage() {
        return message;
    }

    public String getUid() {
        return uid;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
