package Model.DAO;

import DB.JDBC;
import Model.POJO.CartItem;
import Model.POJO.Product;
import Model.POJO.SaleItem;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class SaleItemDAO {
    private static final Logger logger = Logger.getLogger(SaleItemDAO.class.getName());

    public static boolean insert(Connection conn, int saleId, SaleItem saleItem) throws SQLException {
        String sql = """
            INSERT INTO sale_item 
            (sale_id, product_id, si_qty, si_price, si_date)
            VALUES (?, ?, ?, ?, NOW())
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleItem.getSaleId());
            stmt.setInt(2, saleItem.getProductId());
            stmt.setInt(3, saleItem.getSiQty());
            stmt.setDouble(4, saleItem.getSiPrice());
            return stmt.executeUpdate() > 0;
        }
    }

    //Employee
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
                    String productName = rs.getString("product_name");
                    int qty = rs.getInt("si_qty");

                    summary.append(productName)
                            .append(" (")
                            .append(qty)
                            .append("), ");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // or use proper logging
        }

        // Remove last comma and space
        if (summary.length() > 2) {
            summary.setLength(summary.length() - 2);
        }

        return summary.toString();
    }

    public static List<SaleItem> getSaleItemsByDate(LocalDate date) throws SQLException {
        List<SaleItem> items = new ArrayList<>();

        String sql = """
        SELECT si.*, p.product_name, c.category_name
        FROM sale_item si
        JOIN product p ON si.product_id = p.product_id
        JOIN category c ON p.category_id = c.category_id
        WHERE DATE(si.si_date) = ?
    """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();

                    item.setSiQty(rs.getInt("si_qty"));
                    item.setSiPrice(rs.getDouble("si_price"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));      // ✅ this is the key fix
                    item.setCategoryName(rs.getString("category_name"));    // ✅ this too

                    items.add(item);
                }
            }
        }

        return items;
    }

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
            e.printStackTrace(); // Or use a logger
        }

        return dates;
    }

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
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SaleItem item = new SaleItem();
                item.setSaleId(rs.getInt("sale_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setSiQty(rs.getInt("si_qty"));
                item.setSiPrice(rs.getDouble("si_price"));
                item.setSiDate(rs.getTimestamp("si_date").toLocalDateTime());

                // Set product object
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setProductName(rs.getString("product_name"));
                product.setProductPrice(rs.getDouble("product_price")); // optional
                item.setProduct(product); // << IMPORTANT

                items.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching sale items by saleId: " + e.getMessage());
        }

        return items;
    }
}