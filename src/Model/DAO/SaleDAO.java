package Model.DAO;

import DB.JDBC;
import Model.POJO.CartItem;
import Model.POJO.Sale;
import Model.POJO.SaleItem;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles all database operations related to sales and associated sale items.
 */
public class SaleDAO {
    private static final Logger logger = Logger.getLogger(SaleDAO.class.getName());

    /**
     * Inserts a new sale record with quantity and total amount.
     * Returns the generated sale ID.
     */
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

            throw new SQLException("Failed to retrieve generated sale ID.");
        }
    }

    /**
     * Returns all sales on the given date.
     */
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

    /**
     * Returns the total sale amount for a specific date.
     */
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

    /**
     * Returns today's total sale amount.
     */
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
            logger.log(Level.SEVERE, "Error fetching today's sales total", e);
        }

        return 0.0;
    }

    /**
     * Returns sales along with their sale items for the specified date.
     */
    public static List<Sale> getSalesByDate(LocalDate date) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sale WHERE DATE(sale_date) = ? ORDER BY sale_date DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Sale sale = new Sale();
                sale.setSaleId(rs.getInt("sale_id"));
                sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                sale.setSaleQty(rs.getInt("sale_qty"));
                sale.setTotalAmount(rs.getDouble("total_amount"));

                List<SaleItem> items = SaleItemDAO.getSaleItemsBySaleId(sale.getSaleId());
                sale.setSaleItems(items);

                sales.add(sale);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving sales by date", e);
        }

        return sales;
    }

    /**
     * Returns all distinct sale dates in descending order.
     */
    public static List<LocalDate> getAllSaleDates() {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT DATE(sale_date) AS sale_date FROM sale ORDER BY sale_date DESC";

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dates.add(rs.getDate("sale_date").toLocalDate());
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all sale dates", e);
        }

        return dates;
    }

    /**
     * Inserts a sale and its associated sale items in one transaction.
     * Returns the generated sale ID.
     */
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

    /**
     * Returns the latest 30 sale IDs ordered by sale date.
     */
    public static List<Integer> getRecentSaleIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT sale_id FROM sale ORDER BY sale_date DESC LIMIT 30";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getInt("sale_id"));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching recent sale IDs", e);
        }

        return ids;
    }

    /**
     * Returns a Sale object by its ID, or null if not found.
     */
    public static Sale getSaleById(int saleId) {
        String sql = "SELECT * FROM sale WHERE sale_id = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Sale sale = new Sale();
                sale.setSaleId(saleId);
                sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                sale.setTotalAmount(rs.getDouble("total_amount"));
                sale.setSaleQty(rs.getInt("sale_qty"));
                return sale;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving sale by ID: " + saleId, e);
        }

        return null;
    }
}