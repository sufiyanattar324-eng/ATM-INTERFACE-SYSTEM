package atm.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * UIHelper.java
 * -------------
 * Utility class for creating styled Swing components.
 * Provides a consistent dark theme across all screens.
 * This keeps GUI code clean and avoids repetition (DRY principle).
 */
public class UIHelper {

    // ========== Color Palette ==========
    public static final Color BG_DARK       = new Color(15, 23, 42);      // Dark navy background
    public static final Color BG_CARD       = new Color(30, 41, 59);      // Card background
    public static final Color BG_INPUT      = new Color(51, 65, 85);      // Input field background
    public static final Color ACCENT_BLUE   = new Color(59, 130, 246);    // Primary blue
    public static final Color ACCENT_GREEN  = new Color(34, 197, 94);     // Success green
    public static final Color ACCENT_RED    = new Color(239, 68, 68);     // Error red
    public static final Color ACCENT_AMBER  = new Color(245, 158, 11);    // Warning amber
    public static final Color TEXT_PRIMARY   = new Color(241, 245, 249);   // White text
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);   // Gray text
    public static final Color BORDER_COLOR  = new Color(71, 85, 105);     // Border color

    // ========== Font Constants ==========
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);

    /**
     * Create a styled text field with dark theme
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_INPUT);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        field.setPreferredSize(new Dimension(380, 42));
        return field;
    }

    /**
     * Create a styled password field with dark theme
     */
    public static JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(FONT_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_INPUT);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        field.setPreferredSize(new Dimension(380, 42));
        return field;
    }

    /**
     * Create a styled label
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /**
     * Create a title label
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    /**
     * Create a styled button with hover effects
     */
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(320, 50));

        // Hover effect
        Color hoverColor = bgColor.brighter();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    /**
     * Create a vector-drawn logo panel to avoid emoji box issues
     */
    public static JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                // Draw a stylized shield/bank logo
                g2.setColor(ACCENT_BLUE);
                g2.fillRoundRect(cx - 30, cy - 25, 60, 50, 15, 15);
                
                g2.setColor(new Color(99, 102, 241));
                g2.fillRoundRect(cx - 20, cy - 15, 40, 30, 8, 8);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String text = "₹";
                g2.drawString(text, cx - fm.stringWidth(text)/2, cy + fm.getAscent()/2 - 2);
                
                g2.dispose();
            }
        };
        logoPanel.setPreferredSize(new Dimension(80, 80));
        logoPanel.setMaximumSize(new Dimension(80, 80));
        logoPanel.setOpaque(false);
        return logoPanel;
    }

    /**
     * Create a card-style panel with dark background
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(35, 40, 35, 40)
        ));
        return panel;
    }

    /**
     * Show a styled error message dialog
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show a styled success message dialog
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show a styled info message dialog
     */
    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Set dark look and feel for the application
     */
    public static void setDarkLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            // Customize JOptionPane colors
            UIManager.put("OptionPane.background", BG_CARD);
            UIManager.put("Panel.background", BG_CARD);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
            UIManager.put("Button.background", ACCENT_BLUE);
            UIManager.put("Button.foreground", Color.WHITE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
