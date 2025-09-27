package frames;

import utilities.Database;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Reminders {
    private final String username;
    private final JFrame parentFrame;

    public Reminders(String username, JFrame parentFrame) {
        this.username = username;
        this.parentFrame = parentFrame;
    }

    public void checkPendingActivities() {
        List<String> pendingActivities = getPendingActivities();

        if (pendingActivities.isEmpty()) {
            showCompletionMessage();
        } else {
            showReminderDialog(pendingActivities);
        }
    }

    private List<String> getPendingActivities() {
        List<String> pending = new ArrayList<>();
        LocalDate today = LocalDate.now();

        try (Connection conn = Database.getConnection()) {
            // Check Daily Assessment
            if (!hasActivityToday(conn, "user_assessment_responses", "response_time")) {
                pending.add("Daily Assessment");
            }

            // Check Mood Log
            if (!hasActivityToday(conn, "mood_logs", "log_time")) {
                pending.add("Mood & Stress Check");
            }

            // Check Journal Entry
            if (!hasActivityToday(conn, "journal_entries", "entry_date")) {
                pending.add("Daily Journal");
            }
        } catch (SQLException e) {
            showError("Error checking activities: " + e.getMessage());
        }

        return pending;
    }

    private boolean hasActivityToday(Connection conn, String tableName, String dateColumn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName +
                " WHERE username = ? AND DATE(" + dateColumn + ") = CURRENT_DATE()";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void showReminderDialog(List<String> pendingActivities) {
        // Create a custom panel with better styling
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(240, 240, 245));

        // Title
        JLabel titleLabel = new JLabel("Pending Activities Reminder");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(70, 70, 70));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Message
        JLabel messageLabel = new JLabel("Please complete these activities:");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(messageLabel, BorderLayout.CENTER);

        // Activities list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));
        listPanel.setBackground(new Color(240, 240, 245));

        for (String activity : pendingActivities) {
            JLabel itemLabel = new JLabel("• " + activity);
            itemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            itemLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            listPanel.add(itemLabel);
        }

        panel.add(new JScrollPane(listPanel), BorderLayout.SOUTH);

        // Show dialog
        JOptionPane.showMessageDialog(
                parentFrame,
                panel,
                "Daily Reminders",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showCompletionMessage() {
        JOptionPane.showMessageDialog(
                parentFrame,
                "All daily activities are completed! Great job!",
                "Completed",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                parentFrame,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}