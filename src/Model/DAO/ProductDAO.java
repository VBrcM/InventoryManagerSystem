package Model.DAO;

import DB.JDBC;
import Model.POJO.Product;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDAO {
    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    public Product getById(int productId) {
        String sql = """
            SELECT p.*, c.category_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            WHERE product_id = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = extractProduct(rs);
                Map<Integer, Integer> thresholds = new CategoryThresholdDAO().getAllThresholds();
                product.setThreshold(thresholds.getOrDefault(product.getCategoryId(), 0));
                return product;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching product by ID: " + productId, e);
        }

        return null;
    }

    public static List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT p.*, c.category_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Map<Integer, Integer> thresholds = new CategoryThresholdDAO().getAllThresholds();

            while (rs.next()) {
                Product product = extractProduct(rs);
                product.setThreshold(thresholds.getOrDefault(product.getCategoryId(), 0));
                list.add(product);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all products", e);
        }

        return list;
    }

    public List<Product> searchByName(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT p.*, c.category_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            WHERE LOWER(product_name) LIKE ? OR LOWER(description) LIKE ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String like = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, like);
            stmt.setString(2, like);

            ResultSet rs = stmt.executeQuery();
            Map<Integer, Integer> thresholds = new CategoryThresholdDAO().getAllThresholds();

            while (rs.next()) {
                Product product = extractProduct(rs);
                product.setThreshold(thresholds.getOrDefault(product.getCategoryId(), 0));
                list.add(product);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching products", e);
        }

        return list;
    }

    public Product insert(Product product) {
        String sql = """
        INSERT INTO product (category_id, product_name, description, product_price, stock)
        VALUES (?, ?, ?, ?, ?)
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getDescription());
            stmt.setDouble(4, product.getProductPrice());
            stmt.setInt(5, product.getStock());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    product.setProductId(keys.getInt(1));
                    return product;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting product", e);
        }

        return null;
    }


    public boolean update(Product product) {
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

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating product", e);
            return false;
        }
    }

    public static void updateStock(int productId, int newStock) {
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement("UPDATE product SET stock = ? WHERE product_id = ?")) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean delete(int productId) {
        String sql = "DELETE FROM product WHERE product_id = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting product", e);
            return false;
        }
    }

    private static Product extractProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setProductName(rs.getString("product_name"));
        product.setDescription(rs.getString("description"));
        product.setProductPrice(rs.getDouble("product_price"));
        product.setStock(rs.getInt("stock"));
        product.setCategoryName(rs.getString("category_name"));
        return product;
    }

    public int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM product";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total product count", e);
        }
        return 0;
    }

    public double getTotalStockValue() {
        String sql = "SELECT SUM(stock * product_price) FROM product";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total stock value", e);
        }
        return 0.0;
    }

    public int getOutOfStockCount() {
        String sql = "SELECT COUNT(*) FROM product WHERE stock = 0";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting out-of-stock count", e);
        }
        return 0;
    }

    public static void reduceStock(int productId, int quantity) throws SQLException {
        String sql = "UPDATE product SET stock = stock - ? WHERE product_id = ? AND stock >= ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity); // Ensure stock won't go negative

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to reduce stock. Insufficient stock or product not found.");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reducing stock for product ID {0}: {1}",
                    new Object[]{productId, e.getMessage()});
            throw e;
        }
    }

}