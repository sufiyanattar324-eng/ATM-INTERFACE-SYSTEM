package atm;

import atm.gui.LoginFrame;
import atm.gui.UIHelper;
import atm.db.DatabaseConnection;

import javax.swing.*;

/**
 * Main.java
 * ---------
 * Entry point for the ATM Interface System.
 * Initializes the dark theme and launches the Login screen.
 * 
 * =============================================
 *   ATM INTERFACE SYSTEM
 *   Version: 1.0
 *   Technology: Java Swing + MySQL + JDBC
 * =============================================
 * 
 * How to Run:
 * 1. Make sure MySQL is running
 * 2. Run schema.sql to create the database
 * 3. Update DatabaseConnection.java with your MySQL credentials
 * 4. Compile and run this Main class
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║      ATM INTERFACE SYSTEM v1.0      ║");
        System.out.println("║   Java Swing + MySQL + JDBC         ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        // Test database connection first
        System.out.println("Connecting to database...");
        if (DatabaseConnection.getConnection() == null) {
            System.err.println("╔══════════════════════════════════════╗");
            System.err.println("║  DATABASE CONNECTION FAILED!         ║");
            System.err.println("║                                      ║");
            System.err.println("║  Please check:                       ║");
            System.err.println("║  1. MySQL server is running          ║");
            System.err.println("║  2. Database 'atm_system' exists     ║");
            System.err.println("║  3. Username/Password in             ║");
            System.err.println("║     DatabaseConnection.java          ║");
            System.err.println("╚══════════════════════════════════════╝");

            JOptionPane.showMessageDialog(null,
                "Cannot connect to MySQL database!\n\n"
                + "Please ensure:\n"
                + "1. MySQL server is running\n"
                + "2. Database 'atm_system' exists (run schema.sql)\n"
                + "3. Check username/password in DatabaseConnection.java",
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("✓ Database connection successful!");
        System.out.println("✓ Launching ATM Interface...");
        System.out.println();

        // Set dark look and feel
        UIHelper.setDarkLookAndFeel();

        // Launch GUI on Event Dispatch Thread (Swing best practice)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });

        // Shutdown hook - close DB connection on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.closeConnection();
            System.out.println("✓ Application closed. Goodbye!");
        }));
    }
}
