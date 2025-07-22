package DB;

import java.sql.*;
import java.util.*;

public class CategoryDAO {

    // Inserts a new category into the database
    public void insert(Category category) throws SQLException {
        String sql = "INSERT INTO category (category) VALUES (?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategory());
            stmt.executeUpdate();
            System.out.println("[DEBUG] Inserted category: " + category.getCategory());
        }
    }

    // Updates an existing category in the database
    public void update(Category category) throws SQLException {
        String sql = "UPDATE category SET category = ? WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategory());
            stmt.setInt(2, category.getCategoryId());
            stmt.executeUpdate();
            System.out.println("[DEBUG] Updated category ID " + category.getCategoryId() + " to: " + category.getCategory());
        }
    }

    // Deletes a category by its ID
    public void delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM category WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            stmt.executeUpdate();
            System.out.println("[DEBUG] Deleted category with ID: " + categoryId);
        }
    }

    // Returns a list of all categories from the database
    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM category")) {

            while (rs.next()) {
                Category c = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category") // Fixed: was "name"
                );
                System.out.println("[DEBUG] Loaded category: " + c.getCategory());
                categories.add(c);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load categories: " + e.getMessage());
        }

        return categories;
    }

    // Gets a category by name; creates it if it does not exist
    public Category getOrCreateCategoryByName(String categoryName) {
        Category category = null;
        String selectSQL = "SELECT * FROM category WHERE category = ?";
        String insertSQL = "INSERT INTO category (category) VALUES (?)";

        try (Connection conn = JDBC.connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {

            selectStmt.setString(1, categoryName);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("category_id");
                category = new Category(id, categoryName);
                System.out.println("[DEBUG] Found existing category: " + categoryName + " (ID: " + id + ")");
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, categoryName);
                    insertStmt.executeUpdate();

                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        category = new Category(id, categoryName);
                        System.out.println("[DEBUG] Created new category: " + categoryName + " (ID: " + id + ")");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] getOrCreateCategoryByName failed: " + e.getMessage());
        }

        return category;
    }

    public static Map<String, Integer> getStockDistributionByCategory() {
        String sql = """
            SELECT c.category, SUM(p.stock) AS total_stock
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            GROUP BY c.category
        """;

        Map<String, Integer> data = new HashMap<>();

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("[DEBUG] Fetching stock distribution by category...");

            while (rs.next()) {
                String category = rs.getString("category");
                int stock = rs.getInt("total_stock");
                data.put(category, stock);
                System.out.println("[DEBUG] Category: " + category + ", Stock: " + stock);
            }

        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to fetch stock distribution.");
            e.printStackTrace();
        }

        return data;
    }

    public static List<String> getAllCategoryNames() {
        List<String> categoryNames = new ArrayList<>();

        String sql = "SELECT category FROM category";
        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categoryNames.add(rs.getString("category"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categoryNames;
    }
}
