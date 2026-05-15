package atm.dao;

import atm.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminDAO.java
 * -------------
 * Data Access Object for Admin Panel operations.
 * Handles:
 * - Freeze / Unfreeze accounts
 * - Reset user PIN
 * - System statistics (total users, balance, transactions)
 * - Audit logging (every admin action recorded)
 * - CSV data export helpers
 */
public class AdminDAO {

    // ============================================================
    // 1. FREEZE / UNFREEZE ACCOUNT
    // ============================================================

    /**
     * Freeze an account — the user will not be able to log in or transact.
     */
    public boolean freezeAccount(String accountNumber) {
        return setFrozenStatus(accountNumber, 1);
    }

    /**
     * Unfreeze an account — restore normal access.
     */
    public boolean unfreezeAccount(String accountNumber) {
        return setFrozenStatus(accountNumber, 0);
    }

    /**
     * Check if an account is currently frozen.
     */
    public boolean isAccountFrozen(String accountNumber) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT is_frozen FROM accounts WHERE account_number = ?");
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("is_frozen") == 1;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private boolean setFrozenStatus(String accountNumber, int status) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE accounts SET is_frozen = ? WHERE account_number = ?");
            stmt.setInt(1, status);
            stmt.setString(2, accountNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // 2. RESET USER PIN
    // ============================================================

    /**
     * Reset the PIN of a user account.
     * Admin provides the new PIN for the user.
     *
     * @param userId    The user's ID
     * @param newPin    The new 4-digit PIN to set
     * @return true if reset was successful
     */
    public boolean resetPin(int userId, String newPin) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET pin = ? WHERE user_id = ?");
            stmt.setString(1, newPin);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // 3. SYSTEM STATISTICS DASHBOARD
    // ============================================================

    /**
     * Get total number of registered users.
     */
    public int getTotalUsers() {
        return getSingleIntResult("SELECT COUNT(*) FROM users");
    }

    /**
     * Get total number of accounts.
     */
    public int getTotalAccounts() {
        return getSingleIntResult("SELECT COUNT(*) FROM accounts");
    }

    /**
     * Get total number of frozen accounts.
     */
    public int getFrozenAccountCount() {
        return getSingleIntResult("SELECT COUNT(*) FROM accounts WHERE is_frozen = 1");
    }

    /**
     * Get total money held across all accounts (bank liquidity).
     */
    public double getTotalBankBalance() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0.0;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(balance), 0) FROM accounts");
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    /**
     * Get total number of transactions ever made.
     */
    public int getTotalTransactions() {
        return getSingleIntResult("SELECT COUNT(*) FROM transactions");
    }

    /**
     * Get number of transactions made today.
     */
    public int getTodayTransactionCount() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM transactions WHERE DATE(transaction_date) = CURDATE()");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /**
     * Get total amount deposited today.
     */
    public double getTodayDepositTotal() {
        return getDailyAmountByType("DEPOSIT");
    }

    /**
     * Get total amount withdrawn today.
     */
    public double getTodayWithdrawTotal() {
        return getDailyAmountByType("WITHDRAW");
    }

    private double getDailyAmountByType(String type) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0.0;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE transaction_type = ? AND DATE(transaction_date) = CURDATE()");
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private int getSingleIntResult(String sql) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ============================================================
    // 4. AUDIT LOGGING
    // ============================================================

    /**
     * Log an admin action to the admin_logs table.
     *
     * @param actionType e.g. "FREEZE", "UNFREEZE", "RESET_PIN", "DELETE_USER", "EDIT_BALANCE"
     * @param targetUser The name of the affected user
     * @param accountNo  The affected account number (can be null)
     * @param details    A human-readable description of what changed
     */
    public void logAction(String actionType, String targetUser, String accountNo, String details) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO admin_logs (action_type, target_user, account_no, details) VALUES (?, ?, ?, ?)");
            stmt.setString(1, actionType);
            stmt.setString(2, targetUser);
            stmt.setString(3, accountNo);
            stmt.setString(4, details);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Get all audit log entries (newest first).
     * Returns: [log_id, action_type, target_user, account_no, details, performed_at]
     */
    public List<Object[]> getAuditLogs() {
        List<Object[]> logs = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return logs;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT log_id, action_type, target_user, account_no, details, performed_at " +
                "FROM admin_logs ORDER BY performed_at DESC");
            while (rs.next()) {
                logs.add(new Object[]{
                    rs.getInt("log_id"),
                    rs.getString("action_type"),
                    rs.getString("target_user"),
                    rs.getString("account_no"),
                    rs.getString("details"),
                    rs.getTimestamp("performed_at")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return logs;
    }

    /**
     * Get all users with account info, including is_frozen status.
     * Returns: [userId, name, mobile, address, accNo, ifsc, balance, is_frozen, createdAt]
     */
    public List<Object[]> getAllUsersWithStatus() {
        List<Object[]> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT u.user_id, u.full_name, u.mobile, u.address, " +
                "a.account_number, a.ifsc_code, a.balance, a.is_frozen, u.created_at " +
                "FROM users u JOIN accounts a ON u.user_id = a.user_id " +
                "ORDER BY u.user_id DESC");
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("mobile"),
                    rs.getString("address"),
                    rs.getString("account_number"),
                    rs.getString("ifsc_code"),
                    rs.getDouble("balance"),
                    rs.getInt("is_frozen"),     // 0 = active, 1 = frozen
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
