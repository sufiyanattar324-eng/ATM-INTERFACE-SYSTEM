package atm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection.java
 * -----------------------
 * Utility class to establish JDBC connection with MySQL database.
 * Uses Singleton pattern - only one connection instance is created.
 */
public class DatabaseConnection {

    // Database configuration - change these according to your MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/atm_system";
    private static final String USERNAME = "root";       // Your MySQL username
    private static final String PASSWORD = "";           // Your MySQL password

    private static Connection connection = null;

    /**
     * Get database connection (creates new one if not exists)
     * @return Connection object
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Establish connection
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("✓ Database connected successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver not found!");
            System.err.println("  Make sure mysql-connector-java.jar is in your classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed!");
            System.err.println("  Check your MySQL server, username, and password.");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Close the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
