package com.curlite.repository;

import com.curlite.config.DatabaseManager;
import com.curlite.model.Collection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CollectionRepository {

    public long create(String name) {
        String sql = "INSERT INTO collections(name) VALUES(?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create collection", e);
        }
        throw new IllegalStateException("No id generated");
    }

    public List<Collection> findAll() {
        String sql = "SELECT id, name, created_at FROM collections ORDER BY created_at DESC";
        List<Collection> collections = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                collections.add(mapRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load collections", e);
        }
        return collections;
    }

    public Optional<Collection> findById(long id) {
        String sql = "SELECT id, name, created_at FROM collections WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load collection", e);
        }
        return Optional.empty();
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM collections WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete collection", e);
        }
    }

    private Collection mapRow(ResultSet rs) throws Exception {
        Collection collection = new Collection();
        collection.setId(rs.getLong("id"));
        collection.setName(rs.getString("name"));
        Timestamp created = rs.getTimestamp("created_at");
        collection.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return collection;
    }
}
