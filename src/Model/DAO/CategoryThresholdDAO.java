package Model.DAO;

import DB.JDBC;
import Model.POJO.CategoryThreshold;

import java.sql.*;
import java.util.*;

public class CategoryThresholdDAO {

    // 1. Load all thresholds into a Map for quick lookup
    public static Map<String, Integer> loadThresholdMap() {
        Map<String, Integer> map = new HashMap<>();
        String sql = """
            SELECT c.category_name, COALESCE(t.threshold, 0) AS threshold
            FROM category c
            LEFT JOIN category_thresholds t ON c.category_id = t.category_id
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString("category_name"), rs.getInt("threshold"));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging if needed
        }
        return map;
    }

    // 2. Get full list of thresholds for use in tables/forms
    public Map<Integer, Integer> getAllThresholds() {
        Map<Integer, Integer> map = new HashMap<>();
        String sql = "SELECT category_id, threshold FROM category_thresholds";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int categoryId = rs.getInt("category_id");
                int threshold = rs.getInt("threshold");
                map.put(categoryId, threshold);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Replace with logger if needed
        }

        return map;
    }

    // 3. Insert or update threshold per category
    public static boolean saveOrUpdateThreshold(int categoryId, int threshold) {
        String sql = """
            INSERT INTO category_thresholds (category_id, threshold)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE threshold = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            stmt.setInt(2, threshold);
            stmt.setInt(3, threshold);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
