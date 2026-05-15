package atm.dao;

import atm.db.DatabaseConnection;
import atm.model.Account;

import java.sql.*;

/**
 * AccountDAO.java
 * ---------------
 * Data Access Object for Account operations.
 * Handles balance check, deposit, withdrawal, and transfer.
 * All money operations update the database in real-time.
 */
public class AccountDAO {

    private TransactionDAO transactionDAO = new TransactionDAO();

    /**
     * Get current balance for an account
     * 
     * @param accountNumber The account number
     * @return current balance, or -1 if error
     */
    public double getBalance(String accountNumber) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -1;

        try {
            String sql = "SELECT balance FROM accounts WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Deposit money into account
     * 
     * @param accountNumber The account to deposit into
     * @param amount Amount to deposit (must be > 0)
     * @return true if successful, false otherwise
     */
    public boolean deposit(String accountNumber, double amount) {
        if (amount <= 0) return false;

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false);

            // Update balance in accounts table
            String sql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                // Get new balance
                double newBalance = getBalanceInternal(conn, accountNumber);

                // Record the transaction
                transactionDAO.recordTransaction(accountNumber, "DEPOSIT", amount,
                        newBalance, "Cash Deposit");

                conn.commit();
                conn.setAutoCommit(true);
                return true;
            }

            conn.rollback();
            conn.setAutoCommit(true);
            return false;

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Withdraw money from account
     * Checks if sufficient balance exists before withdrawing
     * 
     * @param accountNumber The account to withdraw from
     * @param amount Amount to withdraw (must be > 0)
     * @return 1 = success, 0 = insufficient balance, -1 = error
     */
    public int withdraw(String accountNumber, double amount) {
        if (amount <= 0) return -1;

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -1;

        try {
            conn.setAutoCommit(false);

            // Check current balance first
            double currentBalance = getBalanceInternal(conn, accountNumber);

            if (currentBalance < amount) {
                conn.setAutoCommit(true);
                return 0;  // Insufficient balance
            }

            // Deduct amount
            String sql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                double newBalance = getBalanceInternal(conn, accountNumber);

                // Record the transaction
                transactionDAO.recordTransaction(accountNumber, "WITHDRAW", amount,
                        newBalance, "Cash Withdrawal");

                conn.commit();
                conn.setAutoCommit(true);
                return 1;  // Success
            }

            conn.rollback();
            conn.setAutoCommit(true);
            return -1;

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Transfer money from one account to another
     * 
     * @param fromAccount Sender's account number
     * @param toAccount Receiver's account number
     * @param amount Amount to transfer
     * @return 1 = success, 0 = insufficient balance, -1 = receiver not found, -2 = error
     */
    public int transfer(String fromAccount, String toAccount, double amount) {
        if (amount <= 0) return -2;

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -2;

        try {
            conn.setAutoCommit(false);

            // Check if receiver account exists
            if (!accountExists(toAccount)) {
                conn.setAutoCommit(true);
                return -1;  // Receiver not found
            }

            // Check sender's balance
            double senderBalance = getBalanceInternal(conn, fromAccount);
            if (senderBalance < amount) {
                conn.setAutoCommit(true);
                return 0;  // Insufficient balance
            }

            // Deduct from sender
            String deductSQL = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            PreparedStatement deductStmt = conn.prepareStatement(deductSQL);
            deductStmt.setDouble(1, amount);
            deductStmt.setString(2, fromAccount);
            deductStmt.executeUpdate();

            // Add to receiver
            String addSQL = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement addStmt = conn.prepareStatement(addSQL);
            addStmt.setDouble(1, amount);
            addStmt.setString(2, toAccount);
            addStmt.executeUpdate();

            // Get updated balances
            double senderNewBalance = getBalanceInternal(conn, fromAccount);
            double receiverNewBalance = getBalanceInternal(conn, toAccount);

            // Record transactions for both accounts
            transactionDAO.recordTransaction(fromAccount, "TRANSFER_OUT", amount,
                    senderNewBalance, "Transfer to A/C: " + toAccount);

            transactionDAO.recordTransaction(toAccount, "TRANSFER_IN", amount,
                    receiverNewBalance, "Transfer from A/C: " + fromAccount);

            conn.commit();
            conn.setAutoCommit(true);
            return 1;  // Success

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return -2;
        }
    }

    /**
     * Check if an account number exists in the database
     */
    public boolean accountExists(String accountNumber) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            String sql = "SELECT COUNT(*) FROM accounts WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Internal method to get balance within an existing connection/transaction
     */
    private double getBalanceInternal(Connection conn, String accountNumber) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, accountNumber);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getDouble("balance");
        }
        return 0;
    }
}
