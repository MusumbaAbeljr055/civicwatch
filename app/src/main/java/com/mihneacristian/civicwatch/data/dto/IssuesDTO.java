package com.mihneacristian.civicwatch.data.dto;

import com.google.gson.annotations.SerializedName;

public class IssuesDTO {

    @SerializedName("issueTitle")
    private final String issueTitle;

    @SerializedName("issueDescription")
    private final String issueDescription;

    @SerializedName("issueType")
    private String issueType;

    @SerializedName("issueSeverity")
    private String issueSeverity;

    @SerializedName("dateAdded")
    private String dateAdded;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    @SerializedName("userName")
    private String userName;

    @SerializedName("userEmailAddress")
    private String userEmailAddress;

    @SerializedName("photoBase64")  // Changed from photoURL to photoBase64
    private String photoBase64;

    // Constructor
    public IssuesDTO(String issueTitle, String issueDescription, String issueType,
                     String issueSeverity, String dateAdded, double lat, double lng,
                     String userName, String userEmailAddress, String photoBase64) {
        this.issueTitle = issueTitle;
        this.issueDescription = issueDescription;
        this.issueType = issueType;
        this.issueSeverity = issueSeverity;
        this.dateAdded = dateAdded;
        this.lat = lat;
        this.lng = lng;
        this.userName = userName;
        this.userEmailAddress = userEmailAddress;
        this.photoBase64 = photoBase64;
    }

    // Getters and Setters
    public String getIssueTitle() {
        return issueTitle;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getIssueSeverity() {
        return issueSeverity;
    }

    public void setIssueSeverity(String issueSeverity) {
        this.issueSeverity = issueSeverity;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmailAddress() {
        return userEmailAddress;
    }

    public void setUserEmailAddress(String userEmailAddress) {
        this.userEmailAddress = userEmailAddress;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }
}