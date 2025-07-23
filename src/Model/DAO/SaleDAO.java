package Model.DAO;

import DB.JDBC;
import Model.POJO.Sale;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO class for managing sale records in the database.
 */
public class SaleDAO {
    private static final Logger LOGGER = Logger.getLogger(SaleDAO.class.getName());

    /**
     * Inserts a sale with date, quantity, and total amount.
     *
     * @param saleQty     total quantity of items in the sale
     * @param totalAmount total amount of the sale
     * @return generated sale ID
     * @throws SQLException if insertion fails
     */
    public static int insert(int saleQty, double totalAmount) throws SQLException {
        String sql = "INSERT INTO sale (sale_date, sale_qty, total_amount) VALUES (?, ?, ?)";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, saleQty);
            stmt.setDouble(3, totalAmount);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to insert Sale.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // return generated sale_id
                } else {
                    throw new SQLException("Failed to retrieve generated Sale ID.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting sale with quantity {0} and total {1}: {2}",
                    new Object[]{saleQty, totalAmount, e.getMessage()});
            throw e;
        }
    }

    /**
     * Inserts a sale with only the current date.
     *
     * @return generated sale ID
     * @throws SQLException if insertion or key retrieval fails
     */
    public static int insert() throws SQLException {
        String sql = "INSERT INTO sale (sale_date) VALUES (?)";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Failed to insert Sale with date.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated sale_id.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting sale with only date: {0}", e.getMessage());
            throw e;
        }
    }

    /**
     * Updates sale totals by calculating the sum of sale_items associated with the sale.
     *
     * @param saleId the ID of the sale to update
     * @throws SQLException if update fails
     */
    public static void updateTotals(int saleId) throws SQLException {
        String sql = """
            UPDATE sale SET
                sale_qty = (SELECT SUM(si_qty) FROM sale_item WHERE sale_id = ?),
                total_amount = (SELECT SUM(si_qty * si_price) FROM sale_item WHERE sale_id = ?)
            WHERE sale_id = ?
        """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            stmt.setInt(2, saleId);
            stmt.setInt(3, saleId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, String.format("Error updating totals for sale ID %d: %s", saleId, e.getMessage()));
            throw e;
        }
    }

    public static int insertSale(double totalAmount) {
        String sql = "INSERT INTO sale (sale_date, total_amount) VALUES (NOW(), ?)";
        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, totalAmount);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static List<Sale> getSalesByDate(LocalDate date) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sale WHERE sale_date = ? ORDER BY sale_id DESC";

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Sale sale = new Sale();
                sale.setId(rs.getInt("sale_id"));
                sale.setSaleDate(rs.getDate("sale_date").toLocalDate());
                sale.setSaleQty(rs.getInt("sale_qty"));
                sale.setTotalAmount(rs.getDouble("total_amount"));
                sales.add(sale);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching sales for date: " + date, e);
        }

        return sales;
    }
}
