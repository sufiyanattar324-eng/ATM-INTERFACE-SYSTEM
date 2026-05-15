package atm.gui;

import atm.dao.AdminDAO;
import atm.dao.TransactionDAO;
import atm.dao.UserDAO;
import atm.model.Transaction;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminFrame extends JFrame {
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin1234";

    private AdminDAO adminDAO;
    private UserDAO userDAO;
    private TransactionDAO txnDAO;
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JLabel statUsers, statBalance, statTxns, statFrozen, statToday;

    public AdminFrame() {
        adminDAO = new AdminDAO();
        userDAO = new UserDAO();
        txnDAO = new TransactionDAO();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("ATM System - Admin Panel");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIHelper.BG_DARK);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        JLabel title = new JLabel("   Admin Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UIHelper.TEXT_PRIMARY);
        JButton back = UIHelper.createStyledButton("<- Back to Login", UIHelper.ACCENT_RED);
        back.setPreferredSize(new Dimension(160, 34));
        back.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        header.add(title, BorderLayout.WEST);
        header.add(back, BorderLayout.EAST);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UIHelper.BG_DARK);
        tabs.setForeground(UIHelper.TEXT_PRIMARY);
        tabs.setFont(UIHelper.FONT_LABEL);
        tabs.addTab("   Dashboard   ", buildDashboardTab());
        tabs.addTab("   Users   ", buildUsersTab());
        tabs.addTab("   All Transactions   ", buildAllTxnTab());
        tabs.addTab("   Audit Log   ", buildAuditTab());

        main.add(header, BorderLayout.NORTH);
        main.add(tabs, BorderLayout.CENTER);
        setContentPane(main);
        loadStats();
    }

    // ==================== DASHBOARD ====================
    private JPanel buildDashboardTab() {
        JPanel p = new JPanel();
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        p.setLayout(new BorderLayout(0, 20));

        JLabel heading = new JLabel("Bank Statistics Overview");
        heading.setFont(UIHelper.FONT_SUBTITLE);
        heading.setForeground(UIHelper.TEXT_PRIMARY);

        JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
        cards.setBackground(UIHelper.BG_DARK);

        statUsers   = new JLabel("0");
        statBalance = new JLabel("0");
        statTxns    = new JLabel("0");
        statFrozen  = new JLabel("0");
        statToday   = new JLabel("0");

        cards.add(statCard("Total Users", statUsers, UIHelper.ACCENT_BLUE, "Registered accounts"));
        cards.add(statCard("Bank Liquidity", statBalance, UIHelper.ACCENT_GREEN, "Total funds held"));
        cards.add(statCard("Total Transactions", statTxns, new Color(139, 92, 246), "All time"));
        cards.add(statCard("Frozen Accounts", statFrozen, UIHelper.ACCENT_RED, "Currently blocked"));
        cards.add(statCard("Txns Today", statToday, UIHelper.ACCENT_AMBER, "Today's activity"));

        JButton refresh = UIHelper.createStyledButton("Refresh Stats", UIHelper.ACCENT_BLUE);
        refresh.setPreferredSize(new Dimension(180, 38));
        refresh.addActionListener(e -> loadStats());
        JPanel btmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btmPanel.setBackground(UIHelper.BG_DARK);
        btmPanel.add(refresh);

        p.add(heading, BorderLayout.NORTH);
        p.add(cards, BorderLayout.CENTER);
        p.add(btmPanel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent, String sub) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIHelper.BG_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(accent); g2.fillRoundRect(0,0,getWidth(),4,4,4);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel t = new JLabel(title);
        t.setFont(UIHelper.FONT_SMALL);
        t.setForeground(UIHelper.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);

        JLabel s = new JLabel(sub);
        s.setFont(UIHelper.FONT_SMALL);
        s.setForeground(new Color(80, 100, 120));

        card.add(t); card.add(Box.createVerticalStrut(6));
        card.add(valueLabel); card.add(Box.createVerticalStrut(4));
        card.add(s);
        return card;
    }

    private void loadStats() {
        statUsers.setText(String.valueOf(adminDAO.getTotalUsers()));
        statBalance.setText("Rs " + String.format("%,.0f", adminDAO.getTotalBankBalance()));
        statTxns.setText(String.valueOf(adminDAO.getTotalTransactions()));
        statFrozen.setText(String.valueOf(adminDAO.getFrozenAccountCount()));
        statToday.setText(String.valueOf(adminDAO.getTodayTransactionCount()));
    }

    // ==================== USERS TAB ====================
    private JPanel buildUsersTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(UIHelper.BG_DARK);
        JTextField searchField = UIHelper.createStyledTextField(18);
        searchField.setPreferredSize(new Dimension(200, 34));
        JButton searchBtn = UIHelper.createStyledButton("Search", UIHelper.ACCENT_BLUE);
        searchBtn.setPreferredSize(new Dimension(90, 34));
        JButton refreshBtn = UIHelper.createStyledButton("All Users", new Color(14,165,233));
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        searchPanel.add(UIHelper.createLabel("Search:")); searchPanel.add(searchField);
        searchPanel.add(searchBtn); searchPanel.add(refreshBtn);

        // Table
        String[] cols = {"ID","Name","Mobile","Address","Account No","IFSC","Balance","Status","Created"};
        userTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = styledTable(userTableModel);
        JScrollPane scroll = new JScrollPane(userTable);
        scroll.getViewport().setBackground(UIHelper.BG_CARD);
        scroll.setBorder(new LineBorder(UIHelper.BORDER_COLOR));

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actions.setBackground(UIHelper.BG_DARK);

        JButton freezeBtn = UIHelper.createStyledButton("Freeze/Unfreeze", new Color(14,165,233));
        JButton resetBtn  = UIHelper.createStyledButton("Reset PIN", UIHelper.ACCENT_AMBER);
        JButton txnBtn    = UIHelper.createStyledButton("View Transactions", UIHelper.ACCENT_BLUE);
        JButton editBalBtn= UIHelper.createStyledButton("Edit Balance", new Color(139,92,246));
        JButton deleteBtn = UIHelper.createStyledButton("Delete User", UIHelper.ACCENT_RED);
        JButton exportBtn = UIHelper.createStyledButton("Export CSV", UIHelper.ACCENT_GREEN);

        for (JButton b : new JButton[]{freezeBtn,resetBtn,txnBtn,editBalBtn,deleteBtn,exportBtn}) {
            b.setPreferredSize(new Dimension(160, 38));
            actions.add(b);
        }

        p.add(searchPanel, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        p.add(actions, BorderLayout.SOUTH);

        // Wire up actions
        searchBtn.addActionListener(e -> loadUsers(searchField.getText().trim()));
        refreshBtn.addActionListener(e -> { searchField.setText(""); loadUsers(""); });
        freezeBtn.addActionListener(e -> doFreezeToggle());
        resetBtn.addActionListener(e -> doResetPin());
        txnBtn.addActionListener(e -> doViewTransactions());
        editBalBtn.addActionListener(e -> doEditBalance());
        deleteBtn.addActionListener(e -> doDeleteUser());
        exportBtn.addActionListener(e -> doExportCSV());

        loadUsers("");
        return p;
    }

    private void loadUsers(String filter) {
        userTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        for (Object[] u : adminDAO.getAllUsersWithStatus()) {
            String name = (String) u[1];
            String acc  = (String) u[4];
            if (!filter.isEmpty() && !name.toLowerCase().contains(filter.toLowerCase()) && !acc.contains(filter)) continue;
            userTableModel.addRow(new Object[]{
                u[0], u[1], u[2], u[3], u[4], u[5],
                String.format("%,.2f", (Double) u[6]),
                ((Integer) u[7] == 1) ? "FROZEN" : "ACTIVE",
                (u[8] != null) ? sdf.format(u[8]) : "N/A"
            });
        }
    }

    private int selectedUserId() {
        int row = userTable.getSelectedRow();
        if (row < 0) { UIHelper.showError(this, "Select a user first!"); return -1; }
        return (int) userTableModel.getValueAt(row, 0);
    }

    private String selectedValue(int col) {
        int row = userTable.getSelectedRow();
        return row < 0 ? null : (String) userTableModel.getValueAt(row, col);
    }

    private void doFreezeToggle() {
        int row = userTable.getSelectedRow();
        if (row < 0) { UIHelper.showError(this, "Select a user first!"); return; }
        String acc    = (String) userTableModel.getValueAt(row, 4);
        String name   = (String) userTableModel.getValueAt(row, 1);
        String status = (String) userTableModel.getValueAt(row, 7);
        boolean isFrozen = "FROZEN".equals(status);
        String action = isFrozen ? "UNFREEZE" : "FREEZE";
        int confirm = JOptionPane.showConfirmDialog(this,
            action + " account for " + name + "?", action, JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = isFrozen ? adminDAO.unfreezeAccount(acc) : adminDAO.freezeAccount(acc);
        if (ok) {
            adminDAO.logAction(action, name, acc, "Admin " + action.toLowerCase() + "d account");
            UIHelper.showSuccess(this, "Account " + action.toLowerCase() + "d successfully!");
            loadUsers(""); loadStats();
        } else UIHelper.showError(this, "Operation failed.");
    }

    private void doResetPin() {
        int uid = selectedUserId(); if (uid < 0) return;
        String name = selectedValue(1);
        JPanel panel = new JPanel(new GridLayout(3,1,5,5));
        panel.setBackground(UIHelper.BG_CARD);
        JPasswordField np = UIHelper.createStyledPasswordField(10);
        JPasswordField cp = UIHelper.createStyledPasswordField(10);
        panel.add(UIHelper.createLabel("New PIN (4 digits):")); panel.add(np);
        panel.add(UIHelper.createLabel("Confirm PIN:")); panel.add(cp);
        int r = JOptionPane.showConfirmDialog(this, panel, "Reset PIN - "+name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        String pin = new String(np.getPassword());
        if (!pin.equals(new String(cp.getPassword())) || pin.length() != 4 || !pin.matches("\\d+")) {
            UIHelper.showError(this, "PINs must match and be exactly 4 digits!"); return;
        }
        if (adminDAO.resetPin(uid, pin)) {
            adminDAO.logAction("RESET_PIN", name, selectedValue(4), "Admin reset PIN for user");
            UIHelper.showSuccess(this, "PIN reset successfully for " + name + "!");
        } else UIHelper.showError(this, "PIN reset failed.");
    }

    private void doViewTransactions() {
        String acc = selectedValue(4); if (acc == null) { UIHelper.showError(this,"Select a user!"); return; }
        String name = selectedValue(1);
        List<Transaction> txns = txnDAO.getTransactionHistory(acc);
        if (txns.isEmpty()) { UIHelper.showInfo(this,"Transactions","No transactions for "+name); return; }
        String[] cols = {"#","Type","Amount","Balance","Description","Date"};
        DefaultTableModel m = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        for (Transaction t : txns) m.addRow(new Object[]{t.getTransactionId(),t.getTransactionType(),
            String.format("%,.2f",t.getAmount()),String.format("%,.2f",t.getBalanceAfter()),
            t.getDescription(),(t.getTransactionDate()!=null)?sdf.format(t.getTransactionDate()):"N/A"});
        JScrollPane sp = new JScrollPane(styledTable(m));
        sp.setPreferredSize(new Dimension(650,350)); sp.getViewport().setBackground(UIHelper.BG_CARD);
        JOptionPane.showMessageDialog(this,sp,"Transactions - "+name,JOptionPane.PLAIN_MESSAGE);
    }

    private void doEditBalance() {
        int row = userTable.getSelectedRow();
        if (row < 0) { UIHelper.showError(this,"Select a user!"); return; }
        String acc = selectedValue(4), name = selectedValue(1), cur = selectedValue(6);
        JTextField f = UIHelper.createStyledTextField(12);
        JPanel panel = new JPanel(new GridLayout(4,1,5,5)); panel.setBackground(UIHelper.BG_CARD);
        panel.add(UIHelper.createLabel("User: "+name));
        panel.add(UIHelper.createLabel("Current: Rs "+cur));
        panel.add(UIHelper.createLabel("New Balance:")); panel.add(f);
        int r = JOptionPane.showConfirmDialog(this,panel,"Edit Balance",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if (r!=JOptionPane.OK_OPTION) return;
        try {
            double nb = Double.parseDouble(f.getText().trim());
            if (nb<0){UIHelper.showError(this,"Balance cannot be negative!"); return;}
            if (userDAO.updateBalance(acc, nb)) {
                adminDAO.logAction("EDIT_BALANCE", name, acc, "Changed from Rs "+cur+" to Rs "+String.format("%,.2f",nb));
                UIHelper.showSuccess(this,"Balance updated to Rs "+String.format("%,.2f",nb));
                loadUsers(""); loadStats();
            } else UIHelper.showError(this,"Update failed.");
        } catch(NumberFormatException ex){UIHelper.showError(this,"Invalid amount!");}
    }

    private void doDeleteUser() {
        int uid = selectedUserId(); if (uid<0) return;
        String name = selectedValue(1), acc = selectedValue(4);
        int c = JOptionPane.showConfirmDialog(this,"DELETE "+name+"?\nThis cannot be undone!","Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if (c!=JOptionPane.YES_OPTION) return;
        if (userDAO.deleteUser(uid)) {
            adminDAO.logAction("DELETE_USER", name, acc, "Admin deleted user account");
            UIHelper.showSuccess(this,"User "+name+" deleted.");
            loadUsers(""); loadStats();
        } else UIHelper.showError(this,"Delete failed.");
    }

    private void doExportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("atm_users_export.csv"));
        if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
            pw.println("ID,Name,Mobile,Address,Account No,IFSC,Balance,Status,Created");
            for (int i=0;i<userTableModel.getRowCount();i++) {
                StringBuilder sb = new StringBuilder();
                for (int j=0;j<userTableModel.getColumnCount();j++) {
                    if(j>0) sb.append(",");
                    sb.append("\"").append(userTableModel.getValueAt(i,j)).append("\"");
                }
                pw.println(sb);
            }
            UIHelper.showSuccess(this,"Exported to: "+fc.getSelectedFile().getAbsolutePath());
            adminDAO.logAction("EXPORT_CSV","All Users","ALL","Exported user list to CSV");
        } catch(IOException ex) { UIHelper.showError(this,"Export failed: "+ex.getMessage()); }
    }

    // ==================== ALL TRANSACTIONS TAB ====================
    private JPanel buildAllTxnTab() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));
        String[] cols = {"Txn ID","Account No","Name","Type","Amount","Balance After","Description","Date"};
        DefaultTableModel m = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        for (Object[] u : adminDAO.getAllUsersWithStatus()) {
            String acc=(String)u[4], name=(String)u[1];
            for (Transaction t : txnDAO.getTransactionHistory(acc)) {
                m.addRow(new Object[]{t.getTransactionId(),acc,name,t.getTransactionType(),
                    String.format("%,.2f",t.getAmount()),String.format("%,.2f",t.getBalanceAfter()),
                    t.getDescription(),(t.getTransactionDate()!=null)?sdf.format(t.getTransactionDate()):"N/A"});
            }
        }
        JTable table = styledTable(m);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(UIHelper.BG_CARD);
        sp.setBorder(new LineBorder(UIHelper.BORDER_COLOR));
        JLabel lbl = new JLabel("Total: "+m.getRowCount()+" transactions");
        lbl.setForeground(UIHelper.TEXT_SECONDARY); lbl.setFont(UIHelper.FONT_SMALL);
        p.add(lbl, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ==================== AUDIT LOG TAB ====================
    private JPanel buildAuditTab() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));
        String[] cols = {"Log ID","Action","User","Account","Details","Date & Time"};
        DefaultTableModel m = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        for (Object[] log : adminDAO.getAuditLogs()) {
            m.addRow(new Object[]{log[0],log[1],log[2],log[3],log[4],(log[5]!=null)?sdf.format(log[5]):"N/A"});
        }
        JTable table = styledTable(m);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(UIHelper.BG_CARD);
        sp.setBorder(new LineBorder(UIHelper.BORDER_COLOR));
        JButton refresh = UIHelper.createStyledButton("Refresh Log", UIHelper.ACCENT_BLUE);
        refresh.setPreferredSize(new Dimension(160,36));
        refresh.addActionListener(e -> {
            m.setRowCount(0);
            for (Object[] log : adminDAO.getAuditLogs()) {
                m.addRow(new Object[]{log[0],log[1],log[2],log[3],log[4],(log[5]!=null)?sdf.format(log[5]):"N/A"});
            }
        });
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btm.setBackground(UIHelper.BG_DARK); btm.add(refresh);
        JLabel lbl = new JLabel("Every admin action is recorded here for accountability.");
        lbl.setForeground(UIHelper.TEXT_SECONDARY); lbl.setFont(UIHelper.FONT_SMALL);
        p.add(lbl, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        p.add(btm, BorderLayout.SOUTH);
        return p;
    }

    // ==================== HELPERS ====================
    private JTable styledTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setFont(UIHelper.FONT_SMALL); t.setRowHeight(28);
        t.setBackground(UIHelper.BG_CARD); t.setForeground(UIHelper.TEXT_PRIMARY);
        t.setSelectionBackground(UIHelper.ACCENT_BLUE); t.setSelectionForeground(Color.WHITE);
        t.setGridColor(UIHelper.BORDER_COLOR); t.setShowGrid(true);
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI",Font.BOLD,12));
        h.setBackground(new Color(51,65,85)); h.setForeground(UIHelper.TEXT_PRIMARY);
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(JLabel.CENTER);
        for (int i=0;i<t.getColumnCount();i++) t.getColumnModel().getColumn(i).setCellRenderer(cr);
        return t;
    }

    public static boolean showAdminLoginDialog(Component parent) {
        JPanel panel = new JPanel(new GridLayout(5,1,5,5));
        panel.setBackground(UIHelper.BG_CARD);
        JLabel title = new JLabel("Admin Login"); title.setFont(UIHelper.FONT_SUBTITLE); title.setForeground(UIHelper.TEXT_PRIMARY);
        JTextField user = UIHelper.createStyledTextField(15);
        JPasswordField pass = UIHelper.createStyledPasswordField(15);
        panel.add(title);
        panel.add(UIHelper.createLabel("Username:")); panel.add(user);
        panel.add(UIHelper.createLabel("Password:")); panel.add(pass);
        int r = JOptionPane.showConfirmDialog(parent,panel,"Admin Authentication",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if (r==JOptionPane.OK_OPTION) {
            if (user.getText().trim().equals(ADMIN_USERNAME) && new String(pass.getPassword()).equals(ADMIN_PASSWORD)) return true;
            UIHelper.showError(parent,"Invalid admin credentials!");
        }
        return false;
    }
}
