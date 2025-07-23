package Model.DAO;

import DB.JDBC;
import Model.POJO.Product;
import Model.POJO.SaleItem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO class to handle operations for SaleItem.
 */
public class SaleItemDAO {
    private static final Logger LOGGER = Logger.getLogger(SaleItemDAO.class.getName());

    public static void insert(SaleItem si) throws SQLException {
        String sql = "INSERT INTO Sale_item (sale_id, product_id, si_date, si_qty, si_price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, si.getSaleId());
            stmt.setInt(2, si.getProductId());
            stmt.setDate(3, java.sql.Date.valueOf(si.getSiDate()));
            stmt.setInt(4, si.getSiQty());
            stmt.setDouble(5, si.getSiPrice());
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting SaleItem: {0}", e.getMessage());
            throw e;
        }
    }

    /**
     * Inserts a SaleItem using only saleId, productId, and quantity.
     * Price is fetched automatically from product table.
     */
    public static void insert(int saleId, int productId, int qty) throws SQLException {
        String sql = """
            INSERT INTO Sale_item (sale_id, product_id, si_date, si_qty, si_price)
            VALUES (?, ?, ?, ?, (SELECT product_price FROM product WHERE product_id = ?))
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            stmt.setInt(2, productId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setInt(4, qty);
            stmt.setInt(5, productId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, String.format("Error inserting SaleItem with saleId=%d, productId=%d", saleId, productId), e);
            throw e;
        }
    }

    /**
     * Wrapper for insert with boolean result.
     */
    public static boolean insertSaleItem(SaleItem si) {
        try {
            insert(si);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert SaleItem: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing SaleItem.
     */
    public void update(SaleItem si) throws SQLException {
        String sql = "UPDATE sale_item SET sale_id = ?, product_id = ?, si_date = ?, si_qty = ?, si_price = ? WHERE si_id = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, si.getSaleId());
            stmt.setInt(2, si.getProductId());
            stmt.setDate(3, java.sql.Date.valueOf(si.getSiDate()));
            stmt.setInt(4, si.getSiQty());
            stmt.setDouble(5, si.getSiPrice());
            stmt.setInt(6, si.getSiId());
            int rows = stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Updated SaleItem ID {0}: rows affected = {1}", new Object[]{si.getSiId(), rows});

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, String.format("Error updating SaleItem ID %d", si.getSiId()), e);
            throw e;
        }
    }

    /**
     * Deletes a SaleItem by its ID.
     */
    public void delete(int siId) throws SQLException {
        String sql = "DELETE FROM Sale_item WHERE si_id = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, siId);
            int rows = stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Deleted SaleItem ID {0}: rows affected = {1}", new Object[]{siId, rows});

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, String.format("Error deleting SaleItem ID %d", siId), e);
            throw e;
        }
    }

    /**
     * Gets all SaleItems for a given date.
     */
    public static List<SaleItem> getSaleItemsByDate(LocalDate date) {
        List<SaleItem> list = new ArrayList<>();
        String sql = """
            SELECT si.*, p.product_name AS productName, c.category_name AS categoryName
            FROM sale_item si
            JOIN product p ON si.product_id = p.product_id
            JOIN category c ON p.category_id = c.category_id
            JOIN sale s ON si.sale_id = s.sale_id
            WHERE DATE(s.sale_date) = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SaleItem item = new SaleItem();
                item.setSiId(rs.getInt("si_id"));
                item.setSaleId(rs.getInt("sale_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setSiDate(rs.getDate("si_date").toLocalDate());
                item.setSiQty(rs.getInt("si_qty"));
                item.setSiPrice(rs.getDouble("si_price"));
                item.setProductName(rs.getString("productName"));
                item.setCategoryName(rs.getString("categoryName"));
                list.add(item);
            }

            LOGGER.log(Level.INFO, "Fetched {0} SaleItems for date: {1}", new Object[]{list.size(), date});

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, String.format("Error fetching SaleItems for date %s", date), e);
        }

        return list;
    }

    /**
     * Retrieves all distinct sale transaction dates.
     */
    public static List<LocalDate> getAllTransactionDates() {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT DATE(sale_date) AS sale_day FROM sale ORDER BY sale_day DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dates.add(rs.getDate("sale_day").toLocalDate());
            }

            LOGGER.log(Level.INFO, "Found {0} distinct transaction dates", dates.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving transaction dates", e);
        }

        return dates;
    }

    /**
     * Retrieves all distinct sale dates.
     */
    public static List<LocalDate> getAllSaleDates() {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT DATE(sale_date) AS sale_date FROM sale ORDER BY sale_date DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dates.add(rs.getDate("sale_date").toLocalDate());
            }

            LOGGER.log(Level.INFO, "Retrieved {0} unique sale dates", dates.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving sale dates", e);
        }

        return dates;
    }

    public static void insertItem(int saleId, Product product, int qty, double price) {
        String sql = "INSERT INTO sale_item (sale_id, product_id, si_qty, si_price, si_date) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            stmt.setInt(2, product.getProductId());
            stmt.setInt(3, qty);
            stmt.setDouble(4, price);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getItemSummaryBySaleId(int saleId) {
        String sql = """
            SELECT p.product_name, si.si_qty
            FROM sale_item si
            JOIN product p ON si.product_id = p.product_id
            WHERE si.sale_id = ?
        """;

        StringBuilder summary = new StringBuilder();

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String productName = rs.getString("product_name");
                int qty = rs.getInt("si_qty");

                if (summary.length() > 0) {
                    summary.append(", ");
                }
                summary.append(productName).append(" (").append(qty).append(")");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summary.toString();
    }
}
