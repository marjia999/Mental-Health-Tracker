package frames;

import utilities.Database;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.Year;

public class Profile extends JFrame {
    private final String username;

    // Modern color palette
    private static final Color PRIMARY_COLOR = new Color(99, 102, 241); // Indigo
    private static final Color SECONDARY_COLOR = new Color(255, 255, 255); // Pure white
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251); // Light gray bg
    private static final Color TEXT_COLOR = new Color(55, 65, 81); // Dark gray
    private static final Color ACCENT_COLOR = new Color(236, 72, 153); // Pink accent

    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 28);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font INFO_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font VALUE_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 16);
    private static final Font COPYRIGHT_FONT = new Font("Segoe UI", Font.PLAIN, 11);

    // User data fields
    private int height;
    private int weight;
    private int birthYear;
    private String gender;

    public Profile(String username) {
        this.username = username;
        loadUserData();
        initializeUI();
        setVisible(true);
    }

    private void loadUserData() {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT height, weight, birthYear, gender FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                height = rs.getInt("height");
                weight = rs.getInt("weight");
                birthYear = rs.getInt("birthYear");
                gender = rs.getString("gender");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading user data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        setTitle("Profile - " + username);
        setIconImage(app.App.getIcon());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(30, 50, 20, 50));
        headerPanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Mental Health Tracker");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);

        JLabel userLabel = new JLabel("User Profile");
        userLabel.setFont(SUBTITLE_FONT);
        userLabel.setForeground(TEXT_COLOR);
        userLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(userLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(0, 50, 30, 50));
        contentPanel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        // Profile Card
        JPanel profileCard = createProfileCard();
        contentPanel.add(profileCard, gbc);

        // Stats Card
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel statsCard = createStatsCard();
        contentPanel.add(statsCard, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        footerPanel.setBackground(BACKGROUND_COLOR);

        JLabel copyrightLabel = new JLabel("Developed by Afsana Hena & Marjia Khatun © 2025");
        copyrightLabel.setFont(COPYRIGHT_FONT);
        copyrightLabel.setForeground(TEXT_COLOR.brighter());

        footerPanel.add(copyrightLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createProfileCard() {
        JPanel card = new JPanel(new BorderLayout(30, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        card.setBackground(SECONDARY_COLOR);
        card.setMaximumSize(new Dimension(800, 200));

        // Profile picture (initials)
        JLabel profilePic = new JLabel(getInitials(username), SwingConstants.CENTER);
        profilePic.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 36));
        profilePic.setForeground(Color.WHITE);
        profilePic.setOpaque(true);
        profilePic.setBackground(PRIMARY_COLOR);
        profilePic.setPreferredSize(new Dimension(120, 120));
        profilePic.setBorder(BorderFactory.createEmptyBorder());

        // Make it circular
        profilePic.setBorder(new RoundedBorder(60, PRIMARY_COLOR.darker()));

        // User info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(SECONDARY_COLOR);
        infoPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel nameLabel = new JLabel(username);
        nameLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 24));
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel genderLabel = new JLabel(gender);
        genderLabel.setFont(INFO_FONT);
        genderLabel.setForeground(TEXT_COLOR.brighter());

        infoPanel.add(nameLabel);
        infoPanel.add(genderLabel);

        card.add(profilePic, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStatsCard() {
        JPanel card = new JPanel(new GridLayout(1, 4, 20, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card.setBackground(SECONDARY_COLOR);
        card.setMaximumSize(new Dimension(800, 120));

        // Calculate age
        int currentYear = Year.now().getValue();
        int age = currentYear - birthYear;

        // Age stat
        card.add(createStatPanel("Age", age + " years"));

        // Height stat
        card.add(createStatPanel("Height", height + " cm"));

        // Weight stat
        card.add(createStatPanel("Weight", weight + " kg"));

        // BMI stat (if height is available)
        if (height > 0) {
            double heightInMeters = height / 100.0;
            double bmi = weight / (heightInMeters * heightInMeters);
            card.add(createStatPanel("BMI", String.format("%.1f", bmi)));
        } else {
            card.add(createStatPanel("BMI", "N/A"));
        }

        return card;
    }

    private JPanel createStatPanel(String title, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SECONDARY_COLOR);
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(INFO_FONT);
        titleLabel.setForeground(TEXT_COLOR.brighter());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(VALUE_FONT);
        valueLabel.setForeground(PRIMARY_COLOR);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        panel.add(titleLabel);
        panel.add(valueLabel);

        return panel;
    }

    private String getInitials(String name) {
        String[] parts = name.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }

    // Custom rounded border for profile picture
    private static class RoundedBorder implements javax.swing.border.Border {
        private final int radius;
        private final Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(color);
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }
}