package frames;

import utilities.Database;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class Resources extends JFrame {
    private final String username;
    private DefaultListModel<String> resourceListModel;
    private JList<String> resourceList;
    private JTextField searchField;
    private JTextArea detailsArea;
    private JProgressBar progressBar;

    // Custom color scheme
    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private final Color ACCENT_COLOR = new Color(255, 140, 0);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color TEXT_COLOR = new Color(50, 50, 50);

    public Resources(String username) {
        this.username = username;

        setIconImage(app.App.getIcon());
        initializeUI();
        loadResources("");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeUI() {
        setTitle("Help Resources - " + username);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width* 1.02);
        int height = (int) (screenSize.height * 0.95);
        setSize(width, height);

        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JButton backButton = new JButton("←");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        backButton.setForeground(PRIMARY_COLOR);
        backButton.setBackground(BACKGROUND_COLOR);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 15));
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

        backButton.addActionListener(e -> {
            this.dispose();
            new Dashboard(username);
        });

        JLabel titleLabel = new JLabel("Help Resources", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        mainPanel.add(progressBar, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation((int) (getWidth() * 0.3));
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setResizeWeight(0.3);

        JPanel listPanel = createResourceListPanel();
        splitPane.setLeftComponent(listPanel);

        JPanel detailsPanel = createDetailsPanel();
        splitPane.setRightComponent(detailsPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setBackground(PRIMARY_COLOR);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        closeButton.addActionListener(e -> dispose());

        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_COLOR, 1),
                new EmptyBorder(8, 15, 8, 15)
        ));
        searchField.setForeground(TEXT_COLOR);

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchButton.setBorder(new EmptyBorder(8, 20, 8, 20));
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> performSearch());

        // Search as you type
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { performSearch(); }
            public void insertUpdate(DocumentEvent e) { performSearch(); }
            public void removeUpdate(DocumentEvent e) { performSearch(); }
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        return searchPanel;
    }

    private JPanel createResourceListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(BACKGROUND_COLOR);
        listPanel.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_COLOR, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));

        resourceListModel = new DefaultListModel<>();
        resourceList = new JList<>(resourceListModel);
        resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resourceList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        resourceList.setBackground(SECONDARY_COLOR);
        resourceList.setForeground(TEXT_COLOR);
        resourceList.setCellRenderer(new ResourceListCellRenderer());
        resourceList.addListSelectionListener(e -> showResourceDetails());

        JScrollPane scrollPane = new JScrollPane(resourceList);
        scrollPane.setBorder(null);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        return listPanel;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(BACKGROUND_COLOR);
        detailsPanel.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_COLOR, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        detailsArea.setBackground(SECONDARY_COLOR);
        detailsArea.setForeground(TEXT_COLOR);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setBorder(null);
        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        return detailsPanel;
    }



    private void performSearch() {
        String query = searchField.getText().trim();
        loadResources(query);
    }

    private void loadResources(String query) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(true);
                    progressBar.setVisible(true);
                });

                try {
                    String sql = "SELECT id, title, keywords, content FROM resources WHERE " +
                            "LOWER(title) LIKE ? OR LOWER(keywords) LIKE ? OR LOWER(content) LIKE ? " +
                            "ORDER BY title";

                    String searchPattern = "%" + query.toLowerCase() + "%";
                    ResultSet rs = Database.executeQuery(sql, searchPattern, searchPattern, searchPattern);

                    final List<Resource> resources = new ArrayList<>();
                    while (rs != null && rs.next()) {
                        resources.add(new Resource(
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("keywords"),
                                rs.getString("content")
                        ));
                    }

                    SwingUtilities.invokeLater(() -> {
                        resourceListModel.clear();
                        for (Resource res : resources) {
                            resourceListModel.addElement(res.getTitle());
                        }
                        resourceList.putClientProperty("resources", resources);
                    });

                    if (rs != null) {
                        Statement stmt = rs.getStatement();
                        Database.closeResources(rs, stmt);
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Error loading resources: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
            }
        };

        worker.execute();
    }

    private void showResourceDetails() {
        @SuppressWarnings("unchecked")
        List<Resource> resources = (List<Resource>) resourceList.getClientProperty("resources");
        int selectedIndex = resourceList.getSelectedIndex();

        if (selectedIndex >= 0 && resources != null && selectedIndex < resources.size()) {
            Resource selected = resources.get(selectedIndex);
            detailsArea.setText(String.format(
                    "Title: %s\n\nKeywords: %s\n\n%s",
                    selected.getTitle(),
                    selected.getKeywords(),
                    selected.getContent()
            ));
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private static class Resource {
        private final int id;
        private final String title;
        private final String keywords;
        private final String content;

        public Resource(int id, String title, String keywords, String content) {
            this.id = id;
            this.title = title;
            this.keywords = keywords;
            this.content = content;
        }

        public String getTitle() { return title; }
        public String getKeywords() { return keywords; }
        public String getContent() { return content; }
    }

    private class ResourceListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            label.setBorder(new EmptyBorder(5, 8, 5, 8));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            if (isSelected) {
                label.setBackground(PRIMARY_COLOR);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(index % 2 == 0 ? SECONDARY_COLOR :
                        new Color(230, 230, 230));
                label.setForeground(TEXT_COLOR);
            }

            return label;
        }
    }
}