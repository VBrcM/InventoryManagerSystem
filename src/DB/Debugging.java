package DB;

import java.sql.*;

public class Debugging {
    public class DebugTableList {
        public static void main(String[] args) {
            try (Connection conn = JDBC.connect()) {
                if (conn == null) {
                    System.out.println("Connection is null. Check DB credentials or server.");
                    return;
                }

                ResultSet rs = conn.getMetaData().getTables(null, null, "%", null);
                while (rs.next()) {
                    System.out.println("TABLE: " + rs.getString(3));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
