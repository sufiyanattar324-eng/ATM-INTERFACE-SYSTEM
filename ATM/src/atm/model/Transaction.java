package atm.model;

import java.sql.Timestamp;

/**
 * Transaction.java
 * ----------------
 * Model class representing a bank transaction.
 * Records every deposit, withdrawal, and transfer.
 * Demonstrates OOP concepts: Encapsulation
 */
public class Transaction {

    private int transactionId;
    private String accountNumber;
    private String transactionType;   // DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
    private double amount;
    private double balanceAfter;
    private String description;
    private Timestamp transactionDate;

    // Default constructor
    public Transaction() {}

    // Parameterized constructor (for creating new transactions)
    public Transaction(String accountNumber, String transactionType, double amount,
                       double balanceAfter, String description) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    // Full constructor (for reading from database)
    public Transaction(int transactionId, String accountNumber, String transactionType,
                       double amount, double balanceAfter, String description,
                       Timestamp transactionDate) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    // ========== Getters and Setters ==========

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return "Txn#" + transactionId + " | " + transactionType + " | ₹" + amount
                + " | Balance: ₹" + balanceAfter + " | " + transactionDate;
    }
}
