package com.curlite.service;

import com.curlite.model.Workspace;
import com.curlite.model.WorkspaceTab;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WorkspaceService {

    private static final Path STORE = Paths.get("data", "workspaces.json");
    private final ObjectMapper objectMapper;

    public WorkspaceService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public synchronized List<Workspace> list() {
        return readAll();
    }

    public synchronized Optional<Workspace> get(String id) {
        return readAll().stream().filter(w -> w.getId().equals(id)).findFirst();
    }

    public synchronized Workspace create(Workspace incoming) {
        List<Workspace> all = readAll();
        Workspace workspace = new Workspace();
        workspace.setId(UUID.randomUUID().toString());
        workspace.setName((incoming == null || incoming.getName() == null || incoming.getName().isBlank())
            ? nextUntitled(all)
            : incoming.getName().trim());
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setUpdatedAt(LocalDateTime.now());
        workspace.setTabs(sanitizeTabs(incoming == null ? null : incoming.getTabs()));
        if (workspace.getTabs().isEmpty()) {
            workspace.getTabs().add(defaultTab());
        }
        all.add(workspace);
        writeAll(all);
        return workspace;
    }

    public synchronized Optional<Workspace> update(String id, Workspace incoming) {
        List<Workspace> all = readAll();
        for (Workspace workspace : all) {
            if (workspace.getId().equals(id)) {
                if (incoming.getName() != null && !incoming.getName().isBlank()) {
                    workspace.setName(incoming.getName().trim());
                }
                workspace.setTabs(sanitizeTabs(incoming.getTabs()));
                if (workspace.getTabs().isEmpty()) {
                    workspace.getTabs().add(defaultTab());
                }
                workspace.setUpdatedAt(LocalDateTime.now());
                writeAll(all);
                return Optional.of(workspace);
            }
        }
        return Optional.empty();
    }

    private WorkspaceTab defaultTab() {
        WorkspaceTab tab = new WorkspaceTab();
        tab.setId(UUID.randomUUID().toString());
        tab.setTitle("Untitled Tab");
        tab.setMethod("GET");
        tab.setUrl("");
        tab.setBody("");
        tab.setBearerToken("");
        tab.setHeaders(new ArrayList<>());
        tab.setParams(new ArrayList<>());
        return tab;
    }

    private List<WorkspaceTab> sanitizeTabs(List<WorkspaceTab> tabs) {
        List<WorkspaceTab> out = new ArrayList<>();
        if (tabs == null) {
            return out;
        }
        for (WorkspaceTab tab : tabs) {
            WorkspaceTab safe = new WorkspaceTab();
            safe.setId((tab.getId() == null || tab.getId().isBlank()) ? UUID.randomUUID().toString() : tab.getId());
            safe.setTitle((tab.getTitle() == null || tab.getTitle().isBlank()) ? "Untitled Tab" : tab.getTitle());
            safe.setMethod((tab.getMethod() == null || tab.getMethod().isBlank()) ? "GET" : tab.getMethod().toUpperCase());
            safe.setUrl(tab.getUrl() == null ? "" : tab.getUrl());
            safe.setBody(tab.getBody() == null ? "" : tab.getBody());
            safe.setBearerToken(tab.getBearerToken() == null ? "" : tab.getBearerToken());
            safe.setHeaders(tab.getHeaders() == null ? new ArrayList<>() : tab.getHeaders());
            safe.setParams(tab.getParams() == null ? new ArrayList<>() : tab.getParams());
            out.add(safe);
        }
        return out;
    }

    private List<Workspace> readAll() {
        ensureStore();
        try {
            byte[] bytes = Files.readAllBytes(STORE);
            if (bytes.length == 0) {
                return new ArrayList<>();
            }
            List<Workspace> data = objectMapper.readValue(bytes, new TypeReference<List<Workspace>>() { });
            data.sort(Comparator.comparing(Workspace::getUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read workspace file", e);
        }
    }

    private void writeAll(List<Workspace> all) {
        ensureStore();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(STORE.toFile(), all);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write workspace file", e);
        }
    }

    private void ensureStore() {
        try {
            if (!Files.exists(STORE.getParent())) {
                Files.createDirectories(STORE.getParent());
            }
            if (!Files.exists(STORE)) {
                Files.write(STORE, "[]".getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize workspace file", e);
        }
    }

    private String nextUntitled(List<Workspace> all) {
        int max = 0;
        for (Workspace workspace : all) {
            String name = workspace.getName();
            if (name != null && name.matches("Untitled\\d+")) {
                int n = Integer.parseInt(name.replaceAll("[^0-9]", ""));
                max = Math.max(max, n);
            }
        }
        return "Untitled" + (max + 1);
    }
}
