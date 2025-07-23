package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class JDBC {
    private static final Logger logger = Logger.getLogger(JDBC.class.getName());

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/inventory_management";
    private static final String USER = "school";
    private static final String PASSWORD = "Jan2024108219";

    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("[DEBUG] Connected successfully to the database.");
            return conn;
        } catch (SQLException e) {
            logger.severe(String.format("[ERROR] Failed to connect to database: %s", e.getMessage()));
            return null;
        }
    }
}
