package com.example.mylovesongadminsevasit.model;

public class TrackClass {
    public String id;
    public String name;
    public float rating;
    public String url;

    public TrackClass() {
    }

    public TrackClass(String id, String name, float rating, String url) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
