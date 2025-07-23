package Model.DAO;

import DB.JDBC;
import Model.POJO.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Insert product
    public Product insert(Product p) {
        String sql = "INSERT INTO product (product_name, category_id, stock, product_price) VALUES (?, ?, ?, ?)";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, p.getProductName());
            stmt.setInt(2, p.getCategoryId());
            stmt.setInt(3, p.getStock());
            stmt.setDouble(4, p.getProductPrice());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setProductId(rs.getInt(1)); // Set generated ID
                    }
                }
            }

            return p;

        } catch (SQLException e) {
            e.printStackTrace(); // Log properly in production
            return null;
        }
    }


    // Update product
    public void update(Product product) throws SQLException {
        String sql = """
            UPDATE product
            SET category_id = ?, product_name = ?, description = ?, product_price = ?, stock = ?
            WHERE product_id = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getDescription());
            stmt.setDouble(4, product.getProductPrice());
            stmt.setInt(5, product.getStock());
            stmt.setInt(6, product.getProductId());

            stmt.executeUpdate();
            System.out.printf("[DEBUG] Updated product ID %d%n", product.getProductId());

        } catch (SQLException e) {
            System.err.printf("[ERROR] Failed to update product ID: %d%n", product.getProductId());
            e.printStackTrace();
            throw e;
        }
    }

    // Delete product
    public void delete(int productId) throws SQLException {
        String sql = "DELETE FROM product WHERE product_id = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.executeUpdate();
            System.out.printf("[DEBUG] Deleted product with ID: %d%n", productId);

        } catch (SQLException e) {
            System.err.printf("[ERROR] Failed to delete product ID: %d%n", productId);
            e.printStackTrace();
            throw e;
        }
    }

    // Get all products with category
    public List<Product> getAllWithCategory() {
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT p.*, c.category_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
        """;

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load products with categories");
            e.printStackTrace();
        }

        return list;
    }

    // Get product by ID
    public Product getById(int productId) {
        String sql = "SELECT * FROM product WHERE product_id = ?";
        Product product = null;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                product = mapResultSetToProduct(rs);
            }

        } catch (SQLException e) {
            System.err.printf("[ERROR] Failed to fetch product ID: %d%n", productId);
            e.printStackTrace();
        }

        return product;
    }

    // Search by name
    public List<Product> searchByName(String keyword) {
        List<Product> results = new ArrayList<>();
        String sql = """
            SELECT p.*, c.category_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            WHERE p.product_name LIKE ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            System.err.printf("[ERROR] Failed to search products by name: %s%n", keyword);
            e.printStackTrace();
        }

        return results;
    }

    // Update stock only
    public boolean updateStock(int productId, int newStock) {
        String sql = "UPDATE product SET stock = ? WHERE product_id = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            int rows = stmt.executeUpdate();
            System.out.printf("[DEBUG] Updated stock for product ID %d to %d%n", productId, newStock);
            return rows > 0;

        } catch (SQLException e) {
            System.err.printf("[ERROR] Failed to update stock for product ID: %d%n", productId);
            e.printStackTrace();
        }

        return false;
    }

    // ========== PRIVATE MAPPER METHOD ==========
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setProductName(rs.getString("product_name"));
        p.setDescription(rs.getString("description"));
        p.setProductPrice(rs.getDouble("product_price"));
        p.setStock(rs.getInt("stock"));

        try {
            p.setCategoryName(rs.getString("category_name")); // optional if joined
        } catch (SQLException ignored) {
            // No category_name column when not joined, so we ignore it silently
        }

        return p;
    }

    public static int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM product";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging in production
        }
        return 0;
    }

    public static double getTotalStockValue() {
        String sql = "SELECT SUM(stock * product_price) AS total_value FROM product";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total_value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static int getOutOfStockCount() {
        String sql = "SELECT COUNT(*) FROM product WHERE stock = 0";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}