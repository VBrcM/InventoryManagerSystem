package Model.DAO;

import DB.JDBC;
import Model.POJO.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionDAO {
    private static final Logger logger = Logger.getLogger(TransactionDAO.class.getName());

    public static boolean recordTransaction(Transaction txn) {
        String updateStockSQL = "UPDATE product SET stock = stock + ? WHERE product_id = ?";
        String insertTxnSQL = "INSERT INTO transaction (product_id, t_qty, t_type, t_date) VALUES (?, ?, ?, ?)";

        int change = 0;
        if ("add".equalsIgnoreCase(txn.getType())) {
            change = txn.getQuantity();
        } else if ("reduce".equalsIgnoreCase(txn.getType())) {
            change = -txn.getQuantity();
        }

        try (Connection conn = JDBC.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stockStmt = conn.prepareStatement(updateStockSQL);
                 PreparedStatement txnStmt = conn.prepareStatement(insertTxnSQL)) {

                // Update stock
                stockStmt.setInt(1, change);
                stockStmt.setInt(2, txn.getProductId());
                stockStmt.executeUpdate();

                // Insert transaction
                txnStmt.setInt(1, txn.getProductId());
                txnStmt.setInt(2, txn.getQuantity());
                txnStmt.setString(3, txn.getType());
                txnStmt.setDate(4, Date.valueOf(txn.getDate()));
                txnStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Failed to record transaction: {0}", e.getMessage());
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection error: {0}", e.getMessage());
        }
        return false;
    }

    public static List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.*, p.product_name
            FROM transaction t
            JOIN product p ON t.product_id = p.product_id
            ORDER BY t.t_date DESC, t.t_id DESC
        """;

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction txn = new Transaction();
                txn.setId(rs.getInt("t_id"));
                txn.setProductId(rs.getInt("product_id"));
                txn.setProductName(rs.getString("product_name"));
                txn.setQuantity(rs.getInt("t_qty"));
                txn.setType(rs.getString("t_type"));
                txn.setDate(rs.getDate("t_date").toLocalDate());
                list.add(txn);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving transactions: {0}", e.getMessage());
        }

        return list;
    }

    public static int getTodayCount() {
        String sql = "SELECT COUNT(*) FROM transaction WHERE t_date = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting today's transactions: {0}", e.getMessage());
        }
        return 0;
    }

    public static List<Transaction> getSummaryByDate() {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t_date, t_type, SUM(t_qty) as total_qty
            FROM transaction
            GROUP BY t_date, t_type
            ORDER BY t_date DESC
        """;

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction txn = new Transaction();
                txn.setDate(rs.getDate("t_date").toLocalDate());
                txn.setType(rs.getString("t_type"));
                txn.setQuantity(rs.getInt("total_qty"));
                list.add(txn);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error summarizing transactions by date: {0}", e.getMessage());
        }

        return list;
    }

    // Get all unique transaction dates (sorted descending)
    public static List<LocalDate> getAllTransactionDates() {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT t_date FROM transaction ORDER BY t_date DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dates.add(rs.getDate("t_date").toLocalDate());
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving transaction dates: " + e.getMessage());
            e.printStackTrace();
        }

        return dates;
    }

    // Get all transactions for a specific date
    public static List<Transaction> getTransactionsByDate(LocalDate date) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE t_date = ? ORDER BY t_id DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction txn = new Transaction();
                    txn.setId(rs.getInt("t_id"));
                    txn.setProductId(rs.getInt("product_id"));
                    txn.setQuantity(rs.getInt("t_qty"));
                    txn.setType(rs.getString("t_type"));
                    txn.setDate(rs.getDate("t_date").toLocalDate());
                    list.add(txn);
                }
            }

        } catch (SQLException e) {
            System.err.printf("Error fetching transactions on %s: %s%n", date, e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public static int getTodaySalesTotal() {
        String sql = """
        SELECT SUM(t_qty) FROM transaction
        WHERE t_type = 'add' AND t_date = ?
    """;
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting today's sales total: {0}", e.getMessage());
        }
        return 0;
    }
    public static int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM product WHERE stock <= 5";
        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting low stock count: {0}", e.getMessage());
        }
        return 0;
    }
    public static Map<String, Integer> getTodayTransactionSummaryByProduct() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        String sql = """
        SELECT p.product_name, SUM(t.t_qty) AS total_qty
        FROM transaction t
        JOIN product p ON t.product_id = p.product_id
        WHERE t.t_date = ?
        GROUP BY p.product_name
        ORDER BY total_qty DESC
        LIMIT 5
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    summary.put(rs.getString("product_name"), rs.getInt("total_qty"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting today's product summary: {0}", e.getMessage());
        }

        return summary;
    }
    public static Map<String, Integer> getWeeklyTransactionSummaryByProduct() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        String sql = """
        SELECT p.product_name, SUM(t.t_qty) AS total_qty
        FROM transaction t
        JOIN product p ON t.product_id = p.product_id
        WHERE t.t_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        GROUP BY p.product_name
        ORDER BY total_qty DESC
        LIMIT 5
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                summary.put(rs.getString("product_name"), rs.getInt("total_qty"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting weekly product summary: {0}", e.getMessage());
        }

        return summary;
    }

    public static List<Transaction> getRecentTransactions() {
        List<Transaction> list = new ArrayList<>();

        String sql = """
        SELECT 
            DATE_FORMAT(t.t_date, '%h:%i %p') AS txn_time,
            SUM(p.product_price * t.t_qty) AS total_price,
            GROUP_CONCAT(CONCAT(p.product_name, '(', t.t_qty, ')') SEPARATOR ' ') AS products
        FROM transaction t
        JOIN product p ON t.product_id = p.product_id
        WHERE DATE(t.t_date) = CURDATE()
        GROUP BY txn_time
        ORDER BY MAX(t.t_date) DESC
    """;

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction txn = new Transaction();
                txn.setFormattedTime(rs.getString("txn_time")); // reuse formattedTime
                txn.setAmount(rs.getDouble("total_price"));
                txn.setDescription(rs.getString("products"));   // reuse description
                list.add(txn);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving grouped recent transactions: {0}", e.getMessage());
        }

        return list;
    }
}
