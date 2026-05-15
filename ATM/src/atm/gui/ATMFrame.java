package atm.gui;

import atm.dao.AccountDAO;
import atm.dao.TransactionDAO;
import atm.dao.UserDAO;
import atm.model.Account;
import atm.model.Transaction;
import atm.model.User;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * ATMFrame.java
 * -------------
 * Main ATM menu screen shown after successful login.
 * Provides all ATM operations:
 * - Check Balance
 * - Deposit Money
 * - Withdraw Money
 * - Transfer Money
 * - Transaction History
 * - Logout
 */
public class ATMFrame extends JFrame {

    private Account currentAccount;
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;
    private UserDAO userDAO;

    // UI Components that need updating
    private JLabel balanceLabel;
    private JLabel welcomeLabel;

    public ATMFrame(Account account) {
        this.currentAccount = account;
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("ATM Interface System - Main Menu");
        setSize(680, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(UIHelper.BG_DARK);

        // ===== Top Header =====
        JPanel headerPanel = createHeaderPanel();

        // ===== Balance Card =====
        JPanel balancePanel = createBalanceCard();

        // ===== Menu Grid =====
        JPanel menuPanel = createMenuGrid();

        // ===== Footer =====
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(UIHelper.BG_DARK);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        JLabel footerLabel = new JLabel("Account: " + currentAccount.getAccountNumber()
                + "  |  IFSC: " + currentAccount.getIfscCode(), SwingConstants.CENTER);
        footerLabel.setFont(UIHelper.FONT_SMALL);
        footerLabel.setForeground(new Color(100, 116, 139));
        footerPanel.add(footerLabel);

        // ===== Assemble =====
        JPanel topSection = new JPanel();
        topSection.setBackground(UIHelper.BG_DARK);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.add(headerPanel);
        topSection.add(balancePanel);

        mainPanel.add(topSection, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Create the top header with welcome message and logout button
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Get user name
        User user = userDAO.getUserById(currentAccount.getUserId());
        String userName = (user != null) ? user.getFullName() : "User";

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(30, 41, 59));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(UIHelper.FONT_SMALL);
        welcomeLabel.setForeground(UIHelper.TEXT_SECONDARY);

        JLabel nameLabel = new JLabel(userName);
        nameLabel.setFont(UIHelper.FONT_SUBTITLE);
        nameLabel.setForeground(UIHelper.TEXT_PRIMARY);

        leftPanel.add(welcomeLabel);
        leftPanel.add(nameLabel);

        // Logout button
        JButton logoutBtn = UIHelper.createStyledButton("Logout ↗", UIHelper.ACCENT_RED);
        logoutBtn.setPreferredSize(new Dimension(100, 36));
        logoutBtn.addActionListener(e -> handleLogout());

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(logoutBtn, BorderLayout.EAST);

        return panel;
    }

    /**
     * Create the balance display card
     */
    private JPanel createBalanceCard() {
        JPanel wrapper = new JPanel();
        wrapper.setBackground(UIHelper.BG_DARK);
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 25, 5, 25));
        wrapper.setLayout(new BorderLayout());

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(59, 130, 246),
                        getWidth(), getHeight(), new Color(99, 102, 241));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        card.setOpaque(false);

        JLabel titleLabel = new JLabel("Available Balance");
        titleLabel.setFont(UIHelper.FONT_LABEL);
        titleLabel.setForeground(new Color(200, 220, 255));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Refresh balance from database
        double balance = accountDAO.getBalance(currentAccount.getAccountNumber());
        currentAccount.setBalance(balance);

        balanceLabel = new JLabel("₹ " + String.format("%,.2f", balance));
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel accLabel = new JLabel("A/C: " + currentAccount.getAccountNumber());
        accLabel.setFont(UIHelper.FONT_SMALL);
        accLabel.setForeground(new Color(180, 200, 255));
        accLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(balanceLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(accLabel);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Create the menu grid with ATM operation buttons
     */
    private JPanel createMenuGrid() {
        JPanel wrapper = new JPanel();
        wrapper.setBackground(UIHelper.BG_DARK);
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        wrapper.setLayout(new BorderLayout());

        JLabel menuTitle = new JLabel("Quick Actions");
        menuTitle.setFont(UIHelper.FONT_SUBTITLE);
        menuTitle.setForeground(UIHelper.TEXT_PRIMARY);
        menuTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));

        // Grid of buttons
        JPanel grid = new JPanel(new GridLayout(3, 2, 12, 12));
        grid.setBackground(UIHelper.BG_DARK);

        // Menu buttons with icons
        grid.add(createMenuButton("💰", "Check Balance", "View your current balance",
                UIHelper.ACCENT_BLUE, e -> handleCheckBalance()));

        grid.add(createMenuButton("📥", "Deposit", "Add money to your account",
                UIHelper.ACCENT_GREEN, e -> handleDeposit()));

        grid.add(createMenuButton("📤", "Withdraw", "Withdraw cash from ATM",
                UIHelper.ACCENT_AMBER, e -> handleWithdraw()));

        grid.add(createMenuButton("🔄", "Transfer", "Send money to another account",
                new Color(139, 92, 246), e -> handleTransfer()));

        grid.add(createMenuButton("📋", "History", "View transaction history",
                new Color(14, 165, 233), e -> handleTransactionHistory()));

        grid.add(createMenuButton("🚪", "Logout", "Exit to login screen",
                UIHelper.ACCENT_RED, e -> handleLogout()));

        wrapper.add(menuTitle, BorderLayout.NORTH);
        wrapper.add(grid, BorderLayout.CENTER);

        return wrapper;
    }

    /**
     * Create a menu button card with icon, title, and description
     */
    private JPanel createMenuButton(String icon, String title, String desc,
                                     Color accentColor, java.awt.event.ActionListener action) {
        JPanel card = new JPanel() {
            Color bgColor = UIHelper.BG_CARD;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        bgColor = new Color(51, 65, 85);
                        repaint();
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        bgColor = UIHelper.BG_CARD;
                        repaint();
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        action.actionPerformed(null);
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Accent line at top
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(UIHelper.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(UIHelper.FONT_SMALL);
        descLabel.setForeground(UIHelper.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(descLabel);

        return card;
    }

    /**
     * Refresh the balance display
     */
    private void refreshBalance() {
        double balance = accountDAO.getBalance(currentAccount.getAccountNumber());
        currentAccount.setBalance(balance);
        balanceLabel.setText("₹ " + String.format("%,.2f", balance));
    }

    // ================= ATM Operations =================

    /**
     * Check Balance - Display current balance
     */
    private void handleCheckBalance() {
        refreshBalance();
        double balance = currentAccount.getBalance();

        JPanel panel = new JPanel();
        panel.setBackground(UIHelper.BG_CARD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Your Current Balance");
        titleLbl.setFont(UIHelper.FONT_SUBTITLE);
        titleLbl.setForeground(UIHelper.TEXT_PRIMARY);

        JLabel balanceLbl = new JLabel("₹ " + String.format("%,.2f", balance));
        balanceLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        balanceLbl.setForeground(UIHelper.ACCENT_GREEN);

        JLabel accLbl = new JLabel("Account: " + currentAccount.getAccountNumber());
        accLbl.setFont(UIHelper.FONT_SMALL);
        accLbl.setForeground(UIHelper.TEXT_SECONDARY);

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(balanceLbl);
        panel.add(Box.createVerticalStrut(5));
        panel.add(accLbl);

        JOptionPane.showMessageDialog(this, panel, "Balance Inquiry", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Deposit Money - Add money to account
     */
    private void handleDeposit() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBackground(UIHelper.BG_CARD);

        JLabel label = UIHelper.createLabel("Enter amount to deposit (₹):");
        JTextField amountField = UIHelper.createStyledTextField(15);

        panel.add(label);
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Deposit Money",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());

                if (amount <= 0) {
                    UIHelper.showError(this, "Amount must be greater than zero!");
                    return;
                }

                if (amount > 1000000) {
                    UIHelper.showError(this, "Maximum deposit limit is ₹10,00,000 per transaction!");
                    return;
                }

                boolean success = accountDAO.deposit(currentAccount.getAccountNumber(), amount);

                if (success) {
                    refreshBalance();
                    UIHelper.showSuccess(this, "✅ ₹" + String.format("%,.2f", amount)
                            + " deposited successfully!\n\nNew Balance: ₹"
                            + String.format("%,.2f", currentAccount.getBalance()));
                } else {
                    UIHelper.showError(this, "Deposit failed. Please try again.");
                }

            } catch (NumberFormatException ex) {
                UIHelper.showError(this, "Please enter a valid amount!");
            }
        }
    }

    /**
     * Withdraw Money - Deduct money from account
     */
    private void handleWithdraw() {
        refreshBalance();

        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBackground(UIHelper.BG_CARD);

        JLabel balLbl = new JLabel("Available: ₹" + String.format("%,.2f", currentAccount.getBalance()));
        balLbl.setFont(UIHelper.FONT_LABEL);
        balLbl.setForeground(UIHelper.ACCENT_GREEN);

        JLabel label = UIHelper.createLabel("Enter amount to withdraw (₹):");
        JTextField amountField = UIHelper.createStyledTextField(15);

        panel.add(balLbl);
        panel.add(label);
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Withdraw Money",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());

                if (amount <= 0) {
                    UIHelper.showError(this, "Amount must be greater than zero!");
                    return;
                }

                int status = accountDAO.withdraw(currentAccount.getAccountNumber(), amount);

                switch (status) {
                    case 1:  // Success
                        refreshBalance();
                        UIHelper.showSuccess(this, "✅ ₹" + String.format("%,.2f", amount)
                                + " withdrawn successfully!\n\nRemaining Balance: ₹"
                                + String.format("%,.2f", currentAccount.getBalance()));
                        break;
                    case 0:  // Insufficient balance
                        UIHelper.showError(this, "❌ Insufficient Balance!\n\nYour balance: ₹"
                                + String.format("%,.2f", currentAccount.getBalance())
                                + "\nRequested: ₹" + String.format("%,.2f", amount));
                        break;
                    default: // Error
                        UIHelper.showError(this, "Withdrawal failed. Please try again.");
                }

            } catch (NumberFormatException ex) {
                UIHelper.showError(this, "Please enter a valid amount!");
            }
        }
    }

    /**
     * Transfer Money - Send money to another account
     * Now shows receiver's name for verification before transfer
     */
    private void handleTransfer() {
        refreshBalance();

        JPanel panel = new JPanel();
        panel.setBackground(UIHelper.BG_CARD);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.gridx = 0;

        JLabel balLbl = new JLabel("Available: ₹" + String.format("%,.2f", currentAccount.getBalance()));
        balLbl.setFont(UIHelper.FONT_LABEL);
        balLbl.setForeground(UIHelper.ACCENT_GREEN);

        gbc.gridy = 0;
        panel.add(balLbl, gbc);

        gbc.gridy = 1;
        panel.add(UIHelper.createLabel("Receiver Account Number:"), gbc);

        JTextField receiverField = UIHelper.createStyledTextField(20);
        gbc.gridy = 2;
        panel.add(receiverField, gbc);

        gbc.gridy = 3;
        panel.add(UIHelper.createLabel("Amount to Transfer (₹):"), gbc);

        JTextField amountField = UIHelper.createStyledTextField(20);
        gbc.gridy = 4;
        panel.add(amountField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Transfer Money",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String receiverAcc = receiverField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());

                if (receiverAcc.isEmpty()) {
                    UIHelper.showError(this, "Please enter receiver's account number!");
                    return;
                }

                if (receiverAcc.equals(currentAccount.getAccountNumber())) {
                    UIHelper.showError(this, "Cannot transfer to your own account!");
                    return;
                }

                if (amount <= 0) {
                    UIHelper.showError(this, "Amount must be greater than zero!");
                    return;
                }

                // Look up receiver's name from database
                String receiverName = userDAO.getNameByAccountNumber(receiverAcc);

                if (receiverName == null) {
                    UIHelper.showError(this, "❌ Receiver account not found!\nPlease check the account number.");
                    return;
                }

                // Show confirmation with receiver's NAME
                JPanel confirmPanel = new JPanel();
                confirmPanel.setBackground(UIHelper.BG_CARD);
                confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.Y_AXIS));

                JLabel confirmTitle = new JLabel("Confirm Transfer Details");
                confirmTitle.setFont(UIHelper.FONT_SUBTITLE);
                confirmTitle.setForeground(UIHelper.TEXT_PRIMARY);

                JLabel nameRow = new JLabel("👤  Receiver: " + receiverName);
                nameRow.setFont(new Font("Segoe UI", Font.BOLD, 15));
                nameRow.setForeground(UIHelper.ACCENT_GREEN);

                JLabel accRow = new JLabel("🏦  Account: " + receiverAcc);
                accRow.setFont(UIHelper.FONT_LABEL);
                accRow.setForeground(UIHelper.TEXT_PRIMARY);

                JLabel amtRow = new JLabel("💰  Amount: ₹" + String.format("%,.2f", amount));
                amtRow.setFont(new Font("Segoe UI", Font.BOLD, 16));
                amtRow.setForeground(new Color(245, 158, 11));

                JLabel warningRow = new JLabel("⚠  Please verify the receiver's name before confirming.");
                warningRow.setFont(UIHelper.FONT_SMALL);
                warningRow.setForeground(UIHelper.TEXT_SECONDARY);

                confirmPanel.add(confirmTitle);
                confirmPanel.add(Box.createVerticalStrut(15));
                confirmPanel.add(nameRow);
                confirmPanel.add(Box.createVerticalStrut(6));
                confirmPanel.add(accRow);
                confirmPanel.add(Box.createVerticalStrut(6));
                confirmPanel.add(amtRow);
                confirmPanel.add(Box.createVerticalStrut(12));
                confirmPanel.add(warningRow);

                int confirm = JOptionPane.showConfirmDialog(this, confirmPanel,
                        "Confirm Transfer", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    int status = accountDAO.transfer(currentAccount.getAccountNumber(),
                            receiverAcc, amount);

                    switch (status) {
                        case 1:  // Success
                            refreshBalance();
                            UIHelper.showSuccess(this, "✅ Transfer Successful!\n\n₹"
                                    + String.format("%,.2f", amount) + " sent to " + receiverName
                                    + "\nA/C: " + receiverAcc
                                    + "\n\nYour Balance: ₹"
                                    + String.format("%,.2f", currentAccount.getBalance()));
                            break;
                        case 0:  // Insufficient balance
                            UIHelper.showError(this, "❌ Insufficient Balance!");
                            break;
                        case -1: // Receiver not found
                            UIHelper.showError(this, "❌ Receiver account not found!\nPlease check the account number.");
                            break;
                        default: // Error
                            UIHelper.showError(this, "Transfer failed. Please try again.");
                    }
                }

            } catch (NumberFormatException ex) {
                UIHelper.showError(this, "Please enter a valid amount!");
            }
        }
    }

    /**
     * Transaction History - Show all transactions in a table
     */
    private void handleTransactionHistory() {
        List<Transaction> transactions = transactionDAO.getTransactionHistory(
                currentAccount.getAccountNumber());

        if (transactions.isEmpty()) {
            UIHelper.showInfo(this, "Transaction History", "No transactions found for this account.");
            return;
        }

        // Create table
        String[] columns = {"#", "Type", "Amount (₹)", "Balance (₹)", "Description", "Date & Time"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Make table read-only
            }
        };

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

        for (Transaction txn : transactions) {
            model.addRow(new Object[]{
                txn.getTransactionId(),
                txn.getTransactionType(),
                String.format("%,.2f", txn.getAmount()),
                String.format("%,.2f", txn.getBalanceAfter()),
                txn.getDescription(),
                (txn.getTransactionDate() != null) ? sdf.format(txn.getTransactionDate()) : "N/A"
            });
        }

        JTable table = new JTable(model);
        table.setFont(UIHelper.FONT_SMALL);
        table.setRowHeight(30);
        table.setBackground(UIHelper.BG_CARD);
        table.setForeground(UIHelper.TEXT_PRIMARY);
        table.setSelectionBackground(UIHelper.ACCENT_BLUE);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(UIHelper.BORDER_COLOR);
        table.setShowGrid(true);

        // Style the header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(51, 65, 85));
        header.setForeground(UIHelper.TEXT_PRIMARY);
        header.setBorder(new LineBorder(UIHelper.BORDER_COLOR));

        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(620, 350));
        scrollPane.getViewport().setBackground(UIHelper.BG_CARD);
        scrollPane.setBorder(new LineBorder(UIHelper.BORDER_COLOR));

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIHelper.BG_CARD);

        JLabel titleLbl = new JLabel("Transaction History (" + transactions.size() + " records)");
        titleLbl.setFont(UIHelper.FONT_SUBTITLE);
        titleLbl.setForeground(UIHelper.TEXT_PRIMARY);

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, "Transaction History", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Logout - Return to login screen
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
