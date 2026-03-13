package com.curlite.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Workspace {
    private String id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WorkspaceTab> tabs = new ArrayList<>();

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<WorkspaceTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<WorkspaceTab> tabs) {
        this.tabs = tabs;
    }
}
