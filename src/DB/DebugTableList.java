package DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for debugging: lists all tables in the connected database schema.
 * <p>
 * This class is intended for development/debugging purposes only. It connects to the database
 * using the JDBC helper and logs all available table names from the current schema.
 */
public class DebugTableList {

    private static final Logger logger = Logger.getLogger(DebugTableList.class.getName());

    /**
     * Entry point to run the table listing logic.
     * <p>
     * - Connects to the database.
     * - Checks if connection is successful.
     * - Logs each table name retrieved from the metadata.
     * - Logs a severe message if connection fails or SQL errors occur.
     */
    public static void main(String[] args) {
        // Attempt database connection using JDBC helper
        try (Connection conn = JDBC.connect()) {
            if (conn == null) {
                logger.severe("Connection is null. Check DB credentials or server status.");
                return;
            }

            logger.info("Connected to database successfully.");

            // Fetch all table names from current schema
            ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});

            logger.info("Listing all tables in the database:");
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                logger.info("TABLE: " + tableName);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to list tables.", e);
        }
    }
}