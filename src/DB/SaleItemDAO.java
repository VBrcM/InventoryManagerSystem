package DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleItemDAO {

    // Full object insert
    public static void insert(SaleItem si) throws SQLException {
        String sql = "INSERT INTO Sale_item (sale_id, product_id, si_date, si_qty, si_price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, si.getSaleId());
            stmt.setInt(2, si.getProductId());
            stmt.setString(3, si.getSiDate());
            stmt.setInt(4, si.getQuantity());
            stmt.setDouble(5, si.getPrice());
            stmt.executeUpdate();
        }
    }

    // Convenience insert (uses product's price automatically)
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
            stmt.setInt(5, productId);  // correct price fetch from Product using p_id

            stmt.executeUpdate();
        }
    }

    public static boolean insertSaleItem(SaleItem si) {
        try {
            insert(si);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Updates an existing sale item record in the Sale_item table
    public void update(SaleItem si) throws SQLException {
        String sql = "UPDATE Sale_item SET sale_id=?, product_id=?, si_date=?, si_qty=?, si_price=? WHERE si_id=?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, si.getSaleId());
            stmt.setInt(2, si.getProductId());
            stmt.setString(3, si.getSiDate());
            stmt.setInt(4, si.getQuantity());
            stmt.setDouble(5, si.getPrice());
            stmt.setInt(6, si.getSiId());

            int rows = stmt.executeUpdate();
            System.out.println("Update SaleItem ID " + si.getSiId() + ": rows affected = " + rows);
        }
    }

    // Deletes a sale item record from the Sale_item table by its ID
    public void delete(int siId) throws SQLException {
        String sql = "DELETE FROM Sale_item WHERE si_id = ?";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, siId);
            int rows = stmt.executeUpdate();
            System.out.println("Delete SaleItem ID " + siId + ": rows affected = " + rows);
        }
    }

    // Retrieves all sale items for a specific date, including product and category names
    public static List<SaleItem> getSaleItemsByDate(LocalDate date) {
        List<SaleItem> list = new ArrayList<>();

        String sql = "SELECT si.*, p.product AS productName, c.category AS categoryName " +
                "FROM sale_item si " +
                "JOIN product p ON si.product_id = p.product_id " +
                "JOIN category c ON p.category_id = c.category_id " +
                "JOIN sale s ON si.sale_id = s.sale_id " +
                "WHERE DATE(s.sale_date) = ?";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SaleItem item = new SaleItem();
                item.setSiId(rs.getInt("si_id"));
                item.setSaleId(rs.getInt("sale_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setSiDate(rs.getString("si_date"));
                item.setQuantity(rs.getInt("si_qty"));
                item.setPrice(rs.getDouble("si_price"));
                item.setProductName(rs.getString("productName"));
                item.setCategoryName(rs.getString("categoryName"));
                list.add(item);
            }

            System.out.println("Fetched " + list.size() + " SaleItems for date: " + date);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Retrieves all unique sale transaction dates from the sale table
    public static List<LocalDate> getAllTransactionDates() {
        List<LocalDate> dates = new ArrayList<>();

        String sql = "SELECT DISTINCT DATE(sale_date) AS sale_day FROM sale ORDER BY sale_day DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dates.add(rs.getDate("sale_day").toLocalDate());
            }

            System.out.println("Found " + dates.size() + " distinct transaction dates");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dates;
    }

    // Retrieves all distinct sale dates (redundant with getAllTransactionDates but can be separated for clarity)
    public static List<LocalDate> getAllSaleDates() {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT DATE(sale_date) AS sale_date FROM sale ORDER BY sale_date DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dates.add(rs.getDate("sale_date").toLocalDate());
            }

            System.out.println("Retrieved " + dates.size() + " unique sale dates");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dates;
    }
}
