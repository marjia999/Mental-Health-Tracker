package frames;

import utilities.Database;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

public class DailyAssessment extends JFrame {

    private static final Color PRIMARY_COLOR = new Color(99, 102, 241); // Indigo
    private static final Color SECONDARY_COLOR = new Color(236, 239, 241); // Light gray
    private static final Color CARD_COLOR = new Color(255, 255, 255); // White
    private static final Color TEXT_COLOR = new Color(31, 41, 55); // Dark gray
    private static final Color BORDER_COLOR = new Color(229, 231, 235); // Light gray border

    // Sentiment Colors
    private static final Color VERY_NEGATIVE = new Color(220, 38, 38); // Red
    private static final Color NEGATIVE = new Color(245, 158, 11); // Amber
    private static final Color NEUTRAL = new Color(156, 163, 175); // Gray
    private static final Color POSITIVE = new Color(16, 185, 129); // Emerald
    private static final Color VERY_POSITIVE = new Color(5, 150, 105); // Darker Emerald

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 18);
    private static final Font ENTRY_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font SENTIMENT_FONT = new Font("Segoe UI", Font.BOLD, 14);


    private static final int QUESTIONS_PER_DAY = 5;

    private final String username;
    private JPanel mainPanel;
    private JDialog assessmentDialog;
    private JDialog historyDialog;
    private JPanel historyEntriesPanel;

    public DailyAssessment(String username) {
        this.username = username;
        setIconImage(app.App.getIcon()); // Set the frame icon
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Daily Assessment - " + username);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(SECONDARY_COLOR);
        setLayout(new BorderLayout());

        // Header Panel (simplified without back button)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Daily Assessment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel (unchanged)
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 40, 40, 40));
        mainPanel.setBackground(Color.WHITE);

        // Check if assessment is already completed (unchanged)
        try {
            if (hasCompletedToday(username)) {
                showCompletionMessage();
            } else {
                showStartAssessmentButton();
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }

        add(mainPanel, BorderLayout.CENTER);

        // Add Close button at the bottom
        JButton closeButton = createModernButton("Close", PRIMARY_COLOR);
        closeButton.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JButton createModernButton(String text, Color bgColor) {
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

        button.setPreferredSize(new Dimension(180, 45));
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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

    private void showStartAssessmentButton() {
        mainPanel.removeAll();

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        contentPanel.setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel("<html><div style='text-align: center;'>" +
                "<h2 style='color: #6366f1;'>Daily Self-Assessment</h2>" +
                "<p style='font-size: 16px; color: #555;'>Complete your daily assessment to track your progress.<br>" +
                "You'll be asked " + QUESTIONS_PER_DAY + " questions about your current state.</p></div></html>");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(infoLabel, BorderLayout.CENTER);

        JButton startButton = createModernButton("Start Assessment", PRIMARY_COLOR);
        startButton.addActionListener(e -> startAssessment());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(startButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showCompletionMessage() {
        mainPanel.removeAll();

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        contentPanel.setBackground(Color.WHITE);

        JLabel completedLabel = new JLabel("<html><div style='text-align: center;'>" +
                "<h2 style='color: #6366f1;'>Assessment Completed</h2>" +
                "<p style='font-size: 16px; color: #555;'>You've completed today's assessment.</p>" +

                "</div></html>");
        completedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(completedLabel, BorderLayout.CENTER);

        // Button panel with three buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton historyButton = createModernButton("View History", PRIMARY_COLOR);
        historyButton.addActionListener(e -> showHistory());

        JButton analysisButton = createModernButton("Analysis", PRIMARY_COLOR);
        analysisButton.addActionListener(e -> showAnalysisDialog());

        JButton anotherAssessmentButton = createModernButton("Take Another", PRIMARY_COLOR);
        anotherAssessmentButton.addActionListener(e -> {
            try {
                if (hasMoreQuestionsAvailable(username)) {
                    startAssessment();
                } else {
                    showError("No more unique questions available for today.");
                }
            } catch (SQLException ex) {
                showError("Database error: " + ex.getMessage());
            }
        });

        buttonPanel.add(historyButton);
        buttonPanel.add(analysisButton);
        buttonPanel.add(anotherAssessmentButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private boolean hasMoreQuestionsAvailable(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM assessment_questions " +
                "WHERE id NOT IN (SELECT question_id FROM user_asked_questions WHERE username = ? AND asked_date = CURDATE())";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) >= QUESTIONS_PER_DAY;
        }
    }

    private SentimentResult getTodaySentiment(String username) {
        String sql = "SELECT average_score, dominant_type FROM user_daily_sentiment " +
                "WHERE username = ? AND date = CURDATE()";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new SentimentResult(
                        (int) Math.round(rs.getDouble("average_score")),
                        rs.getString("dominant_type")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new SentimentResult(2, "Neutral"); // Default if not found
    }

    private void startAssessment() {
        try {
            List<Question> questions = getRandomQuestions(username);

            if (questions.isEmpty()) {
                showError("No questions available for assessment.");
                return;
            }

            showAssessmentDialog(questions);
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void showAssessmentDialog(List<Question> questions) {
        assessmentDialog = new JDialog(this, "Daily Assessment", true);
        // Set the size to match the history window (which matches the main window)
        assessmentDialog.setSize(this.getSize()); // Use the same size as the parent frame
        assessmentDialog.setLocationRelativeTo(this);
        assessmentDialog.getContentPane().setBackground(SECONDARY_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        JPanel questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        questionsPanel.setBackground(Color.WHITE);

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Create question components
        List<ButtonGroup> buttonGroups = new ArrayList<>();

        for (Question question : questions) {
            JPanel questionPanel = new JPanel(new BorderLayout());
            questionPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                    BorderFactory.createEmptyBorder(10, 5, 15, 5)
            ));
            questionPanel.setBackground(Color.WHITE);
            questionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            JLabel questionLabel = new JLabel("<html><div style='font-size: 16px; color: #333;'>" +
                    question.getText() + "</div></html>");
            questionPanel.add(questionLabel, BorderLayout.NORTH);

            // Create sentiment options panel with new labels
            JPanel optionsPanel = new JPanel(new GridLayout(1, 5, 10, 0));
            optionsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
            optionsPanel.setBackground(Color.WHITE);

            ButtonGroup group = new ButtonGroup();
            buttonGroups.add(group);

            // Updated sentiment options
            String[] sentimentLabels = {"Strongly disagree", "Disagree", "Neutral", "Agree", "Strongly agree"};
            String[] sentimentValues = {"veryNegative", "negative", "neutral", "positive", "veryPositive"};
            int[] sentimentScores = {0, 1, 2, 3, 4};
            Color[] sentimentColors = {VERY_NEGATIVE, NEGATIVE, NEUTRAL, POSITIVE, VERY_POSITIVE};

            for (int i = 0; i < 5; i++) {
                JToggleButton button = new JToggleButton(sentimentLabels[i]);
                button.setActionCommand(sentimentValues[i] + ":" + sentimentScores[i]);
                button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                button.setBackground(Color.WHITE);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(sentimentColors[i], 1),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                button.setFocusPainted(false);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                // Change button appearance when selected
                int finalI = i;
                button.addItemListener(e -> {
                    if (button.isSelected()) {
                        button.setBackground(sentimentColors[finalI]);
                        button.setForeground(Color.WHITE);
                    } else {
                        button.setBackground(Color.WHITE);
                        button.setForeground(TEXT_COLOR);
                    }
                });

                group.add(button);
                optionsPanel.add(button);
            }

            questionPanel.add(optionsPanel, BorderLayout.CENTER);
            questionsPanel.add(questionPanel);
            questionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Submit button
        JButton submitButton = createModernButton("Submit Assessment", PRIMARY_COLOR);
        submitButton.addActionListener(e -> {
            Map<Integer, SentimentResult> responses = new HashMap<>();

            // Collect responses
            for (int i = 0; i < questions.size(); i++) {
                ButtonModel selectedModel = buttonGroups.get(i).getSelection();
                if (selectedModel != null) {
                    String[] parts = selectedModel.getActionCommand().split(":");
                    String sentimentType = parts[0];
                    int sentimentScore = Integer.parseInt(parts[1]);
                    responses.put(questions.get(i).getId(), new SentimentResult(sentimentScore, sentimentType));
                }
            }

            if (responses.isEmpty()) {
                showError("Please answer at least one question to submit.");
                return;
            }

            processAssessment(responses);
            assessmentDialog.dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        assessmentDialog.add(mainPanel);
        assessmentDialog.setVisible(true);
    }

    private void processAssessment(Map<Integer, SentimentResult> responses) {
        try {
            saveResponses(username, responses);
            markQuestionsAsAsked(username, responses.keySet());

            // Calculate overall sentiment
            SentimentResult overallSentiment = calculateOverallSentiment(responses.values());

            // Save daily summary
            saveDailySentiment(username, overallSentiment);

            // Show completion message with sentiment
            mainPanel.removeAll();
            showCompletionMessage();

            showSubmissionSuccessDialog(overallSentiment);

        } catch (SQLException e) {
            showError("Error saving assessment: " + e.getMessage());
        }
    }

    private void saveDailySentiment(String username, SentimentResult overallSentiment) throws SQLException {
        String sql = "INSERT INTO user_daily_sentiment (username, date, average_score, dominant_type, average_type, assessment_count) " +
                "VALUES (?, CURDATE(), ?, ?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE " +
                "average_score = (average_score * assessment_count + ?) / (assessment_count + 1), " +
                "dominant_type = ?, " +
                "average_type = ?, " +
                "assessment_count = assessment_count + 1";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setDouble(2, overallSentiment.score);
            stmt.setString(3, overallSentiment.type); // dominant type
            stmt.setString(4, getSentimentType(overallSentiment.score)); // average type
            stmt.setDouble(5, overallSentiment.score);
            stmt.setString(6, overallSentiment.type); // dominant type
            stmt.setString(7, getSentimentType(overallSentiment.score)); // average type
            stmt.executeUpdate();
        }
    }
    private SentimentResult calculateOverallSentiment(Collection<SentimentResult> sentiments) {
        if (sentiments.isEmpty()) {
            return new SentimentResult(2, "neutral"); // Default neutral if no responses
        }

        double total = 0;
        int count = 0;

        for (SentimentResult sentiment : sentiments) {
            total += sentiment.score;
            count++;
        }

        int averageScore = (int) Math.round(total / count);
        String type = getSentimentType(averageScore);

        return new SentimentResult(averageScore, type);
    }

    private String getSentimentType(int score) {
        switch (score) {
            case 0: return "veryNegative";
            case 1: return "negative";
            case 2: return "neutral";
            case 3: return "positive";
            case 4: return "veryPositive";
            default: return "neutral";
        }
    }

    private void showSubmissionSuccessDialog(SentimentResult overallSentiment) {
        String sentimentText = "<div style='margin-top:15px;text-align:center;'>" +
                "<div style='font-weight:bold;color:" + getSentimentHexColor(overallSentiment.score) + ";'>" +
                "Overall Sentiment: " + getFormattedSentimentType(overallSentiment.type) + " (" + overallSentiment.score + "/4)" +
                "</div></div>";

        JLabel message = new JLabel("<html><div style='text-align:center;width:300px;'>" +
                "<h3 style='color:#6366f1;margin-bottom:10px;'>Assessment Submitted</h3>" +
                "<p>Your responses have been recorded.</p>" +
                sentimentText +
                "</div></html>");

        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getFormattedSentimentType(String type) {
        switch (type) {
            case "veryNegative": return "Very Negative";
            case "negative": return "Negative";
            case "neutral": return "Neutral";
            case "positive": return "Positive";
            case "veryPositive": return "Very Positive";
            default: return "Neutral";
        }
    }

    private void showHistory() {
        if (historyDialog == null) {
            historyDialog = new JDialog(this, "Assessment History", false);
            // Set the size to match the main assessment window
            historyDialog.setSize(this.getSize()); // Use the same size as the parent frame
            historyDialog.setLocationRelativeTo(this);
            historyDialog.getContentPane().setBackground(SECONDARY_COLOR);

            // Create main panel with padding
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
            mainPanel.setBackground(SECONDARY_COLOR);

            // Title
            JLabel titleLabel = new JLabel("Assessment History");
            titleLabel.setFont(TITLE_FONT);
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

            // Entries panel
            historyEntriesPanel = new JPanel();
            historyEntriesPanel.setLayout(new BoxLayout(historyEntriesPanel, BoxLayout.Y_AXIS));
            historyEntriesPanel.setBackground(SECONDARY_COLOR);

            // Scroll pane
            JScrollPane scrollPane = new JScrollPane(historyEntriesPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(SECONDARY_COLOR);

            // Close button
            JButton closeButton = createModernButton("Close", PRIMARY_COLOR);
            closeButton.addActionListener(e -> historyDialog.setVisible(false));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.add(closeButton);

            // Add components
            mainPanel.add(titleLabel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            historyDialog.add(mainPanel);
        }

        loadAssessmentHistory();
        historyDialog.setVisible(true);
    }

    private void loadAssessmentHistory() {
        historyEntriesPanel.removeAll();
        historyEntriesPanel.setLayout(new BorderLayout());

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, average_score, dominant_type, average_type, assessment_count " +
                             "FROM user_daily_sentiment " +
                             "WHERE username = ? " +
                             "ORDER BY date DESC")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            // Create table model
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 1) return Integer.class; // For average score
                    if (columnIndex == 4) return Integer.class; // For assessment count
                    return String.class;
                }
            };

            model.addColumn("Date");
            model.addColumn("Avg Score");
            model.addColumn("Dominant Type");
            model.addColumn("Avg Type");
            model.addColumn("Assessments Taken");

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
            boolean hasEntries = false;

            while (rs.next()) {
                hasEntries = true;
                Date date = rs.getDate("date");
                String formattedDate = dateFormat.format(date);
                int score = (int) Math.round(rs.getDouble("average_score"));
                String dominantType = getFormattedSentimentType(rs.getString("dominant_type"));
                String avgType = getFormattedSentimentType(rs.getString("average_type"));
                int count = rs.getInt("assessment_count");

                model.addRow(new Object[]{formattedDate, score, dominantType, avgType, count});
            }

            if (hasEntries) {
                JTable table = new JTable(model);
                table.setRowHeight(35);
                table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
                table.setShowGrid(false);
                table.setIntercellSpacing(new Dimension(0, 0));

                // Custom renderer for score column
                table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        int score = (int) value;
                        c.setForeground(getSentimentColor(score));
                        setHorizontalAlignment(CENTER);
                        return c;
                    }
                });

                // Custom renderer for dominant type column
                table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        String type = (String) value;
                        int score = getScoreFromType(type);
                        c.setForeground(getSentimentColor(score));
                        setHorizontalAlignment(CENTER);
                        return c;
                    }
                });

                // Custom renderer for average type column
                table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        String type = (String) value;
                        int score = getScoreFromType(type);
                        c.setForeground(getSentimentColor(score));
                        setHorizontalAlignment(CENTER);
                        return c;
                    }
                });

                // Custom renderer for count column
                table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        setHorizontalAlignment(CENTER);
                        return c;
                    }
                });

                // Center align all columns
                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(JLabel.CENTER);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                }

                // Set column widths
                table.getColumnModel().getColumn(0).setPreferredWidth(150); // Date
                table.getColumnModel().getColumn(1).setPreferredWidth(100); // Avg Score
                table.getColumnModel().getColumn(2).setPreferredWidth(150); // Dominant Type
                table.getColumnModel().getColumn(3).setPreferredWidth(150); // Avg Type
                table.getColumnModel().getColumn(4).setPreferredWidth(150); // Assessments Taken

                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                historyEntriesPanel.add(scrollPane, BorderLayout.CENTER);
            } else {
                JLabel noEntries = new JLabel("<html><div style='text-align: center; color: #666;'>" +
                        "No assessment history found.</div></html>");
                noEntries.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                noEntries.setHorizontalAlignment(SwingConstants.CENTER);
                historyEntriesPanel.add(noEntries, BorderLayout.CENTER);
            }
        } catch (SQLException e) {
            showError("Error loading history: " + e.getMessage());
        }

        historyEntriesPanel.revalidate();
        historyEntriesPanel.repaint();
    }

    // Helper method to get score from formatted type
    private int getScoreFromType(String formattedType) {
        switch (formattedType) {
            case "Very Negative": return 0;
            case "Negative": return 1;
            case "Neutral": return 2;
            case "Positive": return 3;
            case "Very Positive": return 4;
            default: return 2;
        }
    }

    private Color getSentimentColor(int score) {
        switch (score) {
            case 0: return VERY_NEGATIVE;
            case 1: return NEGATIVE;
            case 2: return NEUTRAL;
            case 3: return POSITIVE;
            case 4: return VERY_POSITIVE;
            default: return NEUTRAL;
        }
    }

    private String getSentimentHexColor(int score) {
        switch (score) {
            case 0: return "#dc2626";
            case 1: return "#f59e0b";
            case 2: return "#9ca3af";
            case 3: return "#10b981";
            case 4: return "#059669";
            default: return "#9ca3af";
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void addAnalysisButton(JPanel buttonPanel) {
        JButton analysisButton = createModernButton("Analysis", PRIMARY_COLOR);
        analysisButton.addActionListener(e -> showAnalysisDialog());
        buttonPanel.add(analysisButton);
    }

    // Modified showAnalysisDialog to use our custom charts
    private void showAnalysisDialog() {
        JDialog analysisDialog = new JDialog(this, "Assessment Analysis", true);
        // Set the size to match the history window (which matches the main window)
        analysisDialog.setSize(this.getSize());
        analysisDialog.setLocationRelativeTo(this);
        analysisDialog.getContentPane().setBackground(SECONDARY_COLOR);
        analysisDialog.setLayout(new BorderLayout());

        // Create main panel with padding to match history dialog
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(SECONDARY_COLOR);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Daily Analysis Tab - Using Radar Chart
        JPanel dailyPanel = new JPanel(new BorderLayout());
        dailyPanel.add(new RadarChartPanel(username), BorderLayout.CENTER);
        tabbedPane.addTab("Daily Analysis", dailyPanel);

        // Weekly Analysis Tab - Using Line Chart
        JPanel weeklyPanel = new JPanel(new BorderLayout());
        weeklyPanel.add(new LineChartPanel(username), BorderLayout.CENTER);
        tabbedPane.addTab("Weekly Trends", weeklyPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Close button
        JButton closeButton = createModernButton("Close", PRIMARY_COLOR);
        closeButton.addActionListener(e -> analysisDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        analysisDialog.add(mainPanel);
        analysisDialog.setVisible(true);
    }

    // Custom Radar Chart Panel for Daily Analysis

    class RadarChartPanel extends JPanel {
        private final String username;
        private Map<String, Integer> sentimentCounts;
        private final String[] sentiments = {"Very Negative", "Negative", "Neutral", "Positive", "Very Positive"};
        private final Color[] sentimentColors = {VERY_NEGATIVE, NEGATIVE, NEUTRAL, POSITIVE, VERY_POSITIVE};
        private final Font chartFont = new Font("Segoe UI", Font.PLAIN, 12);
        private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);

        public RadarChartPanel(String username) {
            this.username = username;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            loadDailyData();
        }

        private void loadDailyData() {
            sentimentCounts = new HashMap<>();
            for (String sentiment : sentiments) {
                sentimentCounts.put(sentiment, 0);
            }

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT selected_sentiment, COUNT(*) as count " +
                                 "FROM user_assessment_responses " +
                                 "WHERE username = ? AND DATE(response_time) = CURDATE() " +
                                 "GROUP BY selected_sentiment")) {

                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String sentiment = getFormattedSentimentType(rs.getString("selected_sentiment"));
                    int count = rs.getInt("count");
                    sentimentCounts.put(sentiment, count);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            int radius = Math.min(width, height) / 2 - 80; // Increased margin for labels

            // Draw title
            g2.setFont(titleFont);
            g2.setColor(TEXT_COLOR);
            String title = "Today's Sentiment Distribution";
            int titleWidth = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (width - titleWidth) / 2, 17);

            // Find maximum count for scaling
            int maxCount = Collections.max(sentimentCounts.values());
            if (maxCount == 0) {
                maxCount = 1; // Avoid division by zero
            }

            // Draw axes (spokes of the radar) and labels
            g2.setFont(chartFont);
            for (int i = 0; i < sentiments.length; i++) {
                double angle = Math.PI * 2 * i / sentiments.length - Math.PI / 2;
                int x = (int) (centerX + radius * Math.cos(angle));
                int y = (int) (centerY + radius * Math.sin(angle));

                // Draw axis line
                g2.setColor(new Color(230, 230, 230));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(centerX, centerY, x, y);

                // Draw sentiment label
                g2.setColor(TEXT_COLOR);
                String sentiment = sentiments[i];
                FontMetrics fm = g2.getFontMetrics();
                int labelX = (int) (centerX + (radius + 40) * Math.cos(angle));
                int labelY = (int) (centerY + (radius + 40) * Math.sin(angle));

                // Adjust label position based on angle
                if (angle < Math.PI / 2 && angle > -Math.PI / 2) {
                    labelX -= fm.stringWidth(sentiment) / 2;
                } else {
                    labelX -= fm.stringWidth(sentiment);
                }
                labelY += fm.getAscent() / 3;

                g2.drawString(sentiment, labelX, labelY);
            }

            // Draw concentric circles (scales) with improved styling
            g2.setStroke(new BasicStroke(1.2f));
            for (int i = 1; i <= 5; i++) {
                int currentRadius = radius * i / 5;
                g2.setColor(new Color(230, 230, 230));
                g2.drawOval(centerX - currentRadius, centerY - currentRadius,
                        currentRadius * 2, currentRadius * 2);

                // Draw scale label
                g2.setColor(new Color(150, 150, 150));
                g2.drawString(String.valueOf(maxCount * i / 5), centerX + 5, centerY - currentRadius + 5);
            }

            // Draw data polygon with improved styling
            Polygon polygon = new Polygon();
            for (int i = 0; i < sentiments.length; i++) {
                double angle = Math.PI * 2 * i / sentiments.length - Math.PI / 2;
                int count = sentimentCounts.get(sentiments[i]);
                int scaledRadius = (int) (radius * count / (float) maxCount);
                int x = (int) (centerX + scaledRadius * Math.cos(angle));
                int y = (int) (centerY + scaledRadius * Math.sin(angle));
                polygon.addPoint(x, y);

                // Draw data points with shadow effect
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillOval(x - 7, y - 7 + 2, 14, 14);

                g2.setColor(sentimentColors[i]);
                g2.fillOval(x - 7, y - 7, 14, 14);
                g2.setColor(Color.WHITE);
                g2.fillOval(x - 3, y - 3, 6, 6);
            }

            // Fill the polygon with semi-transparent color
            Color fillColor = new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(),
                    PRIMARY_COLOR.getBlue(), 30);
            g2.setColor(fillColor);
            g2.fillPolygon(polygon);

            // Draw polygon border
            g2.setColor(PRIMARY_COLOR);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawPolygon(polygon);

            // Draw legend
            int legendX = width - 180;
            int legendY = height - 120;
            g2.setColor(TEXT_COLOR);
            g2.drawString("Sentiment Legend:", legendX, legendY - 10);

            for (int i = 0; i < sentiments.length; i++) {
                g2.setColor(sentimentColors[i]);
                g2.fillRect(legendX, legendY + i * 20, 15, 15);
                g2.setColor(TEXT_COLOR);
                g2.drawString(sentiments[i], legendX + 20, legendY + i * 20 + 12);
            }
        }
    }

    // Custom Line Chart Panel for Weekly Analysis with improved UI
    class LineChartPanel extends JPanel {
        private final String username;
        private final List<DailyScore> dailyScores;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
        private final Font chartFont = new Font("Segoe UI", Font.PLAIN, 12);
        private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
        private final Font axisFont = new Font("Segoe UI", Font.PLAIN, 11);

        public LineChartPanel(String username) {
            this.username = username;
            this.dailyScores = new ArrayList<>();
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            loadWeeklyData();
        }

        private void loadWeeklyData() {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT date, average_score " +
                                 "FROM user_daily_sentiment " +
                                 "WHERE username = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                                 "ORDER BY date")) {

                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Date date = rs.getDate("date");
                    double score = rs.getDouble("average_score");
                    dailyScores.add(new DailyScore(date, score));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int leftPadding = 60;
            int rightPadding = 150; // Increased for legend
            int topPadding = 60;
            int bottomPadding = 60;
            int chartWidth = width - leftPadding - rightPadding;
            int chartHeight = height - topPadding - bottomPadding;

            // Draw title
            g2.setFont(titleFont);
            g2.setColor(TEXT_COLOR);
            String title = "Weekly Sentiment Trend";
            int titleWidth = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (width - titleWidth) / 2, 40);

            if (dailyScores.isEmpty()) {
                g2.setColor(new Color(150, 150, 150));
                g2.drawString("No data available for the past week", width / 2 - 100, height / 2);
                return;
            }

            // Draw Y-axis
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(leftPadding, height - bottomPadding, leftPadding, topPadding);

            // Draw X-axis
            g2.drawLine(leftPadding, height - bottomPadding, width - rightPadding, height - bottomPadding);

            // Draw Y-axis labels and grid lines
            g2.setFont(axisFont);
            for (int i = 0; i <= 4; i++) {
                int y = height - bottomPadding - (i * chartHeight / 4);

                // Grid line
                g2.setColor(new Color(230, 230, 230));
                g2.drawLine(leftPadding, y, width - rightPadding, y);

                // Axis label
                g2.setColor(TEXT_COLOR);
                g2.drawString(String.valueOf(i), leftPadding - 25, y + 5);
            }

            // Handle case when there's only one data point
            if (dailyScores.size() == 1) {
                DailyScore ds = dailyScores.get(0);
                int x = leftPadding + chartWidth / 2;
                int y = height - bottomPadding - (int)(ds.score * chartHeight / 4);

                // Draw single data point
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillOval(x - 6, y - 6 + 2, 12, 12);
                g2.setColor(PRIMARY_COLOR);
                g2.fillOval(x - 6, y - 6, 12, 12);
                g2.setColor(TEXT_COLOR);
                g2.drawString(String.format("%.1f", ds.score), x - 10, y - 10);

                // Draw date label
                String dateStr = dateFormat.format(ds.date);
                int dateWidth = g2.getFontMetrics().stringWidth(dateStr);
                g2.drawString(dateStr, x - dateWidth / 2, height - bottomPadding + 20);
            } else {
                // Draw X-axis labels for multiple points
                g2.setColor(TEXT_COLOR);
                for (int i = 0; i < dailyScores.size(); i++) {
                    DailyScore ds = dailyScores.get(i);
                    int x = leftPadding + (i * chartWidth / Math.max(1, (dailyScores.size() - 1)));
                    String dateStr = dateFormat.format(ds.date);
                    int dateWidth = g2.getFontMetrics().stringWidth(dateStr);
                    g2.drawString(dateStr, x - dateWidth / 2, height - bottomPadding + 20);
                }

                // Draw data points and line for multiple points
                g2.setColor(PRIMARY_COLOR);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int lastX = 0, lastY = 0;
                for (int i = 0; i < dailyScores.size(); i++) {
                    DailyScore ds = dailyScores.get(i);
                    int x = leftPadding + (i * chartWidth / Math.max(1, (dailyScores.size() - 1)));
                    int y = height - bottomPadding - (int)(ds.score * chartHeight / 4);

                    // Draw connecting line
                    if (i > 0) {
                        g2.drawLine(lastX, lastY, x, y);
                    }

                    // Draw data point with shadow effect
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillOval(x - 6, y - 6 + 2, 12, 12);

                    g2.setColor(PRIMARY_COLOR);
                    g2.fillOval(x - 6, y - 6, 12, 12);

                    // Draw value label
                    g2.setColor(TEXT_COLOR);
                    g2.drawString(String.format("%.1f", ds.score), x - 10, y - 10);

                    lastX = x;
                    lastY = y;
                }
            }

            // Draw sentiment level legend
            int legendX = width - rightPadding + 20;
            int legendY = topPadding + 30;

            g2.setColor(TEXT_COLOR);
            g2.drawString("Sentiment Levels:", legendX, legendY - 10);

            String[] levelNames = {"Very Negative", "Negative", "Neutral", "Positive", "Very Positive"};
            Color[] levelColors = {VERY_NEGATIVE, NEGATIVE, NEUTRAL, POSITIVE, VERY_POSITIVE};

            for (int i = 0; i < levelNames.length; i++) {
                g2.setColor(levelColors[i]);
                g2.fillRect(legendX, legendY + i * 20, 12, 12);
                g2.setColor(TEXT_COLOR);
                g2.drawString(levelNames[i] + " (" + i + ")", legendX + 15, legendY + i * 20 + 10);
            }
        }
        private static class DailyScore {
            Date date;
            double score;

            public DailyScore(Date date, double score) {
                this.date = date;
                this.score = score;
            }
        }
    }

    // Database methods
    private boolean hasCompletedToday(String username) throws SQLException {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String sql = "SELECT COUNT(*) FROM user_asked_questions WHERE username = ? AND asked_date = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, today);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) >= QUESTIONS_PER_DAY;
        }
    }

    private List<Question> getRandomQuestions(String username) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, question_text FROM assessment_questions " +
                "WHERE id NOT IN (SELECT question_id FROM user_asked_questions WHERE username = ? AND asked_date = CURDATE()) " +
                "ORDER BY RAND() LIMIT ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, QUESTIONS_PER_DAY);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(new Question(rs.getInt("id"), rs.getString("question_text")));
            }
        }
        return questions;
    }

    private void saveResponses(String username, Map<Integer, SentimentResult> responses) throws SQLException {
        String sql = "INSERT INTO user_assessment_responses (username, question_id, response_time, selected_sentiment, sentiment_score) " +
                "VALUES (?, ?, NOW(), ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Map.Entry<Integer, SentimentResult> entry : responses.entrySet()) {
                stmt.setString(1, username);
                stmt.setInt(2, entry.getKey());
                stmt.setString(3, entry.getValue().type);
                stmt.setInt(4, entry.getValue().score);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void markQuestionsAsAsked(String username, Collection<Integer> questionIds) throws SQLException {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String sql = "INSERT INTO user_asked_questions (username, question_id, asked_date) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Integer questionId : questionIds) {
                stmt.setString(1, username);
                stmt.setInt(2, questionId);
                stmt.setString(3, today);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static class Question {
        private final int id;
        private final String text;

        public Question(int id, String text) {
            this.id = id;
            this.text = text;
        }

        public int getId() { return id; }
        public String getText() { return text; }
    }

    private static class SentimentResult {
        int score; // 0=very negative, 1=negative, 2=neutral, 3=positive, 4=very positive
        String type; // veryNegative, negative, neutral, positive, veryPositive

        public SentimentResult() {
            this(2, "neutral");
        }

        public SentimentResult(int score, String type) {
            this.score = score;
            this.type = type;
        }
    }

    private static class AssessmentEntry {
        String question;
        String response;
        String time;
        int sentimentScore;
        String sentimentType;

        public AssessmentEntry(String question, String response, String time,
                               int sentimentScore, String sentimentType) {
            this.question = question;
            this.response = response;
            this.time = time;
            this.sentimentScore = sentimentScore;
            this.sentimentType = sentimentType;
        }
    }
}