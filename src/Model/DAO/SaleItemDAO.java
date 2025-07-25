package Model.DAO;

import DB.JDBC;
import Model.POJO.CartItem;
import Model.POJO.Product;
import Model.POJO.SaleItem;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles database operations related to sale items.
 */
public class SaleItemDAO {
    private static final Logger logger = Logger.getLogger(SaleItemDAO.class.getName());

    /**
     * Inserts a sale item into the database for admin use.
     */
    public static boolean insert(Connection conn, SaleItem saleItem) throws SQLException {
        String sql = """
            INSERT INTO sale_item
            (product_id, si_qty, si_price, si_date)
            VALUES ( ?, ?, ?, NOW())
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleItem.getProductId());
            stmt.setInt(2, saleItem.getSiQty());
            stmt.setDouble(3, saleItem.getSiPrice());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Inserts a sale item into the database for employee sales.
     */
    public static void insertEmp(Connection conn, int saleId, CartItem item, LocalDateTime saleDate) throws SQLException {
        String sql = "INSERT INTO sale_item (sale_id, product_id, si_qty, si_price, si_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            stmt.setInt(2, item.getProduct().getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getSubtotal());
            stmt.setTimestamp(5, Timestamp.valueOf(saleDate));
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves sale items for a specific sale ID, including product and category details.
     */
    public static List<SaleItem> getBySaleId(int saleId) throws SQLException {
        List<SaleItem> items = new ArrayList<>();
        String sql = """
            SELECT si.*, p.product_name, c.category_name
            FROM sale_item si
            JOIN product p ON si.product_id = p.product_id
            JOIN category c ON p.category_id = c.category_id
            WHERE si.sale_id = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem(
                            rs.getInt("si_id"),
                            rs.getInt("sale_id"),
                            rs.getInt("product_id"),
                            rs.getInt("si_qty"),
                            rs.getDouble("si_price"),
                            rs.getTimestamp("si_date").toLocalDateTime()
                    );
                    item.setProductName(rs.getString("product_name"));
                    item.setCategoryName(rs.getString("category_name"));
                    items.add(item);
                }
            }
        }

        return items;
    }

    /**
     * Generates a human-readable summary of sale items for a specific sale.
     */
    public static String getItemSummaryBySaleId(int saleId) {
        StringBuilder summary = new StringBuilder();

        String sql = """
            SELECT p.product_name, si.si_qty
            FROM sale_item si
            JOIN product p ON si.product_id = p.product_id
            WHERE si.sale_id = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("product_name");
                    int qty = rs.getInt("si_qty");
                    summary.append(name).append(" (").append(qty).append("), ");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get item summary for sale ID " + saleId, e);
        }

        if (summary.length() > 2) {
            summary.setLength(summary.length() - 2);
        }

        return summary.toString();
    }

    /**
     * Retrieves all sale items from a specific date.
     */
    public static List<SaleItem> getSaleItemsByDate(LocalDate date) throws SQLException {
        List<SaleItem> items = new ArrayList<>();

        String query = """
            SELECT si.*, p.product_name, c.category_name, s.sale_id
            FROM sale_item si
            JOIN product p ON si.product_id = p.product_id
            JOIN category c ON p.category_id = c.category_id
            JOIN sale s ON si.sale_id = s.sale_id
            WHERE DATE(s.sale_date) = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setCategoryName(rs.getString("category_name"));
                    item.setSiQty(rs.getInt("si_qty"));
                    item.setSiPrice(rs.getDouble("si_price"));
                    items.add(item);
                }
            }
        }

        return items;
    }

    /**
     * Retrieves all distinct transaction dates from sale items.
     */
    public static List<LocalDate> getAllTransactionDates() {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT DATE(si_date) AS txn_date FROM sale_item ORDER BY txn_date DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dates.add(rs.getDate("txn_date").toLocalDate());
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch transaction dates", e);
        }

        return dates;
    }

    /**
     * Retrieves sale items and product details by sale ID.
     */
    public static List<SaleItem> getSaleItemsBySaleId(int saleId) {
        List<SaleItem> items = new ArrayList<>();

        String sql = """
            SELECT si.*, p.product_id, p.product_name, p.product_price
            FROM sale_item si
            JOIN product p ON si.product_id = p.product_id
            WHERE si.sale_id = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setSiQty(rs.getInt("si_qty"));
                    item.setSiPrice(rs.getDouble("si_price"));
                    item.setSiDate(rs.getTimestamp("si_date").toLocalDateTime());

                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setProductPrice(rs.getDouble("product_price"));
                    item.setProduct(product);

                    items.add(item);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get sale items by sale ID: " + saleId, e);
        }

        return items;
    }
}