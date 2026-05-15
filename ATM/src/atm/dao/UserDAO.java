package atm.dao;

import atm.db.DatabaseConnection;
import atm.model.User;
import atm.model.Account;

import java.sql.*;
import java.util.Random;

/**
 * UserDAO.java
 * ------------
 * Data Access Object for User operations.
 * Handles signup (user creation) and login (validation).
 * Uses PreparedStatement to prevent SQL injection.
 */
public class UserDAO {

    /**
     * Create a new user account (Signup)
     * - Inserts user details into 'users' table
     * - Generates unique account number and IFSC code
     * - Creates entry in 'accounts' table
     * 
     * @param user User object with name, mobile, address, pin
     * @return Account object with generated account number and IFSC code, or null if failed
     */
    public Account createUser(User user) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try {
            // Start transaction (ensures both user and account are created together)
            conn.setAutoCommit(false);

            // Step 1: Insert into users table
            String userSQL = "INSERT INTO users (full_name, mobile, address, pin) VALUES (?, ?, ?, ?)";
            PreparedStatement userStmt = conn.prepareStatement(userSQL, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, user.getFullName());
            userStmt.setString(2, user.getMobile());
            userStmt.setString(3, user.getAddress());
            userStmt.setString(4, user.getPin());
            userStmt.executeUpdate();

            // Get the auto-generated user ID
            ResultSet keys = userStmt.getGeneratedKeys();
            int userId = 0;
            if (keys.next()) {
                userId = keys.getInt(1);
            }

            // Step 2: Generate unique Account Number (10 digits starting with 10)
            String accountNumber = generateAccountNumber();

            // Step 3: Generate IFSC Code (format: ATMB0 + 6 digits)
            String ifscCode = generateIFSCCode();

            // Step 4: Insert into accounts table (initial balance = 0)
            String accountSQL = "INSERT INTO accounts (user_id, account_number, ifsc_code, balance) VALUES (?, ?, ?, ?)";
            PreparedStatement accountStmt = conn.prepareStatement(accountSQL);
            accountStmt.setInt(1, userId);
            accountStmt.setString(2, accountNumber);
            accountStmt.setString(3, ifscCode);
            accountStmt.setDouble(4, 0.0);
            accountStmt.executeUpdate();

            // Commit transaction
            conn.commit();
            conn.setAutoCommit(true);

            // Return the created account details
            Account account = new Account(userId, accountNumber, ifscCode, 0.0);
            user.setUserId(userId);
            return account;

        } catch (SQLException e) {
            try {
                conn.rollback();  // Rollback if anything fails
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Login - Validate user credentials
     * User can login with Account Number + PIN.
     * Returns null if credentials are invalid OR if account is frozen.
     * 
     * @param accountNumber Account number entered by user
     * @param pin PIN entered by user
     * @return Account object if valid & active, null if invalid/frozen
     */
    public Account login(String accountNumber, String pin) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try {
            // Join users and accounts table to validate; also fetch is_frozen
            String sql = "SELECT a.account_id, a.user_id, a.account_number, a.ifsc_code, a.balance, a.is_frozen, "
                       + "u.full_name, u.mobile, u.address "
                       + "FROM accounts a "
                       + "JOIN users u ON a.user_id = u.user_id "
                       + "WHERE a.account_number = ? AND u.pin = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, accountNumber);
            stmt.setString(2, pin);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Check if frozen BEFORE allowing login
                if (rs.getInt("is_frozen") == 1) {
                    // Return special sentinel account with id = -1 to signal "frozen"
                    Account frozen = new Account();
                    frozen.setAccountId(-1);
                    return frozen;
                }
                // Valid credentials - create and return Account object
                Account account = new Account();
                account.setAccountId(rs.getInt("account_id"));
                account.setUserId(rs.getInt("user_id"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setIfscCode(rs.getString("ifsc_code"));
                account.setBalance(rs.getDouble("balance"));
                return account;
            }

            return null;  // Invalid credentials

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get user details by userId
     */
    public User getUserById(int userId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("mobile"),
                    rs.getString("address"),
                    rs.getString("pin")
                );
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get account holder's name by account number
     * Used during transfers to show receiver's name
     * 
     * @param accountNumber The account number to look up
     * @return Full name of the account holder, or null if not found
     */
    public String getNameByAccountNumber(String accountNumber) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try {
            String sql = "SELECT u.full_name FROM users u "
                       + "JOIN accounts a ON u.user_id = a.user_id "
                       + "WHERE a.account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("full_name");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all users with their account details (for Admin Panel)
     * Returns a list of Object arrays: [userId, name, mobile, address, accNo, ifsc, balance, createdAt]
     */
    public java.util.List<Object[]> getAllUsersWithAccounts() {
        java.util.List<Object[]> list = new java.util.ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;

        try {
            String sql = "SELECT u.user_id, u.full_name, u.mobile, u.address, "
                       + "a.account_number, a.ifsc_code, a.balance, u.created_at "
                       + "FROM users u JOIN accounts a ON u.user_id = a.user_id "
                       + "ORDER BY u.user_id DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("mobile"),
                    rs.getString("address"),
                    rs.getString("account_number"),
                    rs.getString("ifsc_code"),
                    rs.getDouble("balance"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Delete a user and their account (for Admin Panel)
     * Cascading delete will also remove transactions
     * 
     * @param userId The user ID to delete
     * @return true if deleted successfully
     */
    public boolean deleteUser(int userId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            String sql = "DELETE FROM users WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update user balance directly (for Admin Panel)
     */
    public boolean updateBalance(String accountNumber, double newBalance) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== Helper Methods ==========

    /**
     * Generate a unique 10-digit account number
     * Format: 10XXXXXXXX (starts with 10)
     */
    private String generateAccountNumber() {
        Random random = new Random();
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return String.valueOf(number);
    }

    /**
     * Generate IFSC Code
     * Format: ATMB0XXXXXX
     */
    private String generateIFSCCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return "ATMB0" + code;
    }
}
