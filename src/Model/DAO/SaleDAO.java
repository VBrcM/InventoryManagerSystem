package Model.DAO;

import DB.JDBC;
import Model.POJO.CartItem;
import Model.POJO.Sale;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class SaleDAO {
    private static final Logger logger = Logger.getLogger(SaleDAO.class.getName());

    public static int insert(Connection conn, int saleQty, double totalAmount) throws SQLException {
        String sql = "INSERT INTO sale (sale_date, sale_qty, total_amount) VALUES (NOW(), ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, saleQty);
            stmt.setDouble(2, totalAmount);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new SQLException("Failed to get generated sale ID");
        }
    }

    public static List<Sale> getByDate(LocalDateTime date) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sale WHERE DATE(sale_date) = ? ORDER BY sale_id DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date.toLocalDate()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(new Sale(
                            rs.getInt("sale_id"),
                            rs.getTimestamp("sale_date").toLocalDateTime(),
                            rs.getInt("sale_qty"),
                            rs.getDouble("total_amount")
                    ));
                }
            }
        }
        return sales;
    }

    public static double getDailyTotal(LocalDateTime date) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM sale WHERE DATE(sale_date) = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date.toLocalDate()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public static double getTodaySalesTotal() {
        String sql = """
        SELECT SUM(total_amount) FROM sale
        WHERE DATE(sale_date) = CURDATE()
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public static List<Sale> getSalesByDate(LocalDate date) {
        List<Sale> sales = new ArrayList<>();

        String sql = "SELECT * FROM sale WHERE DATE(sale_date) = ? ORDER BY sale_date DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setSaleId(rs.getInt("sale_id"));
                    sale.setTotalAmount(rs.getDouble("total_amount"));
                    sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());

                    // set other fields as needed

                    sales.add(sale);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging if needed
        }

        return sales;
    }
    public static int insertSale(Connection conn, Sale sale, List<CartItem> items) throws SQLException {
        String saleSql = "INSERT INTO sale (sale_qty, sale_date, total_amount) VALUES (?, ?, ?)";
        String itemSql = "INSERT INTO sale_item (sale_id, product_id, si_qty, si_price, si_date) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement saleStmt = null;
        ResultSet rs = null;

        try {
            saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            saleStmt.setInt(1, sale.getSaleQty());
            saleStmt.setTimestamp(2, Timestamp.valueOf(sale.getSaleDate()));
            saleStmt.setDouble(3, sale.getTotalAmount());

            int affected = saleStmt.executeUpdate();
            if (affected == 0) throw new SQLException("No rows inserted for sale.");

            rs = saleStmt.getGeneratedKeys();
            if (rs.next()) {
                int saleId = rs.getInt(1);

                try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                    for (CartItem item : items) {
                        itemStmt.setInt(1, saleId);
                        itemStmt.setInt(2, item.getProduct().getProductId());
                        itemStmt.setInt(3, item.getQuantity());
                        itemStmt.setDouble(4, item.getProduct().getProductPrice());
                        itemStmt.setTimestamp(5, Timestamp.valueOf(sale.getSaleDate()));
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }

                return saleId;
            } else {
                throw new SQLException("Failed to retrieve generated sale ID.");
            }

        } finally {
            if (rs != null) rs.close();
            if (saleStmt != null) saleStmt.close();
        }
    }
}