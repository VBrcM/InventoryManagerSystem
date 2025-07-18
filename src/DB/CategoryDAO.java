package DB;

import java.sql.*;
import java.util.*;

public class CategoryDAO {
    public void insert(Category category) throws SQLException {
        String sql = "INSERT INTO category (category) VALUES (?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getCategory());
            stmt.executeUpdate();
        }
    }

    public void update(Category category) throws SQLException {
        String sql = "UPDATE category SET category = ? WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getCategory());
            stmt.setInt(2, category.getCategoryId());
            stmt.executeUpdate();
        }
    }

    public void delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM Category WHERE category_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.executeUpdate();
        }
    }

    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM category")) {

            while (rs.next()) {
                Category c = new Category(
                        rs.getInt("category_id"),
                        rs.getString("name")
                );
                categories.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }
    public Category getOrCreateCategoryByName(String categoryName) {
        Category category = null;
        String selectSQL = "SELECT * FROM category WHERE category = ?";
        String insertSQL = "INSERT INTO category (category) VALUES (?)";

        try (Connection conn = JDBC.connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {

            selectStmt.setString(1, categoryName);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                // Found existing
                int id = rs.getInt("category_id");
                category = new Category(id, categoryName);
            } else {
                // Not found, insert new
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, categoryName);
                    insertStmt.executeUpdate();

                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        category = new Category(id, categoryName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return category;
    }

}
