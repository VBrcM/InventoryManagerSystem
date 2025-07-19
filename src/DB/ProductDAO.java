package DB;

import java.sql.*;
import java.util.*;

public class ProductDAO {
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

    public static boolean delete(int productId) {
        String sql = "DELETE FROM product WHERE product_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



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

                // Store the category name as a trick using `description` or `status`
                // OR extend Product class if you prefer
                p.setStatus(rs.getString("category")); // Use 'status' field for category name

                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}
