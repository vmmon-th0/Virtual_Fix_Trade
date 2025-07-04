package com.vmmon.database.repositories;

import com.vmmon.database.DatabaseConnection;
import com.vmmon.database.entities.ExampleEntity;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExampleRepository {

    public List<ExampleEntity> findAll() throws SQLException {
        String sql = "SELECT id, name FROM examples";
        List<ExampleEntity> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(new ExampleEntity(
                        rs.getLong("id"),
                        rs.getString("name")
                ));
            }
        }
        return results;
    }

    public void insert(ExampleEntity entity) throws SQLException {
        String sql = "INSERT INTO examples (name) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getName());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
        }
    }
}