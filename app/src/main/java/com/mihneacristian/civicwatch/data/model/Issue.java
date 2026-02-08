package com.mihneacristian.civicwatch.data.model;

import java.util.Date;

public class Issue {
    private String issueId;
    private String title;
    private String description;
    private String category;
    private String severity;
    private double latitude;
    private double longitude;
    private String address;
    private String photoBase64;
    private String reporterId;
    private String reporterName;
    private String reporterEmail;
    private String status; // PENDING, IN_PROGRESS, RESOLVED
    private String createdAt;
    private String updatedAt;
    private int upvotes;
    private String assignedTo;

    // Required empty constructor for Firebase
    public Issue() {
    }

    // Constructor for creating new issues
    public Issue(String title, String description, String category, String severity,
                 double latitude, double longitude, String address, String photoBase64,
                 String reporterId, String reporterName, String reporterEmail) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.severity = severity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.photoBase64 = photoBase64;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.status = "PENDING";
        this.createdAt = new Date().toString();
        this.updatedAt = new Date().toString();
        this.upvotes = 0;
        this.assignedTo = "";
    }

    // Getters and Setters
    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}