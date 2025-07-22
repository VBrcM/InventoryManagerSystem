package DB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SaleDAO {
    public static int insert(int saleQty, double totalAmount) throws SQLException {
        String sql = "INSERT INTO sale (sale_date, sale_qty, total_amount) VALUES (?, ?, ?)";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, saleQty);
            stmt.setDouble(3, totalAmount);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to insert Sale.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // return generated sale_id
                } else {
                    throw new SQLException("Failed to retrieve generated Sale ID.");
                }
            }
        }
    }

    // Insert with only sale_date, returns generated sale_id
    public static int insert() throws SQLException {
        String sql = "INSERT INTO Sale (sale_date) VALUES (?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, java.time.LocalDate.now().toString());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Failed to retrieve generated sale_id.");
        }
    }


    public static void updateTotals(int saleId) throws SQLException {
        String sql = """
        UPDATE Sale SET
            sale_qty = (SELECT SUM(si_qty) FROM Sale_item WHERE sale_id = ?),
            total_amount = (SELECT SUM(si_qty * si_price) FROM Sale_item WHERE sale_id = ?)
        WHERE sale_id = ?
    """;
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            stmt.setInt(2, saleId);
            stmt.setInt(3, saleId);
            stmt.executeUpdate();
        }
    }
}
