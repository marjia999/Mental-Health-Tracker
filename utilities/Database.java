package utilities;

import app.App;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class Database {

    private static Connection connection = null;

    public static void checkConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost/" + App.getDatabase(),
                        "root",
                        "#s0ql??@A!>7"
                );
            }
        } catch (Exception e) {
            showError("Could not connect to database: " + App.getDatabase() + "\nError: " + e.getMessage());
        }
    }

    public static ResultSet executeQuery(String query, Object... params) {
        try {
            checkConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            // Set parameters if any
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            return statement.executeQuery();
        } catch (Exception e) {
            showError("Database query failed: " + e.getMessage());
            return null;
        }
    }

    public static int executeUpdate(String update) {
        try {
            checkConnection();
            PreparedStatement statement = connection.prepareStatement(update);
            return statement.executeUpdate();
        } catch (Exception e) {
            showError("Database update failed: " + e.getMessage());
            return 0;
        }
    }

    public static PreparedStatement prepareStatement(String sql) throws SQLException {
        checkConnection();
        return connection.prepareStatement(sql);
    }

    public static Connection getConnection() {
        checkConnection();
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            showError("Error closing database connection: " + e.getMessage());
        }
    }

    public static void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JLabel label = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
            label.setFont(App.getFont().deriveFont(Font.BOLD, 16f));
            label.setForeground(Color.RED);
            label.setBorder(new EmptyBorder(10, 20, 10, 20));

            UIManager.put("OptionPane.background", Color.WHITE);
            UIManager.put("Panel.background", Color.WHITE);

            JOptionPane.showMessageDialog(
                    null,
                    label,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }

    // Helper method to safely close resources
    public static void closeResources(ResultSet rs, Statement stmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            showError("Error closing database resources: " + e.getMessage());
        }
    }

    // Helper method to safely close resources
    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            showError("Error closing database resources: " + e.getMessage());
        }
    }
}