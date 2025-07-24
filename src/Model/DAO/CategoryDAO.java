package Model.DAO;

import DB.JDBC;
import Model.POJO.Category;

import java.sql.*;
import java.util.*;

public class CategoryDAO {

    public static boolean insert(Category category) throws SQLException {
        String sql = "INSERT INTO category (category_name) VALUES (?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getCategoryName());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setCategoryId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static boolean update(Category category) throws SQLException {
        String sql = "UPDATE category SET category_name = ? WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategoryName());
            stmt.setInt(2, category.getCategoryId());
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM category WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static List<Category> getAll() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM category";

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                ));
            }
        }
        return categories;
    }

    public static Category getById(int categoryId) throws SQLException {
        String sql = "SELECT * FROM category WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getInt("category_id"),
                            rs.getString("category_name")
                    );
                }
            }
        }
        return null;
    }

    public static Map<String, Integer> getStockDistributionByCategory() throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = """
            SELECT c.category_name, SUM(p.stock) AS total_stock
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            GROUP BY c.category_name
        """;

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                distribution.put(
                        rs.getString("category_name"),
                        rs.getInt("total_stock")
                );
            }
        }
        return distribution;
    }

    public Category getOrCreateCategoryByName(String categoryName) {
        String selectSQL = "SELECT * FROM category WHERE category_name = ?";
        String insertSQL = "INSERT INTO category (category_name) VALUES (?)";
        Category category = null;

        try (Connection conn = JDBC.connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {

            selectStmt.setString(1, categoryName);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                category = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                );
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, categoryName);
                    int affectedRows = insertStmt.executeUpdate();

                    if (affectedRows > 0) {
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int newId = generatedKeys.getInt(1);
                            category = new Category(newId, categoryName);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getOrCreateCategoryByName: " + e.getMessage());
            e.printStackTrace();
        }
        return category;
    }

    public static List<String> getAllCategoryNames() {
        List<String> categoryNames = new ArrayList<>();
        String sql = "SELECT category_name FROM category";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categoryNames.add(rs.getString("category_name"));
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch category names: " + e.getMessage());
            e.printStackTrace();
        }

        return categoryNames;
    }

    public static boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM category WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }

    public static boolean renameCategory(int id, String newName) {
        String sql = "UPDATE category SET category_name = ? WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error renaming category: " + e.getMessage());
            return false;
        }
    }
}