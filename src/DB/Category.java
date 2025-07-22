package DB;

import java.util.*;
import java.sql.*;

public class Category {
    private int categoryId;
    private String category;

    // Initializes a blank Category object
    public Category() {}

    // Initializes a Category object with provided id and name
    public Category(int id, String name) {
        this.categoryId = id;
        this.category = name;
    }

    // Fetches all categories from the database
    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM category";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategory(rs.getString("category"));
                System.out.println("[DEBUG] Category fetched: ID = " + c.getCategoryId() + ", Name = " + c.getCategory());
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to retrieve categories: " + e.getMessage());
            throw e;
        }

        return list;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return category;
    }
}