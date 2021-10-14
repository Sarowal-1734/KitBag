package com.example.kitbag.model;

import com.google.firebase.Timestamp;

public class ModelClassPost {
    private String imageUrl;
    private String title;
    private String weight;
    private String description;
    private String fromDistrict;
    private String fromUpazilla;
    private String toDistrict;
    private String toUpazilla;
    private Timestamp timeAdded;
    private String userId;
    private String userName;
    private String phoneNumber;
    private String email;
    private String userType;
    private String postReference;
    private String documentReference;
    private String status;
    private String lastMessage;

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPostReference() {
        return postReference;
    }

    public void setPostReference(String postReference) {
        this.postReference = postReference;
    }

    public ModelClassPost() {
    } // must for fireStore to work

    public String getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(String documentReference) {
        this.documentReference = documentReference;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFromDistrict() {
        return fromDistrict;
    }

    public void setFromDistrict(String fromDistrict) {
        this.fromDistrict = fromDistrict;
    }

    public String getFromUpazilla() {
        return fromUpazilla;
    }

    public void setFromUpazilla(String fromUpazilla) {
        this.fromUpazilla = fromUpazilla;
    }

    public String getToDistrict() {
        return toDistrict;
    }

    public void setToDistrict(String toDistrict) {
        this.toDistrict = toDistrict;
    }

    public String getToUpazilla() {
        return toUpazilla;
    }

    public void setToUpazilla(String toUpazilla) {
        this.toUpazilla = toUpazilla;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
