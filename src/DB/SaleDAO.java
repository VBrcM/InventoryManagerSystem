package DB;

import java.sql.*;
import java.util.*;

public class SaleDAO {
    public void insert(Sale s) throws SQLException {
        String sql = "INSERT INTO Sale (sale_qty, sale_date) VALUES (?, ?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, s.getSaleQty());
            stmt.setString(2, s.getSaleDate());
            stmt.executeUpdate();
        }
    }

    public void update(Sale s) throws SQLException {
        String sql = "UPDATE Sale SET sale_qty=?, sale_date=? WHERE sale_id=?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, s.getSaleQty());
            stmt.setString(2, s.getSaleDate());
            stmt.setInt(3, s.getSaleId());
            stmt.executeUpdate();
        }
    }

    public void delete(int saleId) throws SQLException {
        String sql = "DELETE FROM Sale WHERE sale_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            stmt.executeUpdate();
        }
    }
}
