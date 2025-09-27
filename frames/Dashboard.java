package frames;

import app.App;
import utilities.Database;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Dashboard extends JFrame {

    private final String username;
    private static final Color PRIMARY_COLOR = new Color(99, 102, 241);
    private static final Color SECONDARY_COLOR = new Color(248, 250, 252);
    private static final Color ACCENT_COLOR = new Color(239, 68, 68);
    private static final Color ASSESSMENT_COLOR = new Color(59, 130, 246);
    private static final Color CARD_SHADOW = new Color(0, 0, 0, 15);

    private JLabel badge;
    private int lastReminderCount = -1;

    public Dashboard(String username) {
        this.username = username;

        setTitle("Dashboard - " + username);
        setIconImage(App.getIcon());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(SECONDARY_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        setVisible(true);

        // Check for pending reminders
        Timer timer = new Timer(1000, e -> checkPendingReminders());
        timer.setRepeats(false);
        timer.start();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 40, 15, 40));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));

        //User profile
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        profilePanel.setOpaque(false);

        JLabel profilePic = new JLabel("👤") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(79, 70, 229));
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("👤")) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("👤", x, y);

                g2.dispose();
            }
        };
        profilePic.setPreferredSize(new Dimension(50, 50));

        // User info
        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        userInfoPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeLabel.setForeground(new Color(225, 225, 225));

        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        usernameLabel.setForeground(Color.WHITE);

        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(usernameLabel);

        profilePanel.add(profilePic);
        profilePanel.add(userInfoPanel);


        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        JButton notificationBtn = createIconButton("🔔", 40, new Color(255, 255, 255, 30));
        notificationBtn.addActionListener(e -> {
            new Reminders(username, Dashboard.this).checkPendingActivities();
            updateNotificationBadge();
        });

        // Badge for notifications
        badge = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                int count = getReminderCount();
                if (count > 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(ACCENT_COLOR);
                    g2.fillOval(0, 0, getWidth(), getHeight());

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    String text = count > 9 ? "9+" : String.valueOf(count);
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(text, x, y);

                    g2.dispose();
                }
            }
        };
        badge.setPreferredSize(new Dimension(18, 18));

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(40, 40));
        layeredPane.setOpaque(false);

        notificationBtn.setBounds(0, 0, 40, 40);
        badge.setBounds(25, 5, 18, 18);

        layeredPane.add(notificationBtn, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(badge, JLayeredPane.PALETTE_LAYER);

        // Profile button
        JButton profileBtn = createTextButton("Profile", 80, 40);
        profileBtn.addActionListener(e -> new Profile(username));

        // Logout button
        JButton logoutBtn = createTextButton("Logout", 80, 40);
        logoutBtn.addActionListener(e -> logout());

        rightPanel.add(layeredPane);
        rightPanel.add(profileBtn);
        rightPanel.add(logoutBtn);

        headerPanel.add(profilePanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        updateNotificationBadge();

        return headerPanel;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new Welcome();
        }
    }

    private JButton createIconButton(String icon, int size, Color bgColor) {
        JButton button = new JButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(bgColor);
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(icon, x, y);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(size, size));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JButton createTextButton(String text, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, x, y);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(width, height));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 255, 255, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 255, 255, 30));
            }
        });

        return button;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(SECONDARY_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 40, 40, 40));

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Your Activities");
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 41, 59));

        titlePanel.add(titleLabel);

        // Cards panel
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Mood & Stress card
        cardsPanel.add(createFeatureCard(
                "Mood & Stress",
                "Track your emotional state",
                "🧠",
                PRIMARY_COLOR,
                e -> {
                    new Mood(username);
                    updateNotificationBadge();
                }
        ));

        // Journal card
        cardsPanel.add(createFeatureCard(
                "Journal",
                "Record your thoughts",
                "📔",
                PRIMARY_COLOR,
                e -> {
                    new Journal(username);
                    updateNotificationBadge();
                }
        ));

        // Resources card
        cardsPanel.add(createFeatureCard(
                "Resources",
                "Helpful materials",
                "📚",
                PRIMARY_COLOR,
                e -> new Resources(username)
        ));

        // Daily Assessment card
        cardsPanel.add(createFeatureCard(
                "Daily Assessment",
                "Complete your daily check-in",
                "📝",
                ASSESSMENT_COLOR,
                e -> {
                    new DailyAssessment(username);
                    updateNotificationBadge();
                }
        ));

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(cardsPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createFeatureCard(String title, String description, String emoji, Color color, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(CARD_SHADOW);
                g2.fillRoundRect(2, 4, getWidth()-4, getHeight()-4, 20, 20);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-4, 20, 20);

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.actionPerformed(new java.awt.event.ActionEvent(card, 0, ""));
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 2),
                        BorderFactory.createEmptyBorder(23, 23, 23, 23)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
            }
        });

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emojiLabel = new JLabel(emoji) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(emoji)) / 2;
                int y = fm.getAscent() - (fm.getAscent() - fm.getHeight())/4; // Adjusted vertical position
                g2.drawString(emoji, x, y);

                g2.dispose();
            }
        };
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emojiLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0)); // Reduced bottom padding from 15 to 5

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(100, 116, 139));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton actionButton = new JButton("Click here") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("Click here")) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("Click here", x, y);

                g2.dispose();
            }
        };

        actionButton.setPreferredSize(new Dimension(100, 35));
        actionButton.setContentAreaFilled(false);
        actionButton.setBorderPainted(false);
        actionButton.setFocusPainted(false);
        actionButton.addActionListener(action);

        buttonPanel.add(actionButton);

        contentPanel.add(emojiLabel);
        contentPanel.add(titleLabel);
        contentPanel.add(descLabel);
        contentPanel.add(buttonPanel);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private void checkPendingReminders() {
        List<String> pendingReminders = getPendingReminders();
        if (!pendingReminders.isEmpty()) {
            showReminderDialog(pendingReminders);
        }
    }

    private List<String> getPendingReminders() {
        List<String> pending = new ArrayList<>();
        LocalDate today = LocalDate.now();

        try {
            if (!isActivityCompleted("user_assessment_responses", "response_time", today)) {
                pending.add("Daily Assessment");
            }
            if (!isActivityCompleted("mood_logs", "log_time", today)) {
                pending.add("Mood & Stress Check");
            }
            if (!isActivityCompleted("journal_entries", "entry_date", today)) {
                pending.add("Daily Journal");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error checking activities: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return pending;
    }

    private boolean isActivityCompleted(String tableName, String dateColumn, LocalDate date) throws SQLException {
        String sql = String.format(
                "SELECT COUNT(*) FROM %s WHERE username = ? AND DATE(%s) = ?",
                tableName, dateColumn
        );

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void showReminderDialog(List<String> pendingReminders) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Pending Activities", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);


        JLabel messageLabel = new JLabel("You need to complete:");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));


        JPanel activitiesPanel = new JPanel();
        activitiesPanel.setLayout(new BoxLayout(activitiesPanel, BoxLayout.Y_AXIS));

        for (String activity : pendingReminders) {
            JLabel activityLabel = new JLabel("• " + activity);
            activityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            activitiesPanel.add(activityLabel);
        }

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(activitiesPanel, BorderLayout.SOUTH);


        JOptionPane.showMessageDialog(
                this,
                panel,
                "Reminders",
                JOptionPane.INFORMATION_MESSAGE
        );

        updateNotificationBadge();
    }

    private int getReminderCount() {
        if (lastReminderCount == -1) {
            lastReminderCount = calculateReminderCount();
        }
        return lastReminderCount;
    }

    private int calculateReminderCount() {
        try (Connection conn = Database.getConnection()) {
            int count = 0;
            LocalDate today = LocalDate.now();

            // Daily Assessment check - using response_time
            if (!isActivityCompleted(conn, "user_assessment_responses", "response_time", today)) {
                count++;
            }

            // Mood Check - using log_time
            if (!isActivityCompleted(conn, "mood_logs", "log_time", today)) {
                count++;
            }

            // Journal Entry check
            if (!isActivityCompleted(conn, "journal_entries", "entry_date", today)) {
                count++;
            }

            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean isActivityCompleted(Connection conn, String tableName,
                                        String dateColumn, LocalDate date) throws SQLException {
        String sql = String.format(
                "SELECT COUNT(*) FROM %s WHERE username = ? AND DATE(%s) = ?",
                tableName, dateColumn
        );

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void updateNotificationBadge() {
        lastReminderCount = -1; // Force refresh
        badge.repaint();
    }
}