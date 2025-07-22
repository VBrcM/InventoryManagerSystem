package DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class TransactionDAO {

    //Transaction for the whole day
    public int getToday() {
        String sql = "SELECT COUNT(*) FROM transaction WHERE DATE(trans_date) = CURDATE()";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to get today's transactions.");
            e.printStackTrace();
            return 0;
        }
    }

    // Get all transactions (basic)
    public static boolean recordTransaction(Transaction txn) throws SQLException {
        String updateStockSQL = "UPDATE product SET stock = stock + ? WHERE product_id = ?";
        String insertTxnSQL = "INSERT INTO transaction (product_id, quantity, type) VALUES (?, ?, ?)";

        String type = txn.getType();
        System.out.println("[DEBUG] Received type in DAO: " + type);
        if (type != null) {
            type = type.trim().toUpperCase(); // Normalize to uppercase
        } else {
            type = "";
        }

        int change = 0;

        switch (type) {
            case "ADD":
                change = txn.getQuantity();
                break;
            case "REDUCE":
                change = -txn.getQuantity();
                break;
            default:
                System.err.println("[WARN] Unknown or missing transaction type: '" + type + "'. Using 'UNKNOWN'.");
                type = "UNKNOWN";
                break;
        }

        try (Connection conn = JDBC.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stockStmt = conn.prepareStatement(updateStockSQL);
                 PreparedStatement insertStmt = conn.prepareStatement(insertTxnSQL)) {

                if (change != 0) {
                    stockStmt.setInt(1, change);
                    stockStmt.setInt(2, txn.getProductId());
                    stockStmt.executeUpdate();
                }

                insertStmt.setInt(1, txn.getProductId());
                insertStmt.setInt(2, txn.getQuantity());
                insertStmt.setString(3, type);
                insertStmt.executeUpdate();

                conn.commit();
                System.out.println("[DEBUG] Transaction saved and stock updated (if applicable).");
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[ERROR] Transaction failed. Rolled back.");
                e.printStackTrace();
                return false;
            }
        }
    }



    // Get today's transaction count
    public static int getTodayTransactionCount() {
        String sql = "SELECT COUNT(*) FROM transaction WHERE DATE(trans_date) = CURDATE()";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to get today's transaction count.");
            e.printStackTrace();
            return 0;
        }
    }

    // Get transaction counts per day
    public static Map<String, Integer> getTransactionsPerDay() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT DATE(trans_date) AS day, COUNT(*) AS total FROM transaction GROUP BY day ORDER BY day DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString("day"), rs.getInt("total"));
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to get transaction counts per day.");
            e.printStackTrace();
        }

        return map;
    }

    // Get all transactions with product and category details
    public static List<Transaction> getAllWithProductCategory() {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.trans_id, t.product_id, t.quantity, t.type, t.trans_date,
                   p.product AS product_name, c.category AS category_name
            FROM transaction t
            JOIN product p ON t.product_id = p.product_id
            JOIN category c ON p.category_id = c.category_id
            ORDER BY t.trans_date DESC
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setTransId(rs.getInt("trans_id"));
                t.setProductId(rs.getInt("product_id"));
                t.setQuantity(rs.getInt("quantity"));
                t.setType(rs.getString("type"));
                t.setTransDate(rs.getTimestamp("trans_date"));
                t.setProductName(rs.getString("product_name"));
                t.setCategoryName(rs.getString("category_name"));
                list.add(t);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch transactions with product/category.");
            e.printStackTrace();
        }

        return list;
    }

    // Get transactions filtered by category
    public static List<Transaction> getTransactions(String categoryFilter) {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.trans_id, t.product_id, p.product, c.category,
                   t.quantity, t.type, t.trans_date
            FROM transaction t
            JOIN product p ON t.product_id = p.product_id
            JOIN category c ON p.category_id = c.category_id
        """ + (categoryFilter != null ? "WHERE c.category = ? " : "") + "ORDER BY t.trans_date DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (categoryFilter != null) {
                stmt.setString(1, categoryFilter);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction();
                    tx.setTransId(rs.getInt("trans_id"));
                    tx.setProductId(rs.getInt("product_id"));
                    tx.setProductName(rs.getString("product"));
                    tx.setCategoryName(rs.getString("category"));
                    tx.setQuantity(rs.getInt("quantity"));
                    tx.setType(rs.getString("type"));
                    tx.setTransDate(rs.getTimestamp("trans_date"));
                    list.add(tx);
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch filtered transactions.");
            e.printStackTrace();
        }

        return list;
    }

    // 🔍 Get transactions by specific date
    public static List<Transaction> getTransactionsByDate(LocalDate date) {
        List<Transaction> list = new ArrayList<>();
        String sql = """
        SELECT t.trans_id, t.product_id, t.quantity, t.type, t.trans_date,
               p.product AS product_name, c.category AS category_name
        FROM transaction t
        JOIN product p ON t.product_id = p.product_id
        JOIN category c ON p.category_id = c.category_id
        WHERE DATE(t.trans_date) = ?
        ORDER BY t.trans_date DESC
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTransId(rs.getInt("trans_id"));
                    t.setProductId(rs.getInt("product_id"));
                    t.setQuantity(rs.getInt("quantity"));
                    t.setType(rs.getString("type")); // this now works
                    t.setTransDate(rs.getTimestamp("trans_date"));
                    t.setProductName(rs.getString("product_name"));
                    t.setCategoryName(rs.getString("category_name"));
                    list.add(t);
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to get transactions by date.");
            e.printStackTrace();
        }

        return list;
    }


    // 📅 Get all unique transaction dates
    public static List<LocalDate> getAllTransactionDates() {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT DATE(trans_date) AS date_only FROM transaction ORDER BY date_only DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dates.add(rs.getDate("date_only").toLocalDate());
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch transaction dates.");
            e.printStackTrace();
        }

        return dates;
    }

    // Optional: Find a product by name
    public static Product findByName(String name) {
        String sql = "SELECT * FROM product WHERE product = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Product(
                        rs.getInt("product_id"),
                        rs.getString("product"),
                        rs.getInt("category_id"),
                        rs.getDouble("p_price"),
                        rs.getInt("stock"),
                        rs.getString("description")
                );
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to find product by name.");
            e.printStackTrace();
        }

        return null;
    }
}
