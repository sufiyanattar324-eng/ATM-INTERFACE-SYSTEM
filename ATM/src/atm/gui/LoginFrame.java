package atm.gui;

import atm.dao.UserDAO;
import atm.model.Account;

import javax.swing.*;
import java.awt.*;

/**
 * LoginFrame.java
 * ---------------
 * Login screen for the ATM system.
 * Users can log in with Account Number + PIN.
 * Also has option to create a new account (Signup).
 */
public class LoginFrame extends JFrame {

    private JTextField accountField;
    private JPasswordField pinField;
    private UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("ATM Interface System");
        setSize(480, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ===== Scrollable wrapper =====
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIHelper.BG_DARK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 45, 25, 45));

        // ===== Logo Section =====
        JPanel logoPanel = UIHelper.createLogoPanel();
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(logoPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        JLabel titleLabel = UIHelper.createTitleLabel("ATM System");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(4));

        JLabel subtitleLabel = new JLabel("Secure Banking at Your Fingertips", SwingConstants.CENTER);
        subtitleLabel.setFont(UIHelper.FONT_SMALL);
        subtitleLabel.setForeground(UIHelper.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(22));

        // ===== Login Card =====
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(UIHelper.BG_CARD);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(UIHelper.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(22, 28, 22, 28)
        ));
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login heading
        JLabel loginLabel = new JLabel("Login to Your Account");
        loginLabel.setFont(UIHelper.FONT_SUBTITLE);
        loginLabel.setForeground(UIHelper.TEXT_PRIMARY);
        loginLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(loginLabel);
        cardPanel.add(Box.createVerticalStrut(16));

        // Account Number
        JLabel accLabel = UIHelper.createLabel("Account Number");
        accLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(accLabel);
        cardPanel.add(Box.createVerticalStrut(5));

        accountField = UIHelper.createStyledTextField(20);
        accountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        accountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(accountField);
        cardPanel.add(Box.createVerticalStrut(14));

        // PIN
        JLabel pinLabel = UIHelper.createLabel("4-Digit PIN");
        pinLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(pinLabel);
        cardPanel.add(Box.createVerticalStrut(5));

        pinField = UIHelper.createStyledPasswordField(20);
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pinField.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(pinField);

        contentPanel.add(cardPanel);
        contentPanel.add(Box.createVerticalStrut(22));

        // ===== Buttons =====
        JButton loginButton = UIHelper.createStyledButton("  →  Login  ", UIHelper.ACCENT_BLUE);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(380, 44));
        loginButton.addActionListener(e -> handleLogin());
        contentPanel.add(loginButton);
        contentPanel.add(Box.createVerticalStrut(10));

        JButton signupButton = UIHelper.createStyledButton("  +  Create New Account  ", UIHelper.BG_INPUT);
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupButton.setMaximumSize(new Dimension(380, 44));
        signupButton.addActionListener(e -> {
            dispose();
            new SignupFrame().setVisible(true);
        });
        contentPanel.add(signupButton);
        contentPanel.add(Box.createVerticalStrut(10));

        JButton adminButton = UIHelper.createStyledButton("  🛡️  Admin Panel  ", new Color(139, 92, 246));
        adminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminButton.setMaximumSize(new Dimension(380, 44));
        adminButton.addActionListener(e -> {
            if (AdminFrame.showAdminLoginDialog(this)) {
                dispose();
                new AdminFrame().setVisible(true);
            }
        });
        contentPanel.add(adminButton);
        contentPanel.add(Box.createVerticalStrut(16));

        // Footer
        JLabel footerLabel = new JLabel("© 2026 ATM Interface System", SwingConstants.CENTER);
        footerLabel.setFont(UIHelper.FONT_SMALL);
        footerLabel.setForeground(new Color(100, 116, 139));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(footerLabel);

        // ===== Scroll Pane =====
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        scrollPane.getViewport().setBackground(UIHelper.BG_DARK);

        setContentPane(scrollPane);
        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Handle login button click
     * Validates credentials against database
     */
    private void handleLogin() {
        String accountNumber = accountField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        if (accountNumber.isEmpty() || pin.isEmpty()) {
            UIHelper.showError(this, "Please enter Account Number and PIN!");
            return;
        }

        if (pin.length() != 4) {
            UIHelper.showError(this, "PIN must be 4 digits!");
            return;
        }

        Account account = userDAO.login(accountNumber, pin);

        if (account != null && account.getAccountId() == -1) {
            // Account is frozen by admin
            UIHelper.showError(this, "🔒 Your account has been frozen!\n\nPlease contact the bank administrator.");
            pinField.setText("");
        } else if (account != null) {
            dispose();
            new ATMFrame(account).setVisible(true);
        } else {
            UIHelper.showError(this, "Invalid Account Number or PIN!\nPlease try again.");
            pinField.setText("");
        }
    }
}
