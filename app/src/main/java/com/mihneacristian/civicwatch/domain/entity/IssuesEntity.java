package com.mihneacristian.civicwatch.domain.entity;

public class IssuesEntity {
    private String issueTitle;
    private String dateAdded;
    private double lat;
    private double lng;
    private String photoURL;

    public IssuesEntity() {
        // Default constructor
    }

    public IssuesEntity(String issueTitle, String dateAdded, double lat, double lng, String photoURL) {
        this.issueTitle = issueTitle;
        this.dateAdded = dateAdded;
        this.lat = lat;
        this.lng = lng;
        this.photoURL = photoURL;
    }

    // Getters and Setters
    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
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

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
}