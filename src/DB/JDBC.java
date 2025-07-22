package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBC {
    public static Connection connect() {
        String url = "jdbc:mysql://127.0.0.1:3306/hardware_inventory_management";
        String user = "school";
        String password = "Jan2024108219";

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("[DEBUG] Connected successfully to database.");
            return connection;
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to connect to database: " + e.getMessage());
            return null;
        }
    }
}
