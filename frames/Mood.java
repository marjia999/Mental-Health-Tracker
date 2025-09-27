package frames;

import app.App;
import utilities.Database;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Mood extends JFrame {
    private final DefaultTableModel model;
    private final String username;
    private JSlider stressSlider;
    private String selectedMood = null;
    private int selectedStressLevel = 50;
    private JDialog historyDialog;
    private JDialog analysisDialog;

    private static final Color PRIMARY_COLOR = new Color(99, 102, 241);
    private static final Color SECONDARY_COLOR = new Color(236, 239, 241);
    private static final Color ACCENT_COLOR = new Color(239, 68, 68);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font BUTTON_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 18);

    private static final Color[] MOOD_COLORS = {
            new Color(102, 153, 204),
            new Color(255, 204, 102),
            new Color(255, 102, 102),
            new Color(102, 204, 204),
            new Color(153, 153, 153),
            new Color(221, 221, 221)
    };

    public Mood(String username) {
        this.username = username;
        setTitle(App.getTitle() + " - Mood & Stress Tracker");
        setIconImage(App.getIcon());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(SECONDARY_COLOR);
        setLayout(new BorderLayout());


        String[] columns = {"Mood", "Stress Level", "Logged At"};
        model = new DefaultTableModel(columns, 0);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(40, 60, 40, 60));
        mainPanel.setBackground(SECONDARY_COLOR);
        add(mainPanel, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("How are you feeling today?");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.setOpaque(false);
        backButtonPanel.add(createThickBackButton());
        headerPanel.add(backButtonPanel, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        contentPanel.setOpaque(false);

        contentPanel.add(createMoodPanel());
        contentPanel.add(createStressPanel());

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        controlPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        controlPanel.setOpaque(false);

        controlPanel.add(createModernButton("Save Entry", PRIMARY_COLOR, e -> saveMoodEntry()));
        controlPanel.add(createModernButton("View History", PRIMARY_COLOR, e -> showHistoryDialog()));
        controlPanel.add(createModernButton("Analysis", PRIMARY_COLOR, e -> showAnalysisDialog()));

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        setupWindow();
    }

    private void setupWindow() {
        setResizable(false);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private JButton createThickBackButton() {
        JButton backButton = new JButton("<html><span style='font-size:24px; font-weight:bold'>❮</span></html>");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        backButton.setForeground(PRIMARY_COLOR);
        backButton.setBackground(SECONDARY_COLOR);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setContentAreaFilled(false);

        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backButton.setForeground(PRIMARY_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backButton.setForeground(PRIMARY_COLOR);
            }
        });

        backButton.addActionListener(e -> this.dispose());

        return backButton;
    }

    private JPanel createMoodPanel() {
        JPanel moodPanel = new JPanel(new BorderLayout());
        moodPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 15),
                createModernBorder("Select Your Mood", PRIMARY_COLOR)
        ));
        moodPanel.setBackground(Color.WHITE);

        String[] moods = {"😢 Sad", "😊 Happy", "😠 Angry", "🤩 Excited", "😞 Depressed", "😐 Neutral"};
        String[] moodValues = {"Sad", "Happy", "Angry", "Excited", "Depressed", "Neutral"};

        JPanel moodButtonPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        moodButtonPanel.setBackground(Color.WHITE);
        moodButtonPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        for (int i = 0; i < moods.length; i++) {
            JButton btn = createMoodButton(moods[i], MOOD_COLORS[i]);

            final String mood = moodValues[i];
            btn.addActionListener(e -> {
                selectedMood = mood;
                highlightSelectedButton(btn, MOOD_COLORS);
            });

            moodButtonPanel.add(btn);
        }

        moodPanel.add(moodButtonPanel, BorderLayout.CENTER);
        return moodPanel;
    }

    private JButton createMoodButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setLayout(new BorderLayout());
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        button.setForeground(Color.BLACK);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        BorderFactory.createEmptyBorder(13, 13, 13, 13)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            }
        });

        return button;
    }

    private JPanel createStressPanel() {
        JPanel stressPanel = new JPanel(new BorderLayout());
        stressPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 0, 0),
                createModernBorder("Rate Your Stress Level", PRIMARY_COLOR)
        ));
        stressPanel.setBackground(Color.WHITE);

        JPanel stressContent = new JPanel(new BorderLayout());
        stressContent.setBackground(Color.WHITE);
        stressContent.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel labelPanel = new JPanel(new GridLayout(1, 5));
        labelPanel.setOpaque(false);
        String[] stressLabels = {"😌 Calm", "🙂 Mild", "😟 Moderate", "😤 High", "😵 Extreme"};
        for (String label : stressLabels) {
            JLabel lbl = new JLabel(label, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            labelPanel.add(lbl);
        }

        stressSlider = new JSlider(0, 100, 50);
        stressSlider.setMajorTickSpacing(25);
        stressSlider.setMinorTickSpacing(5);
        stressSlider.setPaintTicks(true);
        stressSlider.setPaintLabels(false);
        stressSlider.setBackground(Color.WHITE);
        stressSlider.setOpaque(false);

        stressSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(stressSlider) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int trackHeight = 8;
                int trackTop = (trackRect.height - trackHeight) / 2;

                g2d.setColor(new Color(230, 230, 230));
                g2d.fillRoundRect(trackRect.x, trackRect.y + trackTop,
                        trackRect.width, trackHeight, 4, 4);

                int fillWidth = thumbRect.x + (thumbRect.width / 2) - trackRect.x;

                GradientPaint gp = new GradientPaint(
                        trackRect.x, 0, new Color(76, 175, 80),
                        trackRect.x + trackRect.width, 0, ACCENT_COLOR
                );
                g2d.setPaint(gp);
                g2d.fillRoundRect(trackRect.x, trackRect.y + trackTop,
                        fillWidth, trackHeight, 4, 4);
            }

            @Override
            protected Dimension getThumbSize() {
                return new Dimension(24, 24);
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int value = slider.getValue();
                Color thumbColor;

                if (value < 25) {
                    thumbColor = new Color(76, 175, 80);
                } else if (value < 50) {
                    thumbColor = new Color(139, 195, 74);
                } else if (value < 75) {
                    thumbColor = new Color(255, 193, 7);
                } else {
                    thumbColor = ACCENT_COLOR;
                }

                g2d.setColor(thumbColor);
                g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);

                g2d.setColor(Color.WHITE);
                g2d.fillOval(thumbRect.x + 6, thumbRect.y + 6, thumbRect.width - 12, thumbRect.height - 12);
            }
        });

        stressSlider.addChangeListener(e -> {
            selectedStressLevel = stressSlider.getValue();
        });

        JLabel stressValueLabel = new JLabel("50", SwingConstants.CENTER);
        stressValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        stressValueLabel.setForeground(PRIMARY_COLOR);
        stressSlider.addChangeListener(e -> {
            stressValueLabel.setText(String.valueOf(stressSlider.getValue()));
        });

        stressContent.add(labelPanel, BorderLayout.NORTH);
        stressContent.add(stressSlider, BorderLayout.CENTER);
        stressContent.add(stressValueLabel, BorderLayout.SOUTH);
        stressPanel.add(stressContent, BorderLayout.CENTER);

        return stressPanel;
    }

    private TitledBorder createModernBorder(String title, Color color) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)
                ), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 18), color);
        border.setTitlePosition(TitledBorder.TOP);
        return border;
    }

    private JButton createModernButton(String text, Color bgColor, java.awt.event.ActionListener action) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setPreferredSize(new Dimension(200, 50));
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(bgColor.darker(), 2),
                        BorderFactory.createEmptyBorder(3, 3, 3, 3)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBorder(BorderFactory.createEmptyBorder());
            }
        });

        return button;
    }

    private void highlightSelectedButton(JButton selectedButton, Color[] moodColors) {
        Component[] components = ((JPanel)selectedButton.getParent()).getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JButton) {
                JButton btn = (JButton) components[i];
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                btn == selectedButton ? PRIMARY_COLOR : new Color(220, 220, 220),
                                btn == selectedButton ? 3 : 1
                        ),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                ));
            }
        }
    }
    //method to calculate and store daily summary
    private void calculateAndStoreDailySummary() {
        LocalDate today = LocalDate.now();

        try (Connection conn = Database.getConnection()) {
            // Calculate dominant mood and count for the day
            Map<String, Object> moodStats = getDominantMoodAndCountForDay(conn, today);
            String dominantMood = (String) moodStats.get("dominantMood");
            int moodCount = (int) moodStats.get("moodCount");

            // Calculate average stress for the day
            double averageStress = getAverageStressForDay(conn, today);

            String sql = "INSERT INTO user_daily_mood_stress (username, date, average_score, dominant_type, mood_cont) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "average_score = VALUES(average_score), " +
                    "dominant_type = VALUES(dominant_type), " +
                    "mood_cont = VALUES(mood_cont)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setDate(2, Date.valueOf(today));
                stmt.setDouble(3, averageStress);
                stmt.setString(4, dominantMood);
                stmt.setInt(5, moodCount);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error calculating daily summary: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<String, Object> getDominantMoodAndCountForDay(Connection conn, LocalDate date) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        result.put("dominantMood", "No Data");
        result.put("moodCount", 0);

        //mood counts for the day
        Map<String, Integer> moodCounts = new HashMap<>();
        String[] moods = {"Sad", "Happy", "Angry", "Excited", "Depressed", "Neutral"};

        for (String mood : moods) {
            moodCounts.put(mood, 0);
        }

        String sql = "SELECT mood, COUNT(*) as count FROM mood_logs " +
                "WHERE username = ? AND DATE(log_time) = ? " +
                "GROUP BY mood";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                moodCounts.put(rs.getString("mood"), rs.getInt("count"));
            }
        }

        // Calculate total count and find dominant mood
        int totalCount = moodCounts.values().stream().mapToInt(Integer::intValue).sum();
        int maxCount = moodCounts.values().stream().max(Integer::compare).orElse(0);
        List<String> dominantMoods = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            if (entry.getValue() == maxCount) {
                dominantMoods.add(entry.getKey());
            }
        }

        String dominantMood = dominantMoods.size() == 1 ? dominantMoods.get(0) : "Mixed Mood";

        result.put("dominantMood", dominantMood);
        result.put("moodCount", totalCount);
        return result;
    }

    private double getAverageStressForDay(Connection conn, LocalDate date) throws SQLException {
        String sql = "SELECT AVG(stress_level) as avg_stress FROM mood_logs " +
                "WHERE username = ? AND DATE(log_time) = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_stress");
            }
        }

        return 0.0;
    }

    private void saveMoodEntry() {
        if (selectedMood == null) {
            JOptionPane.showMessageDialog(this, "Please select a mood first!",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Save the mood entry
        logMoodAndStress(selectedMood, selectedStressLevel);

        // Update daily summary
        calculateAndStoreDailySummary();

        refreshAllViews();
    }

    private void refreshAllViews() {
        if (historyDialog != null && historyDialog.isVisible()) {
            loadMoodLogs();
            loadDailySummaries();
        }

        if (analysisDialog != null && analysisDialog.isVisible()) {
            refreshAnalysisCharts();
        }
    }

    private void showHistoryDialog() {
        if (historyDialog == null) {
            historyDialog = new JDialog(this, "Mood & Stress History - " + username, false);
            historyDialog.setIconImage(App.getIcon());

            historyDialog.setSize(this.getSize());
            historyDialog.setLocationRelativeTo(this);
            historyDialog.getContentPane().setBackground(SECONDARY_COLOR);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setBackground(SECONDARY_COLOR);
            tabbedPane.setForeground(PRIMARY_COLOR);
            tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

            tabbedPane.addTab("Detailed Logs", createDetailedLogsPanel());

            tabbedPane.addTab("Daily Summary", createDailySummaryPanel());

            JButton closeButton = createModernButton("Close", PRIMARY_COLOR, e -> historyDialog.setVisible(false));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.add(closeButton);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
            mainPanel.add(tabbedPane, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            historyDialog.add(mainPanel);
        }

        loadMoodLogs();
        loadDailySummaries();
        historyDialog.setVisible(true);
    }
    private JPanel createDetailedLogsPanel() {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }

                c.setForeground(new Color(64, 64, 64));

                String mood = getValueAt(row, 0).toString();
                int stress = column == 1 ? Integer.parseInt(getValueAt(row, 1).toString()) : 0;

                if (mood.equals("Depressed") || mood.equals("Sad") ||
                        (column == 1 && stress > 80)) {
                    c.setForeground(ACCENT_COLOR);
                }

                return c;
            }
        };

        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(Color.WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        table.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    //daily summary table model
    private DefaultTableModel dailySummaryModel;

    private JPanel createDailySummaryPanel() {
        String[] summaryColumns = {"Date", "Dominant Mood", "Average Stress", "Entries"};
        dailySummaryModel = new DefaultTableModel(summaryColumns, 0);

        JTable summaryTable = new JTable(dailySummaryModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }

                c.setForeground(new Color(64, 64, 64));

                String mood = column == 1 ? getValueAt(row, 1).toString() : "";
                double stress = column == 2 ? Double.parseDouble(getValueAt(row, 2).toString()) : 0;

                if (mood.equals("Depressed") || mood.equals("Sad") ||
                        (column == 2 && stress > 80)) {
                    c.setForeground(ACCENT_COLOR);
                }

                return c;
            }
        };
        summaryTable.setRowHeight(40);
        summaryTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        summaryTable.setBackground(Color.WHITE);
        summaryTable.setShowGrid(false);
        summaryTable.setIntercellSpacing(new Dimension(0, 0));
        summaryTable.getTableHeader().setBackground(PRIMARY_COLOR);
        summaryTable.getTableHeader().setForeground(Color.WHITE);
        summaryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        summaryTable.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(summaryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadDailySummaries() {
        dailySummaryModel.setRowCount(0);
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, dominant_type, average_score, mood_cont " +
                             "FROM user_daily_mood_stress " +
                             "WHERE username = ? " +
                             "ORDER BY date DESC")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getDate("date").toString());
                row.add(rs.getString("dominant_type"));
                row.add(String.format("%.1f", rs.getDouble("average_score")));
                row.add(rs.getInt("mood_cont"));
                dailySummaryModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading daily summaries: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showAnalysisDialog() {
        if (analysisDialog == null) {
            analysisDialog = new JDialog(this, "Mood & Stress Analysis - " + username, false);
            analysisDialog.setIconImage(App.getIcon());
            analysisDialog.setBounds(this.getBounds());
            analysisDialog.setResizable(true);
            analysisDialog.getContentPane().setBackground(SECONDARY_COLOR);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(new EmptyBorder(20, 40, 40, 40));
            mainPanel.setBackground(SECONDARY_COLOR);

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
            headerPanel.setBackground(SECONDARY_COLOR);

            JLabel titleLabel = new JLabel("Mood & Stress Analysis");
            titleLabel.setFont(TITLE_FONT);
            titleLabel.setForeground(PRIMARY_COLOR);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel chartsPanel = new JPanel();
            chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
            chartsPanel.setBackground(SECONDARY_COLOR);

            // Add daily chart
            JPanel dailyChartCard = createCombinedChartCard();
            chartsPanel.add(dailyChartCard);
            chartsPanel.add(Box.createRigidArea(new Dimension(0, 30)));

            // Add weekly chart
            JPanel weeklyChartCard = createWeeklyChartCard();
            chartsPanel.add(weeklyChartCard);

            JScrollPane scrollPane = new JScrollPane(chartsPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(SECONDARY_COLOR);

            JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            closePanel.setOpaque(false);
            closePanel.setBorder(new EmptyBorder(20, 0, 0, 0));

            JButton closeButton = createModernButton("Close", PRIMARY_COLOR, e -> analysisDialog.dispose());
            closePanel.add(closeButton);

            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(closePanel, BorderLayout.SOUTH);
            analysisDialog.add(mainPanel);
        }

        refreshAnalysisCharts();
        analysisDialog.setVisible(true);
    }

    private JPanel createWeeklyChartCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(12, new Color(220, 220, 220)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(900, 500));

        JLabel titleLabel = new JLabel("Weekly Stress Trend VS Dominant Moods");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int margin = 50;
                int chartWidth = getWidth() - 2 * margin;
                int chartHeight = getHeight() - 2 * margin;

                g2.setColor(Color.WHITE);
                g2.fillRect(margin, margin, chartWidth, chartHeight);

                drawWeeklyTrendChart(g2, margin, chartWidth, chartHeight);
                g2.dispose();
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(800, 400));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCombinedChartCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(12, new Color(220, 220, 220)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(900, 500));

        JLabel titleLabel = new JLabel("Mood Frequency VS Stress Levels");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCombinedChart(g, getWidth(), getHeight());
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(800, 400));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }

    private void drawCombinedChart(Graphics g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int margin = 50;
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin;

        g2.setColor(Color.WHITE);
        g2.fillRect(margin, margin, chartWidth, chartHeight);

        g2.setColor(new Color(150, 150, 150));
        g2.drawLine(margin, margin + chartHeight, margin + chartWidth, margin + chartHeight);
        g2.drawLine(margin, margin, margin, margin + chartHeight);

        drawDailyCombinedChart(g2, margin, chartWidth, chartHeight);

        g2.dispose();
    }

    private void drawDailyCombinedChart(Graphics2D g2, int margin, int chartWidth, int chartHeight) {
        Map<String, Integer> moodCounts = getDailyMoodCounts();
        Map<String, Double> moodStressAverages = getDailyMoodStressAverages();

        if (moodCounts.isEmpty() || moodStressAverages.isEmpty()) {
            drawNoDataMessage(g2, chartWidth + 2 * margin, chartHeight + 2 * margin);
            return;
        }

        String[] moods = {"Sad", "Happy", "Angry", "Excited", "Depressed", "Neutral"};
        int numBars = moods.length;
        int barWidth = chartWidth / (numBars * 2);
        int spacing = barWidth / 2;
        int xPos = margin + spacing;

        // Find maximum values for scaling
        int maxCount = moodCounts.values().stream().max(Integer::compare).orElse(10);
        double maxStress = moodStressAverages.values().stream().max(Double::compare).orElse(100.0);

        // Draw bars for mood counts
        for (int i = 0; i < moods.length; i++) {
            String mood = moods[i];
            int count = moodCounts.getOrDefault(mood, 0);
            int barHeight = (int)((count / (double)maxCount) * (chartHeight - 30));

            g2.setColor(MOOD_COLORS[i]);
            g2.fillRect(xPos, margin + chartHeight - barHeight, barWidth, barHeight);

            // Draw count label
            g2.setColor(new Color(80, 80, 80));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String countLabel = String.valueOf(count);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(countLabel, xPos + (barWidth - fm.stringWidth(countLabel))/2,
                    margin + chartHeight - barHeight - 5);

            xPos += barWidth + spacing;
        }

        //line for stress levels
        xPos = margin + spacing + barWidth/2;
        int prevX = xPos;
        int prevY = margin + chartHeight - (int)((moodStressAverages.getOrDefault(moods[0], 0.0) / maxStress) * (chartHeight - 30));

        g2.setColor(PRIMARY_COLOR);
        g2.setStroke(new BasicStroke(2.5f));
        for (int i = 1; i < moods.length; i++) {
            xPos += barWidth + spacing;
            int y = margin + chartHeight - (int)((moodStressAverages.getOrDefault(moods[i], 0.0) / maxStress) * (chartHeight - 30));
            g2.drawLine(prevX, prevY, xPos, y);

            //stress value point
            g2.setColor(ACCENT_COLOR);
            g2.fillOval(xPos - 4, y - 4, 8, 8);
            g2.setColor(PRIMARY_COLOR);

            prevX = xPos;
            prevY = y;
        }

        //mood labels
        xPos = margin + spacing;
        g2.setColor(new Color(100, 100, 100));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (String mood : moods) {
            String moodLabel = mood;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(moodLabel, xPos + (barWidth - fm.stringWidth(moodLabel))/2,
                    margin + chartHeight + 15);
            xPos += barWidth + spacing;
        }

        // Draw Y-axis labels for mood counts left
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(100, 100, 100));
        for (int j = 0; j <= maxCount; j += (maxCount > 10 ? 2 : 1)) {
            int yPos = margin + chartHeight - (int)((j / (double)maxCount) * (chartHeight - 30));
            g2.drawString(String.valueOf(j), margin - 25, yPos + 5);
        }

        // Draw Y-axis labels for stress levels right
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(PRIMARY_COLOR);
        for (int j = 0; j <= maxStress; j += (maxStress > 50 ? 20 : 10)) {
            int yPos = margin + chartHeight - (int)((j / maxStress) * (chartHeight - 30));
            g2.drawString(String.valueOf(j), margin + chartWidth + 5, yPos + 5);
        }

        int legendX = margin + chartWidth - 150;
        int legendY = margin + 20;
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("Legend", legendX, legendY);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.drawString("Mood Count", legendX, legendY + 20);
        g2.setColor(PRIMARY_COLOR);
        g2.drawString("Stress Level", legendX, legendY + 40);

    }
    private Map<LocalDate, Double> getWeeklyStressAverages() {
        Map<LocalDate, Double> result = new LinkedHashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, average_score FROM user_daily_mood_stress " +
                             "WHERE username = ? AND date BETWEEN ? AND ? " +
                             "ORDER BY date")) {

            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getDate("date").toLocalDate(), rs.getDouble("average_score"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            result.putIfAbsent(date, 0.0);
        }

        return result;
    }

    private Map<LocalDate, String> getWeeklyDominantMoods() {
        Map<LocalDate, String> result = new LinkedHashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, dominant_type FROM user_daily_mood_stress " +
                             "WHERE username = ? AND date BETWEEN ? AND ? " +
                             "ORDER BY date")) {

            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getDate("date").toLocalDate(), rs.getString("dominant_type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            result.putIfAbsent(date, "No Data");
        }

        return result;
    }
    private void drawWeeklyTrendChart(Graphics2D g2, int margin, int chartWidth, int chartHeight) {
        g2.setColor(Color.WHITE);
        g2.fillRect(margin, margin, chartWidth, chartHeight);

        Map<LocalDate, Double> stressAverages = getWeeklyStressAverages();
        Map<LocalDate, String> dominantMoods = getWeeklyDominantMoods();

        if (stressAverages.isEmpty() || dominantMoods.isEmpty()) {
            drawNoDataMessage(g2, chartWidth + 2 * margin, chartHeight + 2 * margin);
            return;
        }

        // Find maximum stress for scaling
        double maxStress = stressAverages.values().stream().max(Double::compare).orElse(100.0);
        if (maxStress == 0) maxStress = 100; // Prevent division by zero

        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(1));

        for (int i = 0; i <= 5; i++) {
            int y = margin + (int)(chartHeight * (1 - (i * 0.2)));
            g2.drawLine(margin, y, margin + chartWidth, y);

            g2.setColor(new Color(100, 100, 100));
            g2.drawString(String.valueOf((int)(i * 20)), margin - 25, y + 5);
            g2.setColor(new Color(220, 220, 220));
        }

        // Vertical grid lines and date labels
        int dayWidth = chartWidth / 7;
        int xPos = margin;

        List<LocalDate> dates = new ArrayList<>(stressAverages.keySet());
        dates.sort(LocalDate::compareTo);

        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = dates.get(i);
            xPos = margin + (int)(dayWidth * (i + 0.5));

            // Draw vertical grid line
            g2.setColor(new Color(220, 220, 220));
            g2.drawLine(xPos, margin, xPos, margin + chartHeight);

            // Draw date label
            g2.setColor(new Color(100, 100, 100));
            String dayLabel = date.getDayOfWeek().toString().substring(0, 3) + "\n" + date.getDayOfMonth();
            String[] lines = dayLabel.split("\n");
            for (int j = 0; j < lines.length; j++) {
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lines[j], xPos - fm.stringWidth(lines[j])/2,
                        margin + chartHeight + 15 + j * 15);
            }
        }

        // Draw stress trend line
        g2.setColor(PRIMARY_COLOR);
        g2.setStroke(new BasicStroke(2.5f));

        int prevX = -1;
        int prevY = -1;

        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = dates.get(i);
            double stress = stressAverages.get(date);
            int x = margin + (int)(dayWidth * (i + 0.5));
            int y = margin + (int)(chartHeight * (1 - (stress / maxStress)));

            // Draw data point
            g2.setColor(ACCENT_COLOR);
            g2.fillOval(x - 4, y - 4, 8, 8);

            // Draw line connecting to previous point
            if (prevX != -1) {
                g2.setColor(PRIMARY_COLOR);
                g2.drawLine(prevX, prevY, x, y);
            }

            // Draw mood icon
            String mood = dominantMoods.get(date);
            if (!mood.equals("No Data")) {
                String emoji = getMoodEmoji(mood);
                if (emoji != null) {
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(emoji, x - fm.stringWidth(emoji)/2, y - 15);
                }
            }

            // Draw stress value
            g2.setColor(new Color(100, 100, 100));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String stressLabel = String.format("%.0f", stress);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(stressLabel, x - fm.stringWidth(stressLabel)/2, y - 30);

            prevX = x;
            prevY = y;
        }

    }

    private String getMoodEmoji(String mood) {
        switch (mood) {
            case "Sad": return "😢";
            case "Happy": return "😊";
            case "Angry": return "😠";
            case "Excited": return "🤩";
            case "Depressed": return "😞";
            case "Neutral": return "😐";
            case "Mixed Mood": return "🤔";
            default: return null;
        }
    }


    private Map<String, Integer> getDailyMoodCounts() {
        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT mood, COUNT(*) as mood_count " +
                             "FROM mood_logs WHERE username = ? AND DATE(log_time) = ? " +
                             "GROUP BY mood")) {

            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(today));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("mood"), rs.getInt("mood_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String[] moods = {"Sad", "Happy", "Angry", "Excited", "Depressed", "Neutral"};
        for (String mood : moods) {
            result.putIfAbsent(mood, 0);
        }

        return result;
    }

    private Map<String, Double> getDailyMoodStressAverages() {
        Map<String, Double> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT mood, AVG(stress_level) as avg_stress " +
                             "FROM mood_logs WHERE username = ? AND DATE(log_time) = ? " +
                             "GROUP BY mood")) {

            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(today));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("mood"), rs.getDouble("avg_stress"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String[] moods = {"Sad", "Happy", "Angry", "Excited", "Depressed", "Neutral"};
        for (String mood : moods) {
            result.putIfAbsent(mood, 0.0);
        }

        return result;
    }

    private void drawNoDataMessage(Graphics g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int iconSize = 60;
        int iconX = (width - iconSize) / 2;
        int iconY = (height - iconSize) / 2 - 20;

        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(iconX, iconY, iconSize, iconSize);
        g2.drawLine(iconX + iconSize/4, iconY + iconSize/4,
                iconX + 3*iconSize/4, iconY + 3*iconSize/4);

        g2.setColor(new Color(150, 150, 150));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        String message = "No data available";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(message, (width - fm.stringWidth(message)) / 2, iconY + iconSize + 30);

        g2.dispose();
    }

    private void refreshAnalysisCharts() {
        if (analysisDialog != null) {
            for (Component comp : analysisDialog.getContentPane().getComponents()) {
                if (comp instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) comp;
                    Component view = scrollPane.getViewport().getView();
                    if (view instanceof JPanel) {
                        view.revalidate();
                        view.repaint();
                    }
                }
            }
        }
    }

    private void loadMoodLogs() {
        model.setRowCount(0);
        try {
            ResultSet rs = Database.executeQuery(
                    "SELECT mood, stress_level, log_time FROM mood_logs WHERE username = '" + username + "' ORDER BY log_time DESC");
            while (rs != null && rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("mood"));
                row.add(rs.getInt("stress_level"));
                row.add(rs.getTimestamp("log_time").toString());
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading mood/stress data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void logMoodAndStress(String mood, int stressLevel) {
        try {
            String sql = String.format(
                    "INSERT INTO mood_logs (username, mood, stress_level, log_time) VALUES ('%s', '%s', %d, '%s')",
                    username, mood, stressLevel, Timestamp.valueOf(LocalDateTime.now()).toString()
            );
            Database.executeUpdate(sql);

            JOptionPane.showMessageDialog(this,
                    String.format("<html><div style='text-align: center;'><h3>Entry Saved!</h3>" +
                                    "<p>Mood: <b>%s</b></p>" +
                                    "<p>Stress Level: <b>%d</b></p></div></html>",
                            mood, stressLevel),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadMoodLogs();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving mood/stress: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static class RoundBorder extends AbstractBorder {
        private int radius;
        private Color color;

        public RoundBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.bottom = insets.top = radius;
            return insets;
        }
    }
}