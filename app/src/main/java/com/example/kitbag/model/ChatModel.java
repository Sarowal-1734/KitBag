package com.example.kitbag.model;

import com.google.firebase.Timestamp;

public class ChatModel {
    private String sender;
    private String receiver;
    private String message;
    //private Timestamp time;
    private String status;

    public ChatModel() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

//    public Timestamp getTime() {
//        return time;
//    }
//
//    public void setTime(Timestamp time) {
//        this.time = time;
//    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
