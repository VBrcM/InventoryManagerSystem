package DB;

import java.sql.*;

public class DebugTableList {
    public static void main(String[] args) {
        try (Connection conn = JDBC.connect()) {
            if (conn == null) {
                System.out.println("[ERROR] Connection is null. Check DB credentials or server.");
                return;
            }

            System.out.println("[DEBUG] Connected to database.");
            ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" });

            System.out.println("[DEBUG] Listing all tables:");
            while (rs.next()) {
                System.out.println("TABLE: " + rs.getString("TABLE_NAME"));
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to list tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

