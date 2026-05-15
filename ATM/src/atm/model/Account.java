package atm.model;

/**
 * Account.java
 * ------------
 * Model class representing a bank account.
 * Each account is linked to a User and has a unique account number.
 * Demonstrates OOP concepts: Encapsulation, Association (User-Account relationship)
 */
public class Account {

    private int accountId;
    private int userId;
    private String accountNumber;
    private String ifscCode;
    private double balance;

    // Default constructor
    public Account() {}

    // Parameterized constructor
    public Account(int userId, String accountNumber, String ifscCode, double balance) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.balance = balance;
    }

    // Full constructor
    public Account(int accountId, int userId, String accountNumber, String ifscCode, double balance) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.balance = balance;
    }

    // ========== Getters and Setters ==========

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account [accNo=" + accountNumber + ", IFSC=" + ifscCode + ", balance=₹" + balance + "]";
    }
}
