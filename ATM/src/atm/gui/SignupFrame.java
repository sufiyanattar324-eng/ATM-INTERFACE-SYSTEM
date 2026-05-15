package atm.gui;

import atm.dao.UserDAO;
import atm.model.Account;
import atm.model.User;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * SignupFrame.java
 * ----------------
 * Registration screen for new users.
 * Collects name, mobile, address, and PIN.
 * Generates account number and IFSC code upon successful signup.
 */
public class SignupFrame extends JFrame {

    private JTextField nameField, mobileField, addressField;
    private JPasswordField pinField, confirmPinField;
    private UserDAO userDAO;

    public SignupFrame() {
        userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("ATM System - Create Account");
        setSize(520, 640);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ===== Scrollable content =====
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIHelper.BG_DARK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 45, 25, 45));

        // ===== Title =====
        JLabel titleLabel = UIHelper.createTitleLabel("Create Account");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("Fill in your details to get started", SwingConstants.CENTER);
        subtitleLabel.setFont(UIHelper.FONT_SMALL);
        subtitleLabel.setForeground(UIHelper.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(22));

        // ===== Form Card =====
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(UIHelper.BG_CARD);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIHelper.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(22, 28, 22, 28)
        ));
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Helper to add a labeled field
        addFormField(cardPanel, "Full Name", nameField = UIHelper.createStyledTextField(20));
        cardPanel.add(Box.createVerticalStrut(12));
        addFormField(cardPanel, "Mobile Number", mobileField = UIHelper.createStyledTextField(20));
        cardPanel.add(Box.createVerticalStrut(12));
        addFormField(cardPanel, "Address", addressField = UIHelper.createStyledTextField(20));
        cardPanel.add(Box.createVerticalStrut(12));
        addFormField(cardPanel, "Set 4-Digit PIN", pinField = UIHelper.createStyledPasswordField(20));
        cardPanel.add(Box.createVerticalStrut(12));
        addFormField(cardPanel, "Confirm PIN", confirmPinField = UIHelper.createStyledPasswordField(20));

        contentPanel.add(cardPanel);
        contentPanel.add(Box.createVerticalStrut(22));

        // ===== Buttons =====
        JButton signupButton = UIHelper.createStyledButton("  ✦  Create Account  ", UIHelper.ACCENT_GREEN);
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupButton.setMaximumSize(new Dimension(380, 44));
        signupButton.addActionListener(e -> handleSignup());
        contentPanel.add(signupButton);
        contentPanel.add(Box.createVerticalStrut(10));

        JButton backButton = UIHelper.createStyledButton("  ←  Back to Login  ", UIHelper.BG_INPUT);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(380, 44));
        backButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        contentPanel.add(backButton);
        contentPanel.add(Box.createVerticalStrut(10));

        // ===== Scroll Pane =====
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        scrollPane.getViewport().setBackground(UIHelper.BG_DARK);

        setContentPane(scrollPane);
    }

    /**
     * Helper: add a label + input field to the card
     */
    private void addFormField(JPanel card, String labelText, JComponent field) {
        JLabel label = UIHelper.createLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(label);
        card.add(Box.createVerticalStrut(5));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Make fields wider and properly rectangular
        if (field instanceof JTextField) {
            ((JTextField) field).setColumns(25);
        }
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(380, 42));
        card.add(field);
    }

    /**
     * Handle signup button click
     * Validates input and creates user account
     */
    private void handleSignup() {
        String name = nameField.getText().trim();
        String mobile = mobileField.getText().trim();
        String address = addressField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();
        String confirmPin = new String(confirmPinField.getPassword()).trim();

        // ===== Validation =====
        if (name.isEmpty() || mobile.isEmpty() || address.isEmpty() || pin.isEmpty()) {
            UIHelper.showError(this, "All fields are required!");
            return;
        }

        if (mobile.length() != 10 || !mobile.matches("\\d+")) {
            UIHelper.showError(this, "Enter a valid 10-digit mobile number!");
            return;
        }

        if (pin.length() != 4 || !pin.matches("\\d+")) {
            UIHelper.showError(this, "PIN must be exactly 4 digits!");
            return;
        }

        if (!pin.equals(confirmPin)) {
            UIHelper.showError(this, "PINs do not match!");
            return;
        }

        // ===== Create User =====
        User user = new User(name, mobile, address, pin);
        Account account = userDAO.createUser(user);

        if (account != null) {
            // Show account details in a formatted message
            String message = "╔═══════════════════════════════════════╗\n"
                           + "║       ACCOUNT CREATED SUCCESSFULLY       ║\n"
                           + "╠═══════════════════════════════════════╣\n"
                           + "║  Name:            " + name + "\n"
                           + "║  Account Number:  " + account.getAccountNumber() + "\n"
                           + "║  IFSC Code:       " + account.getIfscCode() + "\n"
                           + "╠═══════════════════════════════════════╣\n"
                           + "║  ⚠ Save your Account Number & PIN!     ║\n"
                           + "╚═══════════════════════════════════════╝";

            JTextArea textArea = new JTextArea(message);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
            textArea.setEditable(false);
            textArea.setBackground(UIHelper.BG_CARD);
            textArea.setForeground(UIHelper.ACCENT_GREEN);

            JOptionPane.showMessageDialog(this, textArea,
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);

            // Go back to login
            dispose();
            new LoginFrame().setVisible(true);
        } else {
            UIHelper.showError(this, "Failed to create account. Please try again.");
        }
    }
}
