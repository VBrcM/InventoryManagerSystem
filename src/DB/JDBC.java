package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.IOException;

/**
 * Provides a utility method to establish a database connection using properties
 * loaded from a configuration file.
 * <p>
 * The configuration file "config.properties"> must be located in the
 * same package as this class (DB). It should contain the following keys:
 *   "db.url – JDBC URL for the database"
 *   "db.user – database username"
 *   "db.password – database password"
 */
public class JDBC {

    private static final Logger logger = Logger.getLogger(JDBC.class.getName());
    private static final Properties config = new Properties();
    private static boolean configLoaded = false;

    // Static initializer block: loads database configuration once
    static {
        try (InputStream input = JDBC.class.getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
                configLoaded = true;
                logger.info("Database configuration loaded from DB/config.properties");
            } else {
                logger.severe("config.properties not found in DB package.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading database configuration.", e);
        }
    }

    /**
     * Establishes a database connection using the credentials and URL defined in
     * the loaded configuration.
     */
    public static Connection connect() {
        if (!configLoaded) {
            logger.warning("Database configuration not loaded. Aborting connection attempt.");
            return null;
        }

        String url = config.getProperty("db.url");
        String user = config.getProperty("db.user");
        String password = config.getProperty("db.password");

        if (url == null || user == null || password == null) {
            logger.severe("Missing required database connection properties.");
            return null;
        }

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            logger.info("Database connection established.");
            return conn;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection failed.", e);
            return null;
        }
    }
}
