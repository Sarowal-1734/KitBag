package com.example.kitbag.model;

public class ModelClassMessageUser {
    private String postReference;
    private String postTitle;
    private String postImageUrl;
    private String postedById;
    private String childKeyUserId;

    public ModelClassMessageUser() {
    }

    public String getPostReference() {
        return postReference;
    }

    public void setPostReference(String postReference) {
        this.postReference = postReference;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostedById() {
        return postedById;
    }

    public void setPostedById(String postedById) {
        this.postedById = postedById;
    }

    public String getChildKeyUserId() {
        return childKeyUserId;
    }

    public void setChildKeyUserId(String childKeyUserId) {
        this.childKeyUserId = childKeyUserId;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }
}
