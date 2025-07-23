package Model.DAO;

import DB.JDBC;
import Model.POJO.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
}
