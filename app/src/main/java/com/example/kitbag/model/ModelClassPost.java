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
    private String phoneNumber;
    private String postReference;
    private String documentReference;
    private String receiverPhoneNumber;
    private String preferredDeliverymanContact;
    private String statusCurrent;
    private String statusPrimaryAgent;
    private String statusDeliveryman;
    private String statusFinalAgent;

    public String getStatusPrimaryAgent() {
        return statusPrimaryAgent;
    }

    public void setStatusPrimaryAgent(String statusPrimaryAgent) {
        this.statusPrimaryAgent = statusPrimaryAgent;
    }

    public String getStatusDeliveryman() {
        return statusDeliveryman;
    }

    public void setStatusDeliveryman(String statusDeliveryman) {
        this.statusDeliveryman = statusDeliveryman;
    }

    public String getStatusFinalAgent() {
        return statusFinalAgent;
    }

    public void setStatusFinalAgent(String statusFinalAgent) {
        this.statusFinalAgent = statusFinalAgent;
    }

    public String getPreferredDeliverymanContact() {
        return preferredDeliverymanContact;
    }

    public void setPreferredDeliverymanContact(String preferredDeliverymanContact) {
        this.preferredDeliverymanContact = preferredDeliverymanContact;
    }

    public String getReceiverPhoneNumber() {
        return receiverPhoneNumber;
    }

    public void setReceiverPhoneNumber(String receiverPhoneNumber) {
        this.receiverPhoneNumber = receiverPhoneNumber;
    }

    public String getStatusCurrent() {
        return statusCurrent;
    }

    public void setStatusCurrent(String statusCurrent) {
        this.statusCurrent = statusCurrent;
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
}
