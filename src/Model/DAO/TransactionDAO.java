package Model.DAO;

import DB.JDBC;
import Model.POJO.CartItem;
import Model.POJO.Product;
import Model.POJO.Transaction;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class TransactionDAO {

    public static boolean record(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transaction (product_id, t_qty, t_date) VALUES (?, ?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transaction.getProductId());
            stmt.setInt(2, transaction.getTQty());
            return stmt.executeUpdate() > 0;
        }
    }

    public static List<Transaction> getByDate(LocalDateTime date) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.*, p.product_name
            FROM transaction t
            JOIN product p ON t.product_id = p.product_id
            WHERE DATE(t.t_date) = ?
            ORDER BY t.t_date DESC
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date.toLocalDate()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getInt("t_id"),
                            rs.getInt("product_id"),
                            rs.getInt("t_qty"),
                            rs.getTimestamp("t_date").toLocalDateTime()
                    ));
                }
            }
        }
        return transactions;
    }

    public static Map<String, Integer> getDailySummary(LocalDateTime date) throws SQLException {
        Map<String, Integer> summary = new HashMap<>();
        String sql = """
            SELECT p.product_name, SUM(t.t_qty) AS total_qty
            FROM transaction t
            JOIN product p ON t.product_id = p.product_id
            WHERE DATE(t.t_date) = ?
            GROUP BY p.product_name
            ORDER BY total_qty DESC
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date.toLocalDate()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    summary.put(
                            rs.getString("product_name"),
                            rs.getInt("total_qty")
                    );
                }
            }
        }
        return summary;
    }

    public static int getTodayCount() {
        String sql = "SELECT COUNT(*) FROM transaction WHERE DATE(t_date) = CURDATE()";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static Map<String, Integer> getTodayTransactionSummaryByProduct() {
        Map<String, Integer> summary = new HashMap<>();

        String sql = """
        SELECT p.product_name, SUM(t.t_qty) as total_qty
        FROM transaction t
        JOIN product p ON t.product_id = p.product_id
        WHERE DATE(t.t_date) = CURDATE()
        GROUP BY p.product_name
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String productName = rs.getString("product_name");
                int totalQty = rs.getInt("total_qty");
                summary.put(productName, totalQty);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summary;
    }

    public static Map<String, Integer> getWeeklyTransactionSummaryByProduct() {
        Map<String, Integer> summary = new HashMap<>();

        String sql = """
        SELECT p.product_name, SUM(t.t_qty) AS total_qty
        FROM transaction t
        JOIN product p ON t.product_id = p.product_id
        WHERE YEARWEEK(DATE(t.t_date), 1) = YEARWEEK(CURDATE(), 1)
        GROUP BY p.product_name
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String productName = rs.getString("product_name");
                int totalQty = rs.getInt("total_qty");
                summary.put(productName, totalQty);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summary;
    }

    public static List<Transaction> getRecentTransactions() {
        Map<Integer, Transaction> transactionMap = new LinkedHashMap<>();

        String sql = """
        SELECT t.t_id, t.product_id, t.t_qty, t.t_date, 
               p.product_name, p.product_price
        FROM transaction t
        JOIN product p ON t.product_id = p.product_id
        ORDER BY t.t_date DESC
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int tId = rs.getInt("t_id");

                Transaction txn = transactionMap.getOrDefault(tId, new Transaction());

                if (txn.getTId() == 0) {
                    txn.setTId(tId);
                    txn.setTDate(rs.getTimestamp("t_date").toLocalDateTime());
                    txn.setAmount(0);
                    txn.setDescription(""); // We'll build below
                }

                String productName = rs.getString("product_name");
                int qty = rs.getInt("t_qty");
                double price = rs.getDouble("product_price");

                // Append to description
                String entry = productName + " (" + qty + ")";
                String currentDesc = txn.getDescription();

                if (!currentDesc.isEmpty()) {
                    currentDesc += ", ";
                }

                if ((currentDesc + entry).length() > 100) {
                    if (!currentDesc.endsWith("...")) {
                        currentDesc += "...";
                    }
                } else {
                    currentDesc += entry;
                }

                txn.setDescription(currentDesc);

                // Add to total amount
                txn.setAmount(txn.getAmount() + (price * qty));

                transactionMap.put(tId, txn);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(transactionMap.values());
    }

    public static List<Integer> getRecentTransactionIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT t_id FROM transaction ORDER BY t_date DESC LIMIT 12";

        try (Connection conn = JDBC.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getInt("t_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }


    public static List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = """
                SELECT t.t_id, t.product_id, t.t_qty, t.t_date, 
                       p.product_name, p.product_price, p.description
                FROM transaction t
                JOIN product p ON t.product_id = p.product_id
                ORDER BY t.t_date DESC
                LIMIT ?
            """;


        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction txn = new Transaction();
                    txn.setTId(rs.getInt("t_id"));
                    txn.setProductId(rs.getInt("product_id"));
                    txn.setTQty(rs.getInt("t_qty"));
                    txn.setTDate(rs.getTimestamp("t_date").toLocalDateTime());
                    txn.setAmount(rs.getDouble("product_price"));
                    txn.setProductName(rs.getString("product_name"));
                    txn.setDescription(rs.getString("description"));// if available
                    list.add(txn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }



    public static List<LocalDate> getAllTransactionDates() {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT DATE(t_date) AS txn_date FROM transaction ORDER BY txn_date DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dates.add(rs.getDate("txn_date").toLocalDate());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dates;
    }

    public static boolean insertCartTransaction(Connection conn, List<CartItem> cartItems) throws SQLException {
        int generatedTId;

        // Step 1: Insert first item to auto-generate t_id
        String firstInsert = "INSERT INTO transaction (product_id, t_qty, t_date) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(firstInsert, Statement.RETURN_GENERATED_KEYS)) {
            CartItem firstItem = cartItems.get(0);
            stmt.setInt(1, firstItem.getProduct().getProductId());
            stmt.setInt(2, firstItem.getQuantity());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedTId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated transaction ID");
                }
            }
        }

        // Step 2: Insert the remaining cart items using the same t_id
        String insertRest = "INSERT INTO transaction (t_id, product_id, t_qty, t_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertRest)) {
            for (int i = 1; i < cartItems.size(); i++) {
                CartItem item = cartItems.get(i);
                stmt.setInt(1, generatedTId);
                stmt.setInt(2, item.getProduct().getProductId());
                stmt.setInt(3, item.getQuantity());
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }

        return true;
    }


    public static List<Transaction> getTransactionsByDate(LocalDate date) {
        List<Transaction> list = new ArrayList<>();
        String sql = """
        SELECT t.t_id, t.product_id, t.t_qty, t.t_date,
               p.product_price
        FROM transaction t
        JOIN product p ON t.product_id = p.product_id
        WHERE DATE(t.t_date) = ?
        ORDER BY t.t_date DESC
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction txn = new Transaction();
                    txn.setTId(rs.getInt("t_id"));
                    txn.setProductId(rs.getInt("product_id"));
                    txn.setTQty(rs.getInt("t_qty"));

                    double price = rs.getDouble("product_price");
                    txn.setPrice(price); // Add this if your Transaction class has it
                    txn.setAmount(txn.getTQty() * price);

                    txn.setTDate(rs.getTimestamp("t_date").toLocalDateTime());
                    list.add(txn);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<Transaction> getTransactionsByTID(int tId) {
        List<Transaction> list = new ArrayList<>();
        String sql = """
        SELECT t.t_id, t.product_id, t.t_qty, t.t_date,
               p.product_name, p.product_price
        FROM transaction t
        JOIN product p ON t.product_id = p.product_id
        WHERE t.t_id = ?
        ORDER BY t.t_date DESC
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setProductPrice(rs.getDouble("product_price"));

                    Transaction txn = new Transaction();
                    txn.setTId(rs.getInt("t_id"));
                    txn.setProductId(rs.getInt("product_id"));
                    txn.setTQty(rs.getInt("t_qty"));
                    txn.setTDate(rs.getTimestamp("t_date").toLocalDateTime());
                    txn.setProduct(product);

                    list.add(txn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}