package DB;

import java.sql.*;

public class SaleItemDAO {
    public void insert(SaleItem si) throws SQLException {
        String sql = "INSERT INTO Sale_item (sale_id, product_id, si_date, si_qty, si_price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, si.getSaleId());
            stmt.setInt(2, si.getProductId());
            stmt.setString(3, si.getSiDate());
            stmt.setInt(4, si.getQuantity());
            stmt.setDouble(5, si.getPrice());
            stmt.executeUpdate();
        }
    }

    public void update(SaleItem si) throws SQLException {
        String sql = "UPDATE Sale_item SET sale_id=?, product_id=?, si_date=?, si_qty=?, si_price=? WHERE si_id=?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, si.getSaleId());
            stmt.setInt(2, si.getProductId());
            stmt.setString(3, si.getSiDate());
            stmt.setInt(4, si.getQuantity());
            stmt.setDouble(5, si.getPrice());
            stmt.setInt(6, si.getSiId());
            stmt.executeUpdate();
        }
    }

    public void delete(int siId) throws SQLException {
        String sql = "DELETE FROM Sale_item WHERE si_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, siId);
            stmt.executeUpdate();
        }
    }
}

