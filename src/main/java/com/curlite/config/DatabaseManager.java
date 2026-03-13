package com.curlite.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./data/curlite;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void initSchema() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS collections ("
                + "id IDENTITY PRIMARY KEY,"
                + "name VARCHAR(255) NOT NULL,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")");

            statement.execute("CREATE TABLE IF NOT EXISTS requests ("
                + "id IDENTITY PRIMARY KEY,"
                + "collection_id BIGINT NOT NULL,"
                + "name VARCHAR(255) NOT NULL,"
                + "method VARCHAR(16) NOT NULL,"
                + "url VARCHAR(2000) NOT NULL,"
                + "headers CLOB,"
                + "body CLOB,"
                + "CONSTRAINT fk_collection FOREIGN KEY(collection_id) REFERENCES collections(id) ON DELETE CASCADE"
                + ")");

            statement.execute("CREATE TABLE IF NOT EXISTS history ("
                + "id IDENTITY PRIMARY KEY,"
                + "method VARCHAR(16) NOT NULL,"
                + "url VARCHAR(2000) NOT NULL,"
                + "status INT NOT NULL,"
                + "response_time BIGINT NOT NULL,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize schema", e);
        }
    }
}
