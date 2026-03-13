package com.curlite.repository;

import com.curlite.config.DatabaseManager;
import com.curlite.model.SavedRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestRepository {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<SavedRequest> findByCollectionId(long collectionId) {
        String sql = "SELECT id, collection_id, name, method, url, headers, body FROM requests WHERE collection_id = ? ORDER BY id";
        List<SavedRequest> requests = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, collectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load requests", e);
        }
        return requests;
    }

    public void saveForCollection(long collectionId, List<SavedRequest> requests) {
        deleteByCollectionId(collectionId);
        if (requests == null || requests.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO requests(collection_id, name, method, url, headers, body) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (SavedRequest request : requests) {
                ps.setLong(1, collectionId);
                ps.setString(2, request.getName() == null ? "Untitled" : request.getName());
                ps.setString(3, request.getMethod() == null ? "GET" : request.getMethod().toUpperCase());
                ps.setString(4, request.getUrl());
                ps.setString(5, objectMapper.writeValueAsString(request.getHeaders() == null ? Collections.emptyMap() : request.getHeaders()));
                ps.setString(6, request.getBody());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save requests", e);
        }
    }

    private void deleteByCollectionId(long collectionId) {
        String sql = "DELETE FROM requests WHERE collection_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, collectionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete requests", e);
        }
    }

    private SavedRequest mapRow(ResultSet rs) throws Exception {
        SavedRequest request = new SavedRequest();
        request.setId(rs.getLong("id"));
        request.setCollectionId(rs.getLong("collection_id"));
        request.setName(rs.getString("name"));
        request.setMethod(rs.getString("method"));
        request.setUrl(rs.getString("url"));
        String headers = rs.getString("headers");
        request.setHeaders(headers == null ? Collections.emptyMap() : objectMapper.readValue(headers, new TypeReference<java.util.Map<String, String>>() { }));
        request.setBody(rs.getString("body"));
        return request;
    }
}
