package frames;

import app.App;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.ejml.simple.SimpleMatrix;
import utilities.Database;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class Journal extends JFrame {

    private static final Color PRIMARY_COLOR = new Color(99, 102, 241);
    private static final Color SECONDARY_COLOR = new Color(248, 250, 252);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = new Color(31, 41, 55);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color SUBTEXT_COLOR = new Color(100, 116, 139);

    private static final Color VERY_NEGATIVE = new Color(220, 38, 38);
    private static final Color NEGATIVE = new Color(245, 158, 11);
    private static final Color NEUTRAL = new Color(156, 163, 175);
    private static final Color POSITIVE = new Color(16, 185, 129);
    private static final Color VERY_POSITIVE = new Color(5, 150, 105);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 16);
    private static final Font ENTRY_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font SENTIMENT_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font DATE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final String username;
    private JTextArea editorArea;
    private JPanel entriesPanel;
    private JDialog historyDialog;
    private JDialog analysisDialog;
    private static StanfordCoreNLP pipeline;

    static {
        try {
            File ejml = new File("lib/ejml-0.23.jar");
            if (!ejml.exists()) {
                throw new FileNotFoundException("ejml-0.23.jar not found at: " + ejml.getAbsolutePath());
            }

            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
            props.setProperty("tokenize.language", "en");
            System.setProperty("stanford.nlp.pipeline.model.path", new File("lib").getAbsolutePath());

            pipeline = new StanfordCoreNLP(props);
            System.out.println("SUCCESS! Pipeline initialized.");

        } catch (Exception e) {
            String errorMsg = "Initialization failed:\n" +
                    "EJML exists: " + new File("lib/ejml-0.23.jar").exists() + "\n" +
                    "Error: " + e;
            JOptionPane.showMessageDialog(null, errorMsg, "Critical Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public Journal(String username) {
        this.username = username;
        initializeFrame();
        createHeaderPanel();
        createMainContentPanel();
        setupWindow();
    }

    private void initializeFrame() {
        setTitle(App.getTitle() + " - Journal - " + username);
        setIconImage(App.getIcon());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(SECONDARY_COLOR);
        setLayout(new BorderLayout());
    }

    private void setupWindow() {
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(Toolkit.getDefaultToolkit().getScreenSize());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        headerPanel.setBackground(SECONDARY_COLOR);

        JLabel titleLabel = new JLabel("Personal Journal");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(0, 40, 40, 40));
        mainPanel.setBackground(SECONDARY_COLOR);

        JPanel editorCard = createEditorCard();
        JScrollPane scrollPane = new JScrollPane(editorCard);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(SECONDARY_COLOR);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton historyButton = createModernButton("View History", PRIMARY_COLOR);
        historyButton.addActionListener(e -> showHistoryDialog());

        JButton analysisButton = createModernButton("Sentiment Analysis", PRIMARY_COLOR);
        analysisButton.addActionListener(e -> showAnalysisDialog());

        JButton closeButton = createModernButton("Close", PRIMARY_COLOR);
        closeButton.addActionListener(e -> this.dispose());

        buttonPanel.add(historyButton);
        buttonPanel.add(analysisButton);
        buttonPanel.add(closeButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void showAnalysisDialog() {
        if (analysisDialog == null) {
            analysisDialog = new JDialog(this, "Journal Sentiment Analysis - " + username, false);
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

            JLabel titleLabel = new JLabel("Sentiment Analysis");
            titleLabel.setFont(TITLE_FONT);
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

            headerPanel.add(titleLabel, BorderLayout.CENTER);

            JPanel chartsPanel = new JPanel();
            chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
            chartsPanel.setBackground(SECONDARY_COLOR);

            JPanel sentimentBreakdownCard = createChartCard("Daily Sentiment Breakdown", "breakdown");
            chartsPanel.add(sentimentBreakdownCard);

            chartsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            //weekly chart card
            JPanel weeklyAnalysisCard = createChartCard("Weekly Sentiment Analysis", "weekly");
            chartsPanel.add(weeklyAnalysisCard);

            JScrollPane scrollPane = new JScrollPane(chartsPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(SECONDARY_COLOR);

            JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            closePanel.setOpaque(false);
            closePanel.setBorder(new EmptyBorder(20, 0, 0, 0));

            JButton closeButton = createModernButton("Close", PRIMARY_COLOR);
            closeButton.addActionListener(e -> analysisDialog.dispose());
            closePanel.add(closeButton);

            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(closePanel, BorderLayout.SOUTH);
            analysisDialog.add(mainPanel);
        }

        refreshAnalysisCharts();
        analysisDialog.setVisible(true);
    }

    private JPanel createChartCard(String title, String chartType) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(12, BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(900, 400));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if ("breakdown".equals(chartType)) {
                    drawSentimentBreakdownChart(g, getWidth(), getHeight());
                } else if ("weekly".equals(chartType)) {
                    drawWeeklySentimentChart(g, getWidth(), getHeight());
                }
            }
        };
        chartPanel.setBackground(CARD_COLOR);
        chartPanel.setPreferredSize(new Dimension(800, 300));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }
    private void drawWeeklySentimentChart(Graphics g, int width, int height) {
        Map<String, Map<String, Double>> weeklyData = getWeeklySentimentAverages();

        if (weeklyData.isEmpty()) {
            drawNoDataMessage(g, width, height);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int margin = 40;
        int chartWidth = width - 2 * margin - 120;
        int chartHeight = height - 2 * margin;
        int numDays = weeklyData.size();
        if (numDays == 0) {
            drawNoDataMessage(g, width, height);
            return;
        }

        int barWidth = chartWidth / (numDays * 2);
        int spacing = barWidth / 2;
        int xPos = margin + spacing;

        String[] categories = {"Very Positive", "Positive", "Neutral", "Negative", "Very Negative"};
        Color[] colors = {VERY_POSITIVE, POSITIVE, NEUTRAL, NEGATIVE, VERY_NEGATIVE};

        // Find maximum value for scaling
        double maxValue = 0;
        for (Map<String, Double> dayData : weeklyData.values()) {
            for (Double value : dayData.values()) {
                if (value > maxValue) maxValue = value;
            }
        }
        if (maxValue == 0) maxValue = 100;

        // Draw bars for each day
        for (Map.Entry<String, Map<String, Double>> dayEntry : weeklyData.entrySet()) {
            String dayName = dayEntry.getKey();
            Map<String, Double> dayData = dayEntry.getValue();

            int yPos = margin + chartHeight;
            int segmentHeight;

            // Draw each sentiment segment
            for (int i = 0; i < categories.length; i++) {
                String category = categories[i];
                double value = dayData.getOrDefault(category, 0.0);
                segmentHeight = (int) ((value / maxValue) * chartHeight);

                if (segmentHeight > 0) {
                    yPos -= segmentHeight;
                    g2.setColor(colors[i]);
                    g2.fillRect(xPos, yPos, barWidth, segmentHeight);

                    if (segmentHeight > 15) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                        String valueLabel = String.format("%.0f%%", value);
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(valueLabel,
                                xPos + (barWidth - fm.stringWidth(valueLabel))/2,
                                yPos + segmentHeight/2 + 5);
                    }
                }
            }

            // Draw day name below bars
            g2.setColor(SUBTEXT_COLOR);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(dayName, xPos + (barWidth - fm.stringWidth(dayName))/2, margin + chartHeight + 15);

            xPos += barWidth + spacing;
        }

        // Draw Y-axis labels
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(SUBTEXT_COLOR);
        for (int j = 0; j <= maxValue; j += (maxValue > 50 ? 20 : 10)) {
            int yPos = margin + chartHeight - (int)((j / maxValue) * chartHeight);
            g2.drawString(String.valueOf(j), margin - 25, yPos + 5);
        }

        // Draw legend on the right side
        int legendX = width - margin - 100;
        int legendY = margin + 20;

        // Legend title
        g2.setColor(TEXT_COLOR);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("Sentiment", legendX, legendY);
        legendY += 20;

        // Legend items
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i < categories.length; i++) {
            g2.setColor(colors[i]);
            g2.fillRect(legendX, legendY - 10, 10, 10);

            // Category name
            g2.setColor(TEXT_COLOR);
            g2.drawString(categories[i], legendX + 15, legendY);

            legendY += 20;
        }

        g2.dispose();
    }

    private Color getColorForSentimentCategory(String category) {
        switch (category.toLowerCase()) {
            case "very positive": return VERY_POSITIVE;
            case "positive": return POSITIVE;
            case "neutral": return NEUTRAL;
            case "negative": return NEGATIVE;
            case "very negative": return VERY_NEGATIVE;
            default: return NEUTRAL;
        }
    }

    private Map<String, Double> getDailySentimentAverages() {
        Map<String, Double> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT AVG(very_positive) as vp, AVG(positive) as p, " +
                             "AVG(neutral) as n, AVG(negative) as neg, AVG(very_negative) as vn " +
                             "FROM journal_entries WHERE username = ? AND DATE(entry_date) = ?")) {

            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(today));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result.put("Very Positive", rs.getDouble("vp"));
                result.put("Positive", rs.getDouble("p"));
                result.put("Neutral", rs.getDouble("n"));
                result.put("Negative", rs.getDouble("neg"));
                result.put("Very Negative", rs.getDouble("vn"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void drawSentimentBreakdownChart(Graphics g, int width, int height) {
        Map<String, Double> sentimentAverages = getDailySentimentAverages();

        if (sentimentAverages.isEmpty()) {
            drawNoDataMessage(g, width, height);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int margin = 40;
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin;

        int numBars = sentimentAverages.size();
        if (numBars == 0) {
            drawNoDataMessage(g, width, height);
            return;
        }

        int barWidth = chartWidth / (numBars * 2);
        int spacing = barWidth / 2;
        int xPos = margin + spacing;

        for (Map.Entry<String, Double> entry : sentimentAverages.entrySet()) {
            int barHeight = (int)((entry.getValue() / 100.0) * chartHeight);
            int yPos = margin + chartHeight - barHeight;

            Color color = getColorForSentimentCategory(entry.getKey());

            GradientPaint gradient = new GradientPaint(
                    xPos, yPos, color,
                    xPos, yPos + barHeight, new Color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    200)
            );
            g2.setPaint(gradient);

            RoundRectangle2D bar = new RoundRectangle2D.Double(
                    xPos, yPos, barWidth, barHeight, 5, 5);
            g2.fill(bar);

            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRoundRect(xPos, yPos, barWidth, barHeight / 3, 5, 5);

            g2.setColor(TEXT_COLOR);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String valueLabel = String.format("%.0f%%", entry.getValue());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valueLabel, xPos + (barWidth - fm.stringWidth(valueLabel))/2, yPos - 5);

            g2.setColor(SUBTEXT_COLOR);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String categoryLabel = entry.getKey();
            g2.drawString(categoryLabel, xPos + (barWidth - fm.stringWidth(categoryLabel))/2,
                    margin + chartHeight + 15);

            xPos += barWidth + spacing;
        }

        // Draw Y-axis labels
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(SUBTEXT_COLOR);
        for (int j = 0; j <= 100; j += 20) {
            int yPos = margin + chartHeight - (int)((j / 100.0) * chartHeight);
            g2.drawString(String.valueOf(j), margin - 25, yPos + 5);
        }

        g2.dispose();
    }

    private Map<String, Map<String, Double>> getWeeklySentimentAverages() {
        Map<String, Map<String, Double>> weeklyData = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(6);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, avg_very_positive, avg_positive, avg_neutral, " +
                             "avg_negative, avg_very_negative FROM user_daily_avg_journal_sentiment " +
                             "WHERE username = ? AND date BETWEEN ? AND ? " +
                             "ORDER BY date")) {

            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(startOfWeek));
            stmt.setDate(3, Date.valueOf(today));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Double> dayData = new HashMap<>();
                dayData.put("Very Positive", rs.getDouble("avg_very_positive"));
                dayData.put("Positive", rs.getDouble("avg_positive"));
                dayData.put("Neutral", rs.getDouble("avg_neutral"));
                dayData.put("Negative", rs.getDouble("avg_negative"));
                dayData.put("Very Negative", rs.getDouble("avg_very_negative"));

                weeklyData.put(rs.getDate("date").toLocalDate().format(DateTimeFormatter.ofPattern("EEE")), dayData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            String dayName = day.format(DateTimeFormatter.ofPattern("EEE"));
            if (!weeklyData.containsKey(dayName)) {
                Map<String, Double> emptyDay = new HashMap<>();
                emptyDay.put("Very Positive", 0.0);
                emptyDay.put("Positive", 0.0);
                emptyDay.put("Neutral", 0.0);
                emptyDay.put("Negative", 0.0);
                emptyDay.put("Very Negative", 0.0);
                weeklyData.put(dayName, emptyDay);
            }
        }

        return weeklyData;
    }

    private void refreshAnalysisCharts() {
        if (analysisDialog != null) {
            analysisDialog.repaint();
        }
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


        g2.setColor(SUBTEXT_COLOR);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        String message = "No data available";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(message, (width - fm.stringWidth(message)) / 2, iconY + iconSize + 30);

        g2.dispose();
    }

    private JPanel createEditorCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(12, BORDER_COLOR),
                new EmptyBorder(25, 25, 25, 25)
        ));
        card.setMaximumSize(new Dimension(900, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel("New Journal Entry");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        editorArea = new JTextArea();
        editorArea.setFont(ENTRY_FONT);
        editorArea.setLineWrap(true);
        editorArea.setWrapStyleWord(true);
        editorArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane editorScroll = new JScrollPane(editorArea);
        editorScroll.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(8, BORDER_COLOR),
                new EmptyBorder(0, 0, 0, 0)
        ));
        editorScroll.setPreferredSize(new Dimension(800, 400));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton saveButton = createModernButton("Save Entry", PRIMARY_COLOR);
        saveButton.addActionListener(e -> saveJournalEntry());

        buttonPanel.add(saveButton);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(editorScroll, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setPreferredSize(new Dimension(150, 42));
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBorder(BorderFactory.createCompoundBorder(
                        new RoundBorder(8, bgColor.darker()),
                        new EmptyBorder(2, 2, 2, 2)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBorder(BorderFactory.createEmptyBorder());
            }
        });

        return button;
    }

    private void saveJournalEntry() {
        String entry = editorArea.getText().trim();
        if (entry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write something before saving.",
                    "Empty Entry", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SentimentResult sentiment = analyzeSentiment(entry);
        LocalDate today = LocalDate.now();

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO journal_entries (username, entry, entry_date, sentiment_score, sentiment_type, " +
                            "very_positive, positive, neutral, negative, very_negative) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                stmt.setString(1, username);
                stmt.setString(2, entry);
                stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(4, sentiment.getSentimentScore());
                stmt.setString(5, sentiment.getSentimentType());
                stmt.setDouble(6, sentiment.getSentimentClass().getVeryPositive());
                stmt.setDouble(7, sentiment.getSentimentClass().getPositive());
                stmt.setDouble(8, sentiment.getSentimentClass().getNeutral());
                stmt.setDouble(9, sentiment.getSentimentClass().getNegative());
                stmt.setDouble(10, sentiment.getSentimentClass().getVeryNegative());

                if (stmt.executeUpdate() == 0) {
                    throw new SQLException("Failed to insert journal entry");
                }
            }

            updateDailyAverages(conn, today, sentiment);

            conn.commit();
            showSaveSuccessDialog(sentiment);
            editorArea.setText("");
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showErrorDialog("Error saving journal entry: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateDailyAverages(Connection conn, LocalDate date, SentimentResult newEntry) throws SQLException {

        boolean exists = false;
        int currentCount = 0;

        try (PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT entry_count FROM user_daily_avg_journal_sentiment WHERE username = ? AND date = ?")) {
            checkStmt.setString(1, username);
            checkStmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    exists = true;
                    currentCount = rs.getInt("entry_count");
                }
            }
        }

        if (exists) {
            // Calculate new averages
            double newCount = currentCount + 1;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE user_daily_avg_journal_sentiment SET " +
                            "entry_count = entry_count + 1, " +
                            "avg_very_positive = ((avg_very_positive * ?) + ?) / ?, " +
                            "avg_positive = ((avg_positive * ?) + ?) / ?, " +
                            "avg_neutral = ((avg_neutral * ?) + ?) / ?, " +
                            "avg_negative = ((avg_negative * ?) + ?) / ?, " +
                            "avg_very_negative = ((avg_very_negative * ?) + ?) / ? " +
                            "WHERE username = ? AND date = ?")) {

                // Set parameters for each calculation
                int paramIndex = 1;
                for (int i = 0; i < 5; i++) { // For each of the 5 sentiment categories
                    stmt.setDouble(paramIndex++, currentCount);
                    stmt.setDouble(paramIndex++, getSentimentValue(newEntry, i));
                    stmt.setDouble(paramIndex++, newCount);
                }

                // WHERE clause parameters
                stmt.setString(paramIndex++, username);
                stmt.setDate(paramIndex, Date.valueOf(date));

                stmt.executeUpdate();
            }
        } else {
            // Insert new daily average (for first entry of the day)
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO user_daily_avg_journal_sentiment " +
                            "(username, date, avg_very_positive, avg_positive, avg_neutral, " +
                            "avg_negative, avg_very_negative, entry_count) VALUES (?, ?, ?, ?, ?, ?, ?, 1)")) {

                stmt.setString(1, username);
                stmt.setDate(2, Date.valueOf(date));
                stmt.setDouble(3, newEntry.getSentimentClass().getVeryPositive());
                stmt.setDouble(4, newEntry.getSentimentClass().getPositive());
                stmt.setDouble(5, newEntry.getSentimentClass().getNeutral());
                stmt.setDouble(6, newEntry.getSentimentClass().getNegative());
                stmt.setDouble(7, newEntry.getSentimentClass().getVeryNegative());

                stmt.executeUpdate();
            }
        }
    }

    // Helper method to get sentiment values by index
    private double getSentimentValue(SentimentResult entry, int index) {
        switch(index) {
            case 0: return entry.getSentimentClass().getVeryPositive();
            case 1: return entry.getSentimentClass().getPositive();
            case 2: return entry.getSentimentClass().getNeutral();
            case 3: return entry.getSentimentClass().getNegative();
            case 4: return entry.getSentimentClass().getVeryNegative();
            default: return 0;
        }
    }

    private SentimentResult analyzeSentiment(String text) {
        SentimentResult result = new SentimentResult();
        if (text == null || text.trim().isEmpty()) return result;

        Annotation annotation = pipeline.process(text);
        List<Double> scores = new ArrayList<>();
        SentimentClassification sentimentClass = new SentimentClassification();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            SimpleMatrix sm = RNNCoreAnnotations.getPredictions(tree);

            // Aggregate scores
            scores.add((double)RNNCoreAnnotations.getPredictedClass(tree));

            // Accumulate sentiment percentages
            sentimentClass.setVeryPositive(sentimentClass.getVeryPositive() + (double)Math.round(sm.get(4) * 100d));
            sentimentClass.setPositive(sentimentClass.getPositive() + (double)Math.round(sm.get(3) * 100d));
            sentimentClass.setNeutral(sentimentClass.getNeutral() + (double)Math.round(sm.get(2) * 100d));
            sentimentClass.setNegative(sentimentClass.getNegative() + (double)Math.round(sm.get(1) * 100d));
            sentimentClass.setVeryNegative(sentimentClass.getVeryNegative() + (double)Math.round(sm.get(0) * 100d));
        }

        // Calculate averages
        int sentenceCount = scores.size();
        if (sentenceCount > 0) {
            // Average sentiment score
            double avgScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(2);
            result.setSentimentScore((int)Math.round(avgScore));

            // Average sentiment percentages
            sentimentClass.setVeryPositive(sentimentClass.getVeryPositive() / sentenceCount);
            sentimentClass.setPositive(sentimentClass.getPositive() / sentenceCount);
            sentimentClass.setNeutral(sentimentClass.getNeutral() / sentenceCount);
            sentimentClass.setNegative(sentimentClass.getNegative() / sentenceCount);
            sentimentClass.setVeryNegative(sentimentClass.getVeryNegative() / sentenceCount);

            // Determine overall sentiment type based on average score
            String[] sentimentTypes = {"Very Negative", "Negative", "Neutral", "Positive", "Very Positive"};
            result.setSentimentType(sentimentTypes[(int)Math.round(avgScore)]);
            result.setSentimentClass(sentimentClass);
        }

        return result;
    }

    private void showSaveSuccessDialog(SentimentResult sentiment) {
        String sentimentDetails = "<div style='margin-top:15px;text-align:center;'>" +
                "<div style='font-weight:bold;color:" + getSentimentHexColor(sentiment.getSentimentScore()) + ";margin-bottom:10px;font-size:16px;'>" +
                "Sentiment: " + sentiment.getSentimentType() + " (" + sentiment.getSentimentScore() + "/4)" +
                "</div>" +
                "<table style='width:100%;border-collapse:collapse;margin-top:10px;'>" +
                "<tr><td style='text-align:left;padding:5px;font-size:14px;'>Very Positive:</td><td style='text-align:right;font-weight:bold;'>" +
                String.format("%.0f", sentiment.getSentimentClass().getVeryPositive()) + "%</td></tr>" +
                "<tr><td style='text-align:left;padding:5px;font-size:14px;'>Positive:</td><td style='text-align:right;font-weight:bold;'>" +
                String.format("%.0f", sentiment.getSentimentClass().getPositive()) + "%</td></tr>" +
                "<tr><td style='text-align:left;padding:5px;font-size:14px;'>Neutral:</td><td style='text-align:right;font-weight:bold;'>" +
                String.format("%.0f", sentiment.getSentimentClass().getNeutral()) + "%</td></tr>" +
                "<tr><td style='text-align:left;padding:5px;font-size:14px;'>Negative:</td><td style='text-align:right;font-weight:bold;'>" +
                String.format("%.0f", sentiment.getSentimentClass().getNegative()) + "%</td></tr>" +
                "<tr><td style='text-align:left;padding:5px;font-size:14px;'>Very Negative:</td><td style='text-align:right;font-weight:bold;'>" +
                String.format("%.0f", sentiment.getSentimentClass().getVeryNegative()) + "%</td></tr>" +
                "</table></div>";

        JLabel message = new JLabel("<html><div style='text-align:center;width:300px;'>" +
                "<h3 style='color:#6366f1;margin-bottom:10px;'>Entry Saved Successfully</h3>" +
                "<p style='font-size:14px;'>Your thoughts have been recorded.</p>" +
                sentimentDetails +
                "</div></html>");

        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showHistoryDialog() {
        if (historyDialog == null) {
            historyDialog = new JDialog(this, "Journal History - " + username, false);
            historyDialog.setIconImage(App.getIcon());

            // Match main window size and position
            historyDialog.setBounds(this.getBounds());
            historyDialog.setResizable(true);
            historyDialog.getContentPane().setBackground(SECONDARY_COLOR);

            // Main panel with consistent padding
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(new EmptyBorder(20, 40, 40, 40));
            mainPanel.setBackground(SECONDARY_COLOR);

            // Header panel with title only
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
            headerPanel.setBackground(SECONDARY_COLOR);

            // Title
            JLabel titleLabel = new JLabel("Journal History");
            titleLabel.setFont(TITLE_FONT);
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

            headerPanel.add(titleLabel, BorderLayout.CENTER);

            // Entries panel
            entriesPanel = new JPanel();
            entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.Y_AXIS));
            entriesPanel.setBackground(SECONDARY_COLOR);

            JScrollPane scrollPane = new JScrollPane(entriesPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(SECONDARY_COLOR);

            // Create close button panel for history dialog
            JPanel historyClosePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            historyClosePanel.setOpaque(false);
            historyClosePanel.setBorder(new EmptyBorder(20, 0, 0, 0));

            JButton historyCloseButton = createModernButton("Close", PRIMARY_COLOR);
            historyCloseButton.addActionListener(e -> historyDialog.dispose());
            historyClosePanel.add(historyCloseButton);

            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(historyClosePanel, BorderLayout.SOUTH);
            historyDialog.add(mainPanel);
        }

        loadJournalEntries();
        historyDialog.setVisible(true);
    }

    private void loadJournalEntries() {
        entriesPanel.removeAll();

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT entry_date, entry, sentiment_score, sentiment_type, " +
                             "very_positive, positive, neutral, negative, very_negative " +
                             "FROM journal_entries WHERE username = '" + username + "' ORDER BY entry_date DESC")) {

            List<JournalEntry> entries = new ArrayList<>();
            while (rs.next()) {
                entries.add(new JournalEntry(
                        rs.getTimestamp("entry_date"),
                        rs.getString("entry"),
                        rs.getInt("sentiment_score"),
                        rs.getString("sentiment_type"),
                        rs.getDouble("very_positive"),
                        rs.getDouble("positive"),
                        rs.getDouble("neutral"),
                        rs.getDouble("negative"),
                        rs.getDouble("very_negative")
                ));
            }

            if (entries.isEmpty()) {
                JLabel noEntries = new JLabel("<html><div style='text-align:center;color:#666;font-size:14px;'>" +
                        "No journal entries found.<br>Start writing your thoughts!</div></html>");
                noEntries.setFont(SUBTITLE_FONT);
                noEntries.setHorizontalAlignment(SwingConstants.CENTER);
                noEntries.setBorder(new EmptyBorder(50, 0, 50, 0));
                entriesPanel.add(noEntries);
            } else {
                // Add a glue component first to push content to top
                entriesPanel.add(Box.createVerticalGlue());

                for (JournalEntry entry : entries) {
                    entriesPanel.add(createEntryCard(entry));
                    entriesPanel.add(Box.createRigidArea(new Dimension(0, 20)));
                }

                // Add another glue at the end to maintain spacing
                entriesPanel.add(Box.createVerticalGlue());
            }

        } catch (Exception e) {
            showErrorDialog("Error loading journal entries: " + e.getMessage());
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = ((JScrollPane)entriesPanel.getParent().getParent()).getViewport();
            viewport.setViewPosition(new Point(0, 0));
        });

        entriesPanel.revalidate();
        entriesPanel.repaint();
    }

    private JPanel createEntryCard(JournalEntry entry) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(12, BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(900, Integer.MAX_VALUE));

        // Header with date and sentiment
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Formatted date
        String formattedDate = entry.date.toLocalDateTime().format(
                DateTimeFormatter.ofPattern("MMMM dd, yyyy - hh:mm a"));
        JLabel dateLabel = new JLabel(formattedDate);
        dateLabel.setFont(DATE_FONT);
        dateLabel.setForeground(SUBTEXT_COLOR);

        // Sentiment badge
        JPanel sentimentBadge = createSentimentBadge(entry.score, entry.type);

        headerPanel.add(dateLabel, BorderLayout.WEST);
        headerPanel.add(sentimentBadge, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);

        // Entry text
        JTextArea entryText = new JTextArea(entry.text);
        entryText.setFont(ENTRY_FONT);
        entryText.setLineWrap(true);
        entryText.setWrapStyleWord(true);
        entryText.setEditable(false);
        entryText.setOpaque(false);
        entryText.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Sentiment details
        JPanel sentimentPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        sentimentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        sentimentPanel.setOpaque(false);

        sentimentPanel.add(createSentimentBar("Very Positive", entry.veryPositive, VERY_POSITIVE));
        sentimentPanel.add(createSentimentBar("Positive", entry.positive, POSITIVE));
        sentimentPanel.add(createSentimentBar("Neutral", entry.neutral, NEUTRAL));
        sentimentPanel.add(createSentimentBar("Negative", entry.negative, NEGATIVE));
        sentimentPanel.add(createSentimentBar("Very Negative", entry.veryNegative, VERY_NEGATIVE));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(entryText, BorderLayout.CENTER);
        contentPanel.add(sentimentPanel, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSentimentBar(String label, double value, Color color) {
        JPanel barPanel = new JPanel();
        barPanel.setLayout(new BoxLayout(barPanel, BoxLayout.Y_AXIS));
        barPanel.setOpaque(false);
        barPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Value percentage label
        JLabel valueLabel = new JLabel(String.format("%.0f%%", value), SwingConstants.CENTER);
        valueLabel.setFont(SENTIMENT_FONT);
        valueLabel.setForeground(TEXT_COLOR);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Bar visualization
        JPanel barContainer = new JPanel(new BorderLayout());
        barContainer.setOpaque(false);
        barContainer.setMaximumSize(new Dimension(70, 10));
        barContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(BORDER_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);

                // Value
                int width = (int) (getWidth() * (value / 100.0));
                g2.setColor(color);
                g2.fillRoundRect(0, 0, width, getHeight(), 5, 5);

                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(70, 8));
        barContainer.add(bar, BorderLayout.CENTER);

        // Sentiment type label
        JLabel nameLabel = new JLabel(label, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLabel.setForeground(SUBTEXT_COLOR);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components with proper spacing
        barPanel.add(valueLabel);
        barPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Small vertical space
        barPanel.add(barContainer);
        barPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Small vertical space
        barPanel.add(nameLabel);

        return barPanel;
    }

    private JPanel createSentimentBadge(int score, String type) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        badge.setOpaque(false);

        // Create the sentiment label with colored text
        JLabel sentimentLabel = new JLabel("Type: "+type + ", Total Score: " + score + "/4");
        sentimentLabel.setFont(SENTIMENT_FONT);
        sentimentLabel.setForeground(getSentimentColor(score));

        badge.add(sentimentLabel);
        return badge;
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

    // Custom rounded border class
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

    private static class SentimentClassification {
        double veryPositive;
        double positive;
        double neutral;
        double negative;
        double veryNegative;

        public double getVeryPositive() {
            return veryPositive;
        }
        public void setVeryPositive(double veryPositive) {
            this.veryPositive = veryPositive;
        }
        public double getPositive() {
            return positive;
        }
        public void setPositive(double positive) {
            this.positive = positive;
        }
        public double getNeutral() {
            return neutral;
        }
        public void setNeutral(double neutral) {
            this.neutral = neutral;
        }
        public double getNegative() {
            return negative;
        }
        public void setNegative(double negative) {
            this.negative = negative;
        }
        public double getVeryNegative() {
            return veryNegative;
        }
        public void setVeryNegative(double veryNegative) {
            this.veryNegative = veryNegative;
        }
    }

    private static class SentimentResult {
        int sentimentScore;
        String sentimentType;
        SentimentClassification sentimentClass;

        public int getSentimentScore() {
            return sentimentScore;
        }
        public void setSentimentScore(int sentimentScore) {
            this.sentimentScore = sentimentScore;
        }
        public String getSentimentType() {
            return sentimentType;
        }
        public void setSentimentType(String sentimentType) {
            this.sentimentType = sentimentType;
        }
        public SentimentClassification getSentimentClass() {
            return sentimentClass;
        }
        public void setSentimentClass(SentimentClassification sentimentClass) {
            this.sentimentClass = sentimentClass;
        }
    }

    private static class JournalEntry {
        Timestamp date;
        String text;
        int score;
        String type;
        double veryPositive;
        double positive;
        double neutral;
        double negative;
        double veryNegative;

        public JournalEntry(Timestamp date, String text, int score, String type,
                            double veryPositive, double positive, double neutral,
                            double negative, double veryNegative) {
            this.date = date;
            this.text = text;
            this.score = score;
            this.type = type;
            this.veryPositive = veryPositive;
            this.positive = positive;
            this.neutral = neutral;
            this.negative = negative;
            this.veryNegative = veryNegative;
        }
    }
}