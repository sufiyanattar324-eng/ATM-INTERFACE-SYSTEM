package atm.dao;

import atm.db.DatabaseConnection;
import atm.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAO.java
 * -------------------
 * Data Access Object for Transaction operations.
 * Records all transactions and retrieves transaction history.
 */
public class TransactionDAO {

    /**
     * Record a new transaction in the database
     * 
     * @param accountNumber Account involved
     * @param type Transaction type (DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT)
     * @param amount Amount of transaction
     * @param balanceAfter Balance after transaction
     * @param description Description of the transaction
     * @return true if recorded successfully
     */
    public boolean recordTransaction(String accountNumber, String type, double amount,
                                     double balanceAfter, String description) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            String sql = "INSERT INTO transactions (account_number, transaction_type, amount, "
                       + "balance_after, description) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, accountNumber);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.setDouble(4, balanceAfter);
            stmt.setString(5, description);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all transactions for an account (most recent first)
     * 
     * @param accountNumber The account number
     * @return List of Transaction objects
     */
    public List<Transaction> getTransactionHistory(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return transactions;

        try {
            String sql = "SELECT * FROM transactions WHERE account_number = ? "
                       + "ORDER BY transaction_date DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction txn = new Transaction(
                    rs.getInt("transaction_id"),
                    rs.getString("account_number"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getDouble("balance_after"),
                    rs.getString("description"),
                    rs.getTimestamp("transaction_date")
                );
                transactions.add(txn);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    /**
     * Get last N transactions for an account
     * 
     * @param accountNumber The account number
     * @param limit Number of transactions to retrieve
     * @return List of Transaction objects
     */
    public List<Transaction> getRecentTransactions(String accountNumber, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return transactions;

        try {
            String sql = "SELECT * FROM transactions WHERE account_number = ? "
                       + "ORDER BY transaction_date DESC LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, accountNumber);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction txn = new Transaction(
                    rs.getInt("transaction_id"),
                    rs.getString("account_number"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getDouble("balance_after"),
                    rs.getString("description"),
                    rs.getTimestamp("transaction_date")
                );
                transactions.add(txn);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }
}
