package com.example.clothingstore;

public class Comment {
    private String userId;
    private String username;
    private int rating;
    private String comment;
    private long timestamp;

    public Comment() {} // Firebase cần constructor rỗng

    public Comment(String userId, String username, int rating, String comment, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}

