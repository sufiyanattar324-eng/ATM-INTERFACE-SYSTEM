package atm.model;

/**
 * User.java
 * ---------
 * Model class representing a bank user.
 * Stores personal details like name, mobile, address, and PIN.
 * Demonstrates OOP concepts: Encapsulation (private fields + getters/setters)
 */
public class User {

    private int userId;
    private String fullName;
    private String mobile;
    private String address;
    private String pin;

    // Default constructor
    public User() {}

    // Parameterized constructor
    public User(String fullName, String mobile, String address, String pin) {
        this.fullName = fullName;
        this.mobile = mobile;
        this.address = address;
        this.pin = pin;
    }

    // Full constructor (with userId)
    public User(int userId, String fullName, String mobile, String address, String pin) {
        this.userId = userId;
        this.fullName = fullName;
        this.mobile = mobile;
        this.address = address;
        this.pin = pin;
    }

    // ========== Getters and Setters ==========

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", name=" + fullName + ", mobile=" + mobile + "]";
    }
}
