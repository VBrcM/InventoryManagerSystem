package DB;

import java.sql.*;
import java.util.*;

public class ProductDAO {

    // Inserts a new product and returns it with the generated ID
    public Product insert(Product product) throws SQLException {
        String sql = "INSERT INTO product (product, category_id, p_price, stock, description) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getProduct());
            stmt.setInt(2, product.getCategoryId());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getDescription());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                product.setProductId(rs.getInt(1));
            }
            return product;
        }
    }

    // Updates an existing product
    public void update(Product p) throws SQLException {
        String sql = "UPDATE Product SET category_id=?, product=?, description=?, p_price=?, stock=?, reorder_level=?, status=? WHERE product_id=?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, p.getCategoryId());
            stmt.setString(2, p.getProduct());
            stmt.setString(3, p.getDescription());
            stmt.setDouble(4, p.getPrice());
            stmt.setInt(5, p.getStock());
            stmt.setInt(6, p.getReorderLevel());
            stmt.setString(7, p.getStatus());
            stmt.setInt(8, p.getProductId());
            stmt.executeUpdate();
        }
    }

    // Deletes a product by ID
    public static boolean delete(int productId) {
        String sql = "DELETE FROM product WHERE product_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Debug: Failed to delete product with ID " + productId);
            e.printStackTrace();
            return false;
        }
    }

    // Retrieves all products with their category names
    public List<Product> getAllWithCategory() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.category " +
                "FROM product p " +
                "JOIN category c ON p.category_id = c.category_id";

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category"));
                p.setProduct(rs.getString("product"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("p_price"));
                p.setStock(rs.getInt("stock"));
                p.setReorderLevel(rs.getInt("reorder_level"));
                p.setStatus(rs.getString("status"));
                p.setStatus(rs.getString("category")); // overwrite with category name

                list.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Debug: Failed to retrieve products with categories.");
            e.printStackTrace();
        }

        return list;
    }

    // Returns the total number of products
    public static int getTotalProducts() {
        int total = 0;
        String query = "SELECT COUNT(*) FROM Product";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) total = rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Debug: Failed to count total products.");
            e.printStackTrace();
        }
        return total;
    }

    // Calculates the total value of all stock (price × quantity)
    public static double getTotalStockValue() {
        double total = 0;
        String query = "SELECT SUM(stock * p_price) FROM Product";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) total = rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("Debug: Failed to calculate total stock value.");
            e.printStackTrace();
        }
        return total;
    }

    // Counts how many products are out of stock
    public static int getOutOfStockCount() {
        int total = 0;
        String query = "SELECT COUNT(*) FROM Product WHERE stock = 0";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) total = rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Debug: Failed to count out-of-stock products.");
            e.printStackTrace();
        }
        return total;
    }
}
