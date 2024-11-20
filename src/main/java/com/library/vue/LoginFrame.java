package com.library.vue;

import com.library.controller.LibraryController;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.AbstractBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

public class LoginFrame extends JFrame {
    private LibraryController controller;
    private JTextField emailField;
    private JPasswordField passwordField;
    private final Color PRIMARY_COLOR = new Color(255, 89, 0);  // Bright Orange
    private final Color SECONDARY_COLOR = new Color(42, 42, 42);  // Dark Gray
    private final Color BACKGROUND_COLOR = Color.WHITE;
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 36);
    private final Font SUBTITLE_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 14);
    private final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public LoginFrame(LibraryController controller) {
        this.controller = controller;
        setupUI();
        setUndecorated(true); // Remove window decorations
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
    }

    private void setupUI() {
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);

        // Main panel with custom background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, BACKGROUND_COLOR,
                        getWidth(), getHeight(), new Color(250, 250, 250)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);

        // Left Panel (Image/Decoration)
        JPanel leftPanel = createLeftPanel();

        // Right Panel (Login Form)
        JPanel rightPanel = createRightPanel();

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(500);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Close button
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        titleBar.setOpaque(false);
        JButton closeButton = createCloseButton();
        titleBar.add(closeButton);
        mainPanel.add(titleBar, BorderLayout.NORTH);

        add(mainPanel);

        // Make window draggable
        new ComponentMover(this);
    }

    private JPanel createLeftPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, PRIMARY_COLOR,
                        getWidth(), getHeight(), PRIMARY_COLOR.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add decorative elements
                g2d.setColor(new Color(255, 255, 255, 30));

                // Top circle
                g2d.fillOval(-100, -100, 300, 300);

                // Bottom circle
                g2d.fillOval(getWidth()-200, getHeight()-200, 400, 400);

                // Middle design element
                int centerY = getHeight() / 2;
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(2));
                for (int i = 0; i < 5; i++) {
                    g2d.drawRoundRect(50 + i*10, centerY - 100 + i*10, 300, 200, 20, 20);
                }

                // Welcome text
                g2d.setColor(Color.WHITE);
                g2d.setFont(TITLE_FONT);
                String welcomeText = "Welcome to Bibl-Tech";
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(welcomeText)) / 2;
                g2d.drawString(welcomeText, textX, centerY - 20);

                g2d.setFont(SUBTITLE_FONT);
                String subText = "Please login to your account";
                fm = g2d.getFontMetrics();
                textX = (getWidth() - fm.stringWidth(subText)) / 2;
                g2d.drawString(subText, textX, centerY + 20);
            }
        };
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BACKGROUND_COLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 40, 5, 40);

        // Title
        JLabel titleLabel = new JLabel("Sign In");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(SECONDARY_COLOR);
        gbc.insets = new Insets(0, 40, 30, 40);
        panel.add(titleLabel, gbc);

        // Username
        panel.add(createInputLabel("Username"), gbc);
        emailField = createStyledTextField("Enter your username");
        gbc.insets = new Insets(5, 40, 20, 40);
        panel.add(emailField, gbc);

        // Password
        gbc.insets = new Insets(0, 40, 5, 40);
        panel.add(createInputLabel("Password"), gbc);
        passwordField = createStyledPasswordField("Enter your password :");
        gbc.insets = new Insets(5, 40, 30, 40);
        panel.add(passwordField, gbc);

        // Login Button
        JButton loginButton = createStyledButton("LOGIN");
        loginButton.addActionListener(e -> login());
        panel.add(loginButton, gbc);

        return panel;
    }

    private JLabel createInputLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBTITLE_FONT);
        label.setForeground(SECONDARY_COLOR);
        return label;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(150, 150, 150));
                    g2d.setFont(INPUT_FONT);
                    g2d.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
                }
            }
        };
        styleInputField(field);
        return field;
    }

    private JPasswordField createStyledPasswordField(String s) {
        JPasswordField field = new JPasswordField();
        styleInputField(field);
        return field;
    }

    private void styleInputField(JComponent field) {
        field.setFont(INPUT_FONT);
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(new Color(200, 200, 200), 10),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        field.setBackground(BACKGROUND_COLOR);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(PRIMARY_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(PRIMARY_COLOR.brighter());
                } else {
                    g2d.setColor(PRIMARY_COLOR);
                }

                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(300, 45));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createCloseButton() {
        JButton button = new JButton("×") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getModel().isRollover()) {
                    g.setColor(new Color(255, 89, 89));
                    g.fillOval(0, 0, getWidth(), getHeight());
                }
                g.setColor(getModel().isRollover() ? Color.WHITE : SECONDARY_COLOR);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                g.drawString("×",
                        (getWidth() - fm.stringWidth("×")) / 2,
                        ((getHeight() - fm.getHeight()) / 2) + fm.getAscent());
            }
        };
        button.setPreferredSize(new Dimension(30, 30));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> System.exit(0));
        return button;
    }

    private void login() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (controller.login(email, password)) {
            new MainFrame(controller).setVisible(true);
            dispose();
        } else {
            showError("Invalid credentials!");
        }
    }

    private void showError(String message) {
        JDialog dialog = new JDialog(this, "", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2d.setColor(Color.RED);
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Error icon and message
        JLabel iconLabel = new JLabel("⚠");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setForeground(Color.RED);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(INPUT_FONT);

        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        messagePanel.setOpaque(false);
        messagePanel.add(iconLabel);
        messagePanel.add(messageLabel);

        panel.add(messagePanel, BorderLayout.CENTER);

        // OK button
        JButton okButton = createStyledButton("OK");
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Custom round border
    private class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        RoundBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
    }

    private class ComponentMover extends MouseAdapter {
        private Point clickPoint;
        private JFrame frame;

        public ComponentMover(JFrame frame) {
            this.frame = frame;
            frame.addMouseListener(this);
            frame.addMouseMotionListener(this);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            clickPoint = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point dragPoint = e.getPoint();
            Point framePoint = frame.getLocation();
            frame.setLocation(
                    framePoint.x + dragPoint.x - clickPoint.x,
                    framePoint.y + dragPoint.y - clickPoint.y
            );
        }
    }
}
