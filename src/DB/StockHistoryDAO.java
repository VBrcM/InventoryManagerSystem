package DB;

import java.sql.*;
import java.util.*;

public class StockHistoryDAO {
    public void insert(StockHistory sh) throws SQLException {
        String sql = "INSERT INTO Stock_History (product_id, sh_qty, reason, sh_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sh.getProductId());
            stmt.setInt(2, sh.getQuantity());
            stmt.setString(3, sh.getReason());
            stmt.setString(4, sh.getDate());
            stmt.executeUpdate();
        }
    }

    public void update(StockHistory sh) throws SQLException {
        String sql = "UPDATE Stock_History SET product_id=?, sh_qty=?, reason=?, sh_date=? WHERE history_id=?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sh.getProductId());
            stmt.setInt(2, sh.getQuantity());
            stmt.setString(3, sh.getReason());
            stmt.setString(4, sh.getDate());
            stmt.setInt(5, sh.getHistoryId());
            stmt.executeUpdate();
        }
    }

    public void delete(int historyId) throws SQLException {
        String sql = "DELETE FROM Stock_History WHERE history_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, historyId);
            stmt.executeUpdate();
        }
    }
}
