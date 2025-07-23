package Model.DAO;

import DB.JDBC;
import Model.POJO.Category;

import java.sql.*;
import java.util.*;

public class CategoryDAO {

    // Insert category
    public void insert(Category category) throws SQLException {
        String sql = "INSERT INTO category (category_name) VALUES (?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategoryName());
            stmt.executeUpdate();
            System.out.printf("[DEBUG] Inserted category: %s%n", category.getCategoryName());

        } catch (SQLException e) {
            System.err.printf("[ERROR] Failed to insert category: %s%n", category.getCategoryName());
            e.printStackTrace();
            throw e;
        }
    }

    // Update category
    public void update(Category category) throws SQLException {
        String sql = "UPDATE category SET category_name = ? WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategoryName());
            stmt.setInt(2, category.getCategoryId());
            stmt.executeUpdate();
            System.out.printf("[DEBUG] Updated category ID %d to: %s%n", category.getCategoryId(), category.getCategoryName());

        } catch (SQLException e) {
            System.err.printf("[ERROR] Failed to update category ID %d%n", category.getCategoryId());
            e.printStackTrace();
            throw e;
        }
    }

    // Delete category
    public void delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM category WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            stmt.executeUpdate();
            System.out.printf("[DEBUG] Deleted category with ID: %d%n", categoryId);

        } catch (SQLException e) {
            System.err.printf("[ERROR] Failed to delete category ID %d%n", categoryId);
            e.printStackTrace();
            throw e;
        }
    }

    // Get all categories
    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM category";

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category c = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                );
                categories.add(c);
                System.out.printf("[DEBUG] Loaded category: %s (ID: %d)%n", c.getCategoryName(), c.getCategoryId());
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load categories");
            e.printStackTrace();
        }

        return categories;
    }

    // Get or create category by name
    public Category getOrCreateCategoryByName(String categoryName) {
        Category category = null;
        String selectSQL = "SELECT * FROM category WHERE category_name = ?";
        String insertSQL = "INSERT INTO category (category_name) VALUES (?)";

        try (Connection conn = JDBC.connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {

            selectStmt.setString(1, categoryName);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("category_id");
                category = new Category(id, categoryName);
                System.out.printf("[DEBUG] Found existing category: %s (ID: %d)%n", categoryName, id);
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, categoryName);
                    insertStmt.executeUpdate();

                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        category = new Category(id, categoryName);
                        System.out.printf("[DEBUG] Created new category: %s (ID: %d)%n", categoryName, id);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.printf("[ERROR] getOrCreateCategoryByName failed for: %s%n", categoryName);
            e.printStackTrace();
        }

        return category;
    }

    // Stock distribution grouped by category
    public static Map<String, Integer> getStockDistributionByCategory() {
        String sql = """
            SELECT c.category_name, SUM(p.stock) AS total_stock
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            GROUP BY c.category_name
        """;

        Map<String, Integer> data = new HashMap<>();

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("[DEBUG] Fetching stock distribution by category...");

            while (rs.next()) {
                String category = rs.getString("category_name");
                int stock = rs.getInt("total_stock");
                data.put(category, stock);
                System.out.printf("[DEBUG] Category: %s, Stock: %d%n", category, stock);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch stock distribution by category");
            e.printStackTrace();
        }

        return data;
    }

    // Get all category names only
    public static List<String> getAllCategoryNames() {
        List<String> categoryNames = new ArrayList<>();
        String sql = "SELECT category_name FROM category";

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categoryNames.add(rs.getString("category_name"));
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load category names");
            e.printStackTrace();
        }

        return categoryNames;
    }
}