package com.curlite.model;

public class Collection {
    private Long id;
    private String name;
    private java.time.LocalDateTime createdAt;
    private java.util.List<SavedRequest> requests = new java.util.ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.List<SavedRequest> getRequests() {
        return requests;
    }

    public void setRequests(java.util.List<SavedRequest> requests) {
        this.requests = requests;
    }
}
