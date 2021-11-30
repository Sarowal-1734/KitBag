package com.example.kitbag.model;

import com.google.firebase.Timestamp;

public class ModelClassNotification {

    private String userId;
    private String title;
    private String message;
    private String postReference;
    private String statusCurrent;
    private Timestamp time;

    public ModelClassNotification() {
        // Must Needed empty constructor
    }

    public String getTitle() {
        return title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatusCurrent() {
        return statusCurrent;
    }

    public void setStatusCurrent(String statusCurrent) {
        this.statusCurrent = statusCurrent;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostReference() {
        return postReference;
    }

    public void setPostReference(String postReference) {
        this.postReference = postReference;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }
}
