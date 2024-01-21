package com.example.mylovesongadminsevasit.model;

public class Artist {
    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String artistId;
    public String name;
    public String history;
    public String genre;

    public String url;

    public Artist() {
        // Default constructor required for calls to DataSnapshot.getValue(Artist.class)
    }

    public Artist(String artistId, String name, String history, String genre, String url) {
        this.artistId = artistId;
        this.name = name;
        this.history = history;
        this.genre = genre;
        this.url = url;
    }
}
