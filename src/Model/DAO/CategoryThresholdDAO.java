package Model.DAO;

import DB.JDBC;
import java.sql.*;
import java.util.*;

public class CategoryThresholdDAO {

    public static boolean saveOrUpdate(int categoryId, int threshold) throws SQLException {
        String sql = """
            INSERT INTO category_threshold (category_id, threshold)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE threshold = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            stmt.setInt(2, threshold);
            stmt.setInt(3, threshold);
            return stmt.executeUpdate() > 0;
        }
    }

    public static Map<Integer, Integer> getAllThresholds() throws SQLException {
        Map<Integer, Integer> thresholds = new HashMap<>();
        String sql = "SELECT * FROM category_threshold";

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                thresholds.put(
                        rs.getInt("category_id"),
                        rs.getInt("threshold")
                );
            }
        }
        return thresholds;
    }

    public static Integer getThresholdByCategoryId(int categoryId) throws SQLException {
        String sql = "SELECT threshold FROM category_threshold WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("threshold");
                }
            }
        }
        return null;
    }
}