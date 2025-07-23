package DB;

import java.sql.*;

public class DebugTableList {
    public static void main(String[] args) {
        try (Connection conn = JDBC.connect()) {
            if (conn == null) {
                System.err.println("[ERROR] Connection is null. Check DB credentials or server.");
                return;
            }

            System.out.println("[DEBUG] Connected to database.");
            ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});

            System.out.println("[DEBUG] Listing all tables:");
            while (rs.next()) {
                System.out.println("TABLE: " + rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            System.err.printf("[ERROR] Failed to list tables: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }
}