package com.curlite.repository;

import com.curlite.config.DatabaseManager;
import com.curlite.model.History;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {

    public void insert(String method, String url, int status, long responseTime) {
        String sql = "INSERT INTO history(method, url, status, response_time) VALUES(?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, method);
            ps.setString(2, url);
            ps.setInt(3, status);
            ps.setLong(4, responseTime);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save history", e);
        }
        trimToLastFifty();
    }

    public List<History> findRecent() {
        String sql = "SELECT id, method, url, status, response_time, created_at FROM history ORDER BY created_at DESC LIMIT 50";
        List<History> histories = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                histories.add(mapRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch history", e);
        }
        return histories;
    }

    private void trimToLastFifty() {
        String sql = "DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY created_at DESC LIMIT 50)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to trim history", e);
        }
    }

    private History mapRow(ResultSet rs) throws Exception {
        History history = new History();
        history.setId(rs.getLong("id"));
        history.setMethod(rs.getString("method"));
        history.setUrl(rs.getString("url"));
        history.setStatus(rs.getInt("status"));
        history.setResponseTime(rs.getLong("response_time"));
        Timestamp created = rs.getTimestamp("created_at");
        history.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return history;
    }
}
