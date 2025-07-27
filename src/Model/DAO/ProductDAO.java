package Model.DAO;

import DB.JDBC;
import Model.POJO.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDAO {

    private static final Logger logger = Logger.getLogger(ProductDAO.class.getName());

    /**
     * Retrieves a single Product by its ID, including category name.
     */
    public static Product getById(int productId) throws SQLException {
        String sql = """
            SELECT p.*, c.category_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            WHERE p.product_id = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractProduct(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all products from the database, including their category names.
     */
    public static List<Product> getAll() throws SQLException {
        List<Product> products = new ArrayList<>();

        String sql = """
            SELECT p.*, c.category_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
        """;

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        }

        return products;
    }

    /**
     * Inserts a new product into the database and returns the product with the generated ID.
     */
    public static Product insert(Product product) throws SQLException {
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
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setProductId(rs.getInt(1));
                        return product;
                    }
                }
            }
        }

        return null;
    }
    /**
     * Updates an existing product's details in the database.
     */
    public static boolean update(Product product) throws SQLException {
        String sql = """
            UPDATE product SET
                category_id = ?,
                product_name = ?,
                description = ?,
                product_price = ?,
                stock = ?
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
        }
    }
    /**
     * Updates the stock quantity of a product by a specified amount.
     */
    public static boolean updateStock(int productId, int quantityChange) throws SQLException {
        String sql = "UPDATE product SET stock = stock + ? WHERE product_id = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantityChange);
            stmt.setInt(2, productId);

            return stmt.executeUpdate() > 0;
        }
    }
    /**
     * Deletes a product from the database by its ID.
     */
    public static boolean delete(int productId) throws SQLException {
        String sql = "DELETE FROM product WHERE product_id = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        }
    }
    /**
     * Reduces the stock of a product if sufficient quantity is available.
     * Used within a transaction with an existing connection.
     */
    public static boolean reduceStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE product SET stock = stock - ? WHERE product_id = ? AND stock >= ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        }
    }
    /**
     * Counts how many products are considered low in stock.
     * A product is low if its stock is less than or equal to 20% of the average stock in its category.
     */
    public static int getLowStockCount() {
        String sql = """
            SELECT COUNT(*) FROM product p
            JOIN (
                SELECT category_id, AVG(stock) * 0.2 AS threshold
                FROM product
                GROUP BY category_id
            ) AS thresholds
            ON p.category_id = thresholds.category_id
            WHERE p.stock <= thresholds.threshold
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting low stock count", e);
        }

        return 0;
    }
    /**
     * Retrieves the total number of products in the database.
     */
    public static int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM product";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting total product count", e);
        }

        return 0;
    }
    /**
     * Calculates the total value of all stock in the database.
     * Computed as the sum of (stock * product_price) for all products.
     */
    public static double getTotalStockValue() {
        String sql = "SELECT SUM(stock * product_price) FROM product";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting total stock value", e);
        }

        return 0.0;
    }
    /**
     * Utility method that maps a ResultSet row to a Product object.
     */
    private static Product extractProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setProductName(rs.getString("product_name"));
        product.setDescription(rs.getString("description"));
        product.setProductPrice(rs.getDouble("product_price"));
        product.setStock(rs.getInt("stock"));
        return product;
    }
}