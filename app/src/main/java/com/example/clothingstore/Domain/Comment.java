package com.example.clothingstore.Domain;

public class Comment {
    private String customerId;
    private int rating;
    private String content;
    private long timestamp;

    public Comment() {}

    public Comment(String customerId, int rating, String content, long timestamp) {
        this.customerId = customerId;
        this.rating = rating;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
